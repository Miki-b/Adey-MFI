package et.adey.mfi.mobilemoney;

import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.Verification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * VERIFY BEFORE TRUSTING (providers whose webhooks are only hints: Chapa).
 *
 * A Chapa webhook proves little on its own (no timestamp, no event id, see ChapaWebhookSignature),
 * so it never moves money. Instead, on every such webhook AND on a timer for every PENDING
 * payment, we ask the provider's verify API, over our own authenticated HTTPS connection, what
 * really happened:
 *   success + same tx_ref + same Chapa reference -> a ProviderPaymentEvent into the SAME inbox as
 *       the simulator's webhooks; the inbox checks amount and currency (else MISMATCH) and
 *       schedules posting; the SAME posting worker posts it, with Chapa's reference as receipt.
 *   failed/cancelled                    -> DECLINED (via the inbox), nothing posted
 *   pending / provider down             -> check again later, with backoff
 *   provider never heard of it          -> CHARGE_FAILED if the charge never started
 *   unknown status (refunded, ...)      -> NEEDS_ATTENTION, never POSTED
 * The polling means a payment completes even if no webhook ever arrives.
 */
@Slf4j
@Service
public class PaymentVerifier {

    static final String ACTOR = "system:mobile-money-verifier";

    public enum Result {
        RECEIVED, DECLINED, DUPLICATE, MISMATCH, STILL_PENDING, PROVIDER_UNAVAILABLE, CHARGE_FAILED,
        NEEDS_ATTENTION, UNKNOWN_REFERENCE, IGNORED
    }

    private final MobileMoneyProvider provider;
    private final PaymentIntentRepository intents;
    private final WebhookInbox inbox;
    private final MobileMoneyProperties props;
    private final RetryPolicy retry;
    private final AuditService audit;
    private final Clock clock;

    public PaymentVerifier(MobileMoneyProvider provider, PaymentIntentRepository intents, WebhookInbox inbox,
                           MobileMoneyProperties props, AuditService audit, Clock clock) {
        this.provider = provider;
        this.intents = intents;
        this.inbox = inbox;
        this.props = props;
        this.retry = RetryPolicy.from(props.verify());
        this.audit = audit;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.mobile-money.verify.poll-interval:5s}", initialDelayString = "15s")
    public void runScheduled() {
        if (props.workersEnabled() && props.verify().pollingEnabled() && provider.needsVerification()) {
            verifyDue();
        }
    }

    /** Verifies every PENDING payment whose next check is due. Returns how many were checked. */
    public int verifyDue() {
        var due = intents.findTop20ByStatusAndProviderAndNextVerifyAtLessThanEqualOrderByNextVerifyAtAsc(
                PaymentStatus.PENDING, provider.name(), clock.instant());
        for (PaymentIntent intent : due) {
            try {
                verifyOne(intent.getId(), "poll", null);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.info("Payment {} was updated concurrently (by a webhook?); skipping this round", intent.getReference());
            }
        }
        return due.size();
    }

    /** A signed webhook named one of our references: verify it, never act on the webhook's own claims. */
    public Result onWebhook(String reference, String event, String rawBody) {
        Optional<PaymentIntent> found = intents.findByReference(reference);
        if (found.isEmpty() || !provider.name().equals(found.get().getProvider())) {
            audit.recordSystem(WebhookInbox.ACTOR, AuditAction.WEBHOOK, null, null, null, AuditOutcome.REJECTED,
                    "%s webhook %s for unknown tx_ref %s: ignored".formatted(provider.name(), event, reference));
            return Result.UNKNOWN_REFERENCE;
        }
        PaymentIntent intent = found.get();
        if (intent.getStatus() != PaymentStatus.PENDING) {
            String e = event == null ? "" : event.toLowerCase(Locale.ROOT);
            boolean moneyBack = e.contains("refund") || e.contains("revers");
            audit.recordSystem(WebhookInbox.ACTOR, moneyBack ? AuditAction.MOBILE_PAYMENT_NEEDS_ATTENTION : AuditAction.WEBHOOK,
                    intent.getClientId(), intent.getLoanId(), intent.getAmount(),
                    moneyBack ? AuditOutcome.ERROR : AuditOutcome.REJECTED,
                    "%s webhook %s for %s: payment already %s%s".formatted(provider.name(), event, reference,
                            intent.getStatus(), moneyBack ? "; check it and reverse the repayment in Fineract by hand" : ", ignored"));
            log.info("Webhook {} for {}: intent already {}, nothing to do", event, reference, intent.getStatus());
            return Result.IGNORED;
        }
        try {
            return verifyOne(intent.getId(), "webhook " + event, rawBody);
        } catch (ObjectOptimisticLockingFailureException e) {
            return Result.IGNORED; // the poller handled it at the same moment
        }
    }

