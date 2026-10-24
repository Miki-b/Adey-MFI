package et.adey.mfi.mobilemoney;

import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.common.FineractDates;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.FineractUnavailableException;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanCommands;
import et.adey.mfi.fineract.model.FineractLoanTransaction;
import et.adey.mfi.fineract.model.FineractPaymentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The POSTING WORKER: turns RECEIVED payments into Fineract repayments.
 *
 * Runs every few seconds. For each payment due now:
 *  1. Already in Fineract? Look for a repayment on the loan whose receipt number is the provider's
 *     transaction id. If found (e.g. our previous attempt reached Fineract but its answer got lost),
 *     just record it as POSTED. This is the second guard against double-posting, after our own
 *     DB status.
 *  2. Otherwise post the repayment (payment type "Mobile Money", receipt = provider txn id).
 *  3. Fineract unreachable or 5xx: RETRYING, next attempt after an exponential backoff;
 *     after maxAttempts: NEEDS_ATTENTION. Fineract refused (4xx, e.g. loan closed meanwhile):
 *     NEEDS_ATTENTION at once, because retrying won't change the answer.
 * The intent row has a version column (optimistic locking), so two workers can never both move it.
 */
@Slf4j
@Service
public class MobileMoneyPostingService {

    static final String ACTOR = "system:mobile-money-worker";

    private final FineractClient fineract;
    private final PaymentIntentRepository intents;
    private final MobileMoneyProperties props;
    private final RetryPolicy retry;
    private final AuditService audit;
    private final Clock clock;
    private final AtomicReference<Long> paymentTypeId = new AtomicReference<>();

    public MobileMoneyPostingService(FineractClient fineract, PaymentIntentRepository intents,
                                     MobileMoneyProperties props, AuditService audit, Clock clock) {
        this.fineract = fineract;
        this.intents = intents;
        this.props = props;
        this.retry = RetryPolicy.from(props.posting());
        this.audit = audit;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.mobile-money.posting.poll-interval:5s}", initialDelayString = "10s")
    public void runScheduled() {
        if (props.workersEnabled()) {
            postDue();
        }
    }

