package et.adey.mfi.mobilemoney;

import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.audit.AuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * INBOX PATTERN: a verified webhook is first written to our own table (webhook_events), in the
 * same database transaction as the intent's status change, and acknowledged with 200 straight
 * away. The slow, failure-prone part (posting to Fineract) happens later in the posting worker.
 *
 * Why: the provider only waits a few seconds and re-sends if we don't answer 2xx. If we posted to
 * Fineract inside the webhook and Fineract was slow or down, we would time out, the provider
 * would retry, and we could double-post. With the inbox the webhook answer never depends on Fineract,
 * and nothing is lost: the stored row survives a crash or restart.
 */
@Slf4j
@Service
public class WebhookInbox {

    static final String ACTOR = "system:mobile-money-webhook";

    private final WebhookEventRepository events;
    private final PaymentIntentRepository intents;
    private final AuditService audit;
    private final Clock clock;
    private final TransactionTemplate transaction;

    public WebhookInbox(WebhookEventRepository events, PaymentIntentRepository intents, AuditService audit,
                        Clock clock, PlatformTransactionManager transactionManager) {
        this.events = events;
        this.intents = intents;
        this.audit = audit;
        this.clock = clock;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    public enum Result { ACCEPTED, DUPLICATE, MISMATCH, UNKNOWN_REFERENCE, IGNORED }

    /**
     * How we learnt the provider's result: its signed webhook (simulator), or our own call to its
     * verify API (Chapa: after a webhook hint, or by polling). Only wording and audit differ.
     */
    public enum Source {
        WEBHOOK("webhook", ACTOR, AuditAction.WEBHOOK),
        VERIFY_API("verify API", PaymentVerifier.ACTOR, AuditAction.MOBILE_PAYMENT_VERIFY);

        final String label;
        final String actor;
        final AuditAction auditAction;

        Source(String label, String actor, AuditAction auditAction) {
            this.label = label;
            this.actor = actor;
            this.auditAction = auditAction;
        }
    }

    /** Stores and applies one provider result that arrived as a signed webhook. */
    public Result receive(ProviderPaymentEvent w, String rawBody) {
        return receive(w, rawBody, Source.WEBHOOK);
    }

    /** Stores and applies one provider result. Never calls Fineract. */
    public Result receive(ProviderPaymentEvent w, String rawBody, Source source) {
        Applied applied;
        try {
            // One transaction: the inbox row and the intent's new status are saved together or not at all.
            applied = transaction.execute(status -> store(w, rawBody, source));
        } catch (DataIntegrityViolationException race) {
            // The same eventId arrived twice at the same moment; the other one won.
            applied = new Applied(Result.DUPLICATE, null, "duplicate eventId (concurrent delivery)");
        }
        auditResult(w, applied, source);
        return applied.result();
    }

    record Applied(Result result, PaymentIntent intent, String detail) {
    }

    private Applied store(ProviderPaymentEvent w, String rawBody, Source source) {
        if (events.existsByEventId(w.eventId())) {
            return new Applied(Result.DUPLICATE, null, "duplicate eventId, ignored");
        }
        Instant now = clock.instant();
        WebhookEvent event = new WebhookEvent(w, now, rawBody);

        Optional<PaymentIntent> found = w.reference() == null ? Optional.empty() : intents.findByReference(w.reference());
        if (found.isEmpty() || !Objects.equals(found.get().getProviderChargeId(), w.chargeId())) {
            event.outcome(WebhookEvent.Outcome.UNKNOWN_REFERENCE, null, "no payment intent with this reference/charge");
            events.saveAndFlush(event);
            return new Applied(Result.UNKNOWN_REFERENCE, null, "unknown reference " + w.reference());
        }
        PaymentIntent intent = found.get();
        if (intent.getStatus() != PaymentStatus.PENDING) {
            event.outcome(WebhookEvent.Outcome.IGNORED, intent.getId(), "intent already " + intent.getStatus());
            events.saveAndFlush(event);
            return new Applied(Result.IGNORED, intent, "intent already " + intent.getStatus());
        }

        if ("DECLINED".equals(w.status())) {
            intent.moveTo(PaymentStatus.DECLINED, now,
                    "provider result (" + source.label + "): declined or failed at the provider (event " + w.eventId() + ")");
            event.outcome(WebhookEvent.Outcome.ACCEPTED, intent.getId(), "declined");
        } else if ("SUCCEEDED".equals(w.status())) {
            intent.paid(w.providerTransactionId(), w.amount(), w.timestamp());
            boolean sameAmount = w.amount() != null && w.amount().compareTo(intent.getAmount()) == 0;
            boolean sameCurrency = Objects.equals(w.currency(), intent.getCurrency());
            if (!sameAmount || !sameCurrency) {
                String why = "provider reports %s %s, we requested %s %s".formatted(
                        w.amount(), w.currency(), intent.getAmount(), intent.getCurrency());
                intent.moveTo(PaymentStatus.MISMATCH, now, "NOT posted: " + why);
                event.outcome(WebhookEvent.Outcome.MISMATCH, intent.getId(), why);
                intents.save(intent);
                events.saveAndFlush(event);
                return new Applied(Result.MISMATCH, intent, why);
            }
            intent.moveTo(PaymentStatus.RECEIVED, now,
                    "provider result (" + source.label + "): paid, txn " + w.providerTransactionId());
            intent.scheduleAttempt(now); // the posting worker picks it up on its next run
            event.outcome(WebhookEvent.Outcome.ACCEPTED, intent.getId(), "paid");
        } else {
            event.outcome(WebhookEvent.Outcome.IGNORED, intent.getId(), "unknown status " + w.status());
            events.saveAndFlush(event);
            return new Applied(Result.IGNORED, intent, "unknown status " + w.status());
        }
        intents.save(intent);
        events.saveAndFlush(event);
        return new Applied(Result.ACCEPTED, intent, w.status());
    }

    private void auditResult(ProviderPaymentEvent w, Applied a, Source source) {
        Long loanId = a.intent() == null ? null : a.intent().getLoanId();
        Long clientId = a.intent() == null ? null : a.intent().getClientId();
        AuditOutcome outcome = a.result() == Result.ACCEPTED ? AuditOutcome.SUCCESS : AuditOutcome.REJECTED;
        audit.recordSystem(source.actor, source.auditAction, clientId, loanId, w.amount(), outcome,
                "provider result %s ref %s via %s: %s (%s)".formatted(w.eventId(), w.reference(), source.label,
                        a.result(), a.detail()));
        log.info("Provider result {} for {} via {}: {} ({})", w.eventId(), w.reference(), source.label, a.result(),
                a.detail());
    }

    /** Signature or timestamp wrong: nothing is stored in the inbox, only the audit row. */
    public void rejected(String reason, String remoteAddr) {
        audit.recordSystem(ACTOR, AuditAction.WEBHOOK, null, null, null, AuditOutcome.DENIED,
                reason + "; from " + remoteAddr);
        log.warn("Webhook rejected from {}: {}", remoteAddr, reason);
    }
}