    /** One verify call for one PENDING payment, and what we do with the answer. */
    public Result verifyOne(long intentId, String trigger, String rawBody) {
        PaymentIntent intent = intents.findById(intentId).orElseThrow();
        if (intent.getStatus() != PaymentStatus.PENDING) {
            return Result.IGNORED;
        }
        Instant now = clock.instant();
        Verification v;
        try {
            v = provider.verifyCharge(intent.getReference());
        } catch (ProviderUnavailableException e) {
            return inconclusive(intent, now, "verify failed (" + trigger + "): " + e.getMessage(), Result.PROVIDER_UNAVAILABLE);
        }
        log.info("Verified {} ({}): provider says {}", intent.getReference(), trigger,
                v.rawStatus() != null ? v.rawStatus() : v.status());

        switch (v.status()) {
            case PENDING -> {
                return inconclusive(intent, now, "still pending at " + provider.name() + " (" + trigger + ")",
                        Result.STILL_PENDING);
            }
            case NOT_FOUND -> {
                if (intent.getProviderChargeId() == null) {
                    // The charge was never started (provider refused it) and the provider has no record: no money moved.
                    String why = provider.name() + " has no payment " + intent.getReference()
                            + " (charge was not initiated: " + intent.getLastError() + ")";
                    return finish(intent, now, PaymentStatus.CHARGE_FAILED, why, AuditOutcome.REJECTED, Result.CHARGE_FAILED);
                }
                return inconclusive(intent, now, provider.name() + " does not know " + intent.getReference() + " yet",
                        Result.STILL_PENDING);
            }
            case UNKNOWN -> {
                return finish(intent, now, PaymentStatus.NEEDS_ATTENTION, "NOT posted: unknown " + provider.name()
                        + " status '" + v.rawStatus() + "'", AuditOutcome.ERROR, Result.NEEDS_ATTENTION);
            }
            case SUCCEEDED, DECLINED -> {
                return applyDecision(intent, v, rawBody);
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private Result applyDecision(PaymentIntent intent, Verification v, String rawBody) {
        Instant now = clock.instant();
        // Reference checks: the answer must be about OUR tx_ref, and about the charge Chapa gave us.
        if (!intent.getReference().equals(v.reference())) {
            return finish(intent, now, PaymentStatus.MISMATCH, "NOT posted: verify answered for tx_ref " + v.reference(),
                    AuditOutcome.ERROR, Result.MISMATCH);
        }
        if (intent.getProviderChargeId() != null && v.providerReference() != null
                && !Objects.equals(intent.getProviderChargeId(), v.providerReference())) {
            return finish(intent, now, PaymentStatus.MISMATCH, "NOT posted: provider reference " + v.providerReference()
                    + " differs from charge " + intent.getProviderChargeId(), AuditOutcome.ERROR, Result.MISMATCH);
        }
        if (intent.getProviderChargeId() == null && v.providerReference() != null) {
            intent.chargeCreated(v.providerReference()); // the charge answer had no id; verify (authenticated) does
            intent = intents.save(intent);
        }
        // From here on: exactly the simulator's path. One event id per payment decision, whichever
        // way (webhook or poll) it is learnt first; the inbox's unique event id stops a second one.
        ProviderPaymentEvent event = new ProviderPaymentEvent(
                provider.name() + ":" + intent.getReference() + ":" + v.status(),
                intent.getProviderChargeId(), intent.getReference(), v.amount(), v.currency(), v.status().name(),
                v.providerReference(), v.paidAt());
        WebhookInbox.Result r = inbox.receive(event, rawBody != null ? rawBody : String.valueOf(v.summary()),
                WebhookInbox.Source.VERIFY_API);
        return switch (r) {
            case ACCEPTED -> v.status() == MobileMoneyProvider.VerifiedStatus.SUCCEEDED ? Result.RECEIVED : Result.DECLINED;
            case DUPLICATE -> Result.DUPLICATE;
            case MISMATCH -> Result.MISMATCH;
            case UNKNOWN_REFERENCE -> Result.UNKNOWN_REFERENCE;
            case IGNORED -> Result.IGNORED;
        };
    }

    /** No final answer yet: try again after a backoff; after maxAttempts checks a person must look. */
    private Result inconclusive(PaymentIntent intent, Instant now, String note, Result result) {
        int attempts = intent.getVerifyAttempts() + 1;
        if (retry.shouldGiveUp(attempts)) {
            intent.verificationInconclusive(note, null);
            return finish(intent, now, PaymentStatus.NEEDS_ATTENTION, "no final answer after " + attempts
                    + " verify checks: " + note, AuditOutcome.ERROR, Result.NEEDS_ATTENTION);
        }
        Instant next = retry.nextAttemptAt(now, attempts);
        intent.verificationInconclusive(note, next);
        intents.save(intent);
        log.info("Payment {}: {}; next check at {}", intent.getReference(), note, next);
        return result;
    }

    private Result finish(PaymentIntent intent, Instant now, PaymentStatus to, String why, AuditOutcome outcome,
                          Result result) {
        intent.moveTo(to, now, why);
        intent.recordError(why);
        intent = intents.save(intent);
        audit.recordSystem(ACTOR, to == PaymentStatus.NEEDS_ATTENTION ? AuditAction.MOBILE_PAYMENT_NEEDS_ATTENTION
                        : AuditAction.MOBILE_PAYMENT_VERIFY, intent.getClientId(), intent.getLoanId(), intent.getAmount(),
                outcome, intent.getReference() + " -> " + to + ": " + why);
        log.warn("Payment {} -> {}: {}", intent.getReference(), to, why);
        return result;
    }
}