    /** Handles every payment whose next attempt is due. Returns how many were handled. */
    public int postDue() {
        var due = intents.findTop20ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
                EnumSet.of(PaymentStatus.RECEIVED, PaymentStatus.RETRYING), clock.instant());
        for (PaymentIntent intent : due) {
            try {
                postOne(intent.getId());
            } catch (ObjectOptimisticLockingFailureException e) {
                log.info("Payment {} was updated concurrently; skipping this round", intent.getReference());
            }
        }
        return due.size();
    }

    /** One attempt for one payment (also used by reconciliation). Returns its new state. */
    public PaymentIntent postOne(long intentId) {
        PaymentIntent intent = intents.findById(intentId).orElseThrow();
        if (!intent.getStatus().isAwaitingPosting()) {
            return intent; // our DB says: already handled (first guard)
        }
        Instant now = clock.instant();
        try {
            Optional<FineractLoanTransaction> existing = findByReceipt(intent);
            if (existing.isPresent()) {
                intent.posted(existing.get().id());
                intent.moveTo(PaymentStatus.POSTED, now,
                        "already in Fineract as transaction " + existing.get().id() + " (receipt found), not posted again");
                intent = intents.save(intent);
                auditPosted(intent, "found existing Fineract transaction " + existing.get().id());
                return intent;
            }
            FineractCommandResult result = fineract.repayLoan(intent.getLoanId(), new FineractLoanCommands.Repayment(
                    FineractDates.format(paymentDate(intent)), intent.getAmount(), mobileMoneyPaymentTypeId(),
                    "Mobile money " + intent.getReference(), intent.getProviderTransactionId(),
                    FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            intent.posted(result.resourceId());
            intent.moveTo(PaymentStatus.POSTED, now, "posted as Fineract transaction " + result.resourceId());
            intent = intents.save(intent);
            auditPosted(intent, "Fineract transaction " + result.resourceId());
            log.info("Payment {} posted to loan {} as transaction {}", intent.getReference(), intent.getLoanId(),
                    result.resourceId());
            return intent;
        } catch (FineractUnavailableException e) {
            return failed(intent, now, "Fineract unavailable: " + e.getMessage(), true);
        } catch (FineractApiException e) {
            boolean retryable = e.getFineractStatus() >= 500 || e.getFineractStatus() == 401;
            return failed(intent, now, "Fineract HTTP " + e.getFineractStatus() + ": " + e.getError().defaultUserMessage(),
                    retryable);
        }
    }

    private PaymentIntent failed(PaymentIntent intent, Instant now, String error, boolean retryable) {
        int attempts = intent.getPostingAttempts() + 1;
        if (!retryable || retry.shouldGiveUp(attempts)) {
            intent.attemptFailed(error, null);
            String why = retryable ? "gave up after " + attempts + " attempts: " + error : "not retryable: " + error;
            intent.moveTo(PaymentStatus.NEEDS_ATTENTION, now, why);
            intent = intents.save(intent);
            audit.recordSystem(ACTOR, AuditAction.MOBILE_PAYMENT_NEEDS_ATTENTION, intent.getClientId(),
                    intent.getLoanId(), intent.getAmount(), AuditOutcome.ERROR, intent.getReference() + ": " + why);
            log.error("Payment {} needs attention: {}", intent.getReference(), why);
            return intent;
        }
        Instant next = retry.nextAttemptAt(now, attempts);
        intent.attemptFailed(error, next);
        intent.moveTo(PaymentStatus.RETRYING, now, "attempt " + attempts + " failed, next at " + next + ": " + error);
        intent = intents.save(intent);
        audit.recordSystem(ACTOR, AuditAction.MOBILE_PAYMENT_RETRY, intent.getClientId(), intent.getLoanId(),
                intent.getAmount(), AuditOutcome.ERROR, "%s: attempt %d failed, retry in %s (%s)".formatted(
                        intent.getReference(), attempts, retry.delayAfter(attempts), error));
        log.warn("Payment {}: attempt {} failed, retry at {}", intent.getReference(), attempts, next);
        return intent;
    }

    /**
     * Reconciliation found a provider payment whose webhook never arrived (intent still PENDING):
     * record what the provider's settlement says, then post through the SAME path as the worker
     * (same receipt check, same Fineract call).
     */
    public PaymentIntent acceptFromReconciliation(long intentId, String providerTransactionId,
                                                  java.math.BigDecimal providerAmount, Instant paidAt, String note) {
        PaymentIntent intent = intents.findById(intentId).orElseThrow();
        if (intent.getStatus() == PaymentStatus.PENDING) {
            Instant now = clock.instant();
            intent.paid(providerTransactionId, providerAmount, paidAt);
            intent.moveTo(PaymentStatus.RECEIVED, now, note);
            intent.scheduleAttempt(now);
            intents.save(intent);
        }
        return postOne(intentId);
    }

    /** Second guard against double-posting: does the loan already have a repayment with this receipt? */
    Optional<FineractLoanTransaction> findByReceipt(PaymentIntent intent) {
        FineractLoan loan = fineract.getLoanWithScheduleAndTransactions(intent.getLoanId());
        if (loan.transactions() == null || intent.getProviderTransactionId() == null) {
            return Optional.empty();
        }
        return loan.transactions().stream()
                .filter(tx -> !Boolean.TRUE.equals(tx.manuallyReversed()))
                .filter(tx -> tx.paymentDetailData() != null
                        && Objects.equals(tx.paymentDetailData().receiptNumber(), intent.getProviderTransactionId()))
                .findFirst();
    }

    /** The repayment is dated the day the client paid (Addis Ababa time), not the day we post it. */
    private LocalDate paymentDate(PaymentIntent intent) {
        Instant paid = intent.getPaidAt() != null ? intent.getPaidAt() : clock.instant();
        return paid.atZone(FineractDates.BUSINESS_ZONE).toLocalDate();
    }

    long mobileMoneyPaymentTypeId() {
        Long id = paymentTypeId.get();
        if (id == null) {
            id = fineract.getPaymentTypes().stream()
                    .filter(p -> props.paymentTypeName().equalsIgnoreCase(p.name()))
                    .map(FineractPaymentType::id)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Fineract has no payment type '"
                            + props.paymentTypeName() + "'. Run the seed profile."));
            paymentTypeId.set(id);
        }
        return id;
    }

    private void auditPosted(PaymentIntent intent, String detail) {
        audit.recordSystem(ACTOR, AuditAction.MOBILE_PAYMENT_POSTED, intent.getClientId(), intent.getLoanId(),
                intent.getAmount(), AuditOutcome.SUCCESS, intent.getReference() + " receipt "
                        + intent.getProviderTransactionId() + ": " + detail);
    }
}
