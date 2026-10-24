package et.adey.mfi.reconciliation;

import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.common.ApiException;
import et.adey.mfi.common.FineractDates;
import et.adey.mfi.common.Money;
import et.adey.mfi.mobilemoney.MobileMoneyPostingService;
import et.adey.mfi.mobilemoney.PaymentIntent;
import et.adey.mfi.mobilemoney.PaymentIntentRepository;
import et.adey.mfi.mobilemoney.PaymentStatus;
import et.adey.mfi.mobilemoney.SettlementSource;
import et.adey.mfi.security.AccessPolicy;
import et.adey.mfi.security.Officer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Runs, shows and resolves reconciliations (see ReconciliationMatcher for the rules). */
@Slf4j
@Service
public class ReconciliationService {

    static final String SYSTEM_ACTOR = "system:reconciliation";

    private final SettlementSource settlements;
    private final FineractMobileMoneyReader fineractReader;
    private final PaymentIntentRepository intents;
    private final ReconciliationRunRepository runs;
    private final ReconciliationFindingRepository findings;
    private final MobileMoneyPostingService posting;
    private final AccessPolicy access;
    private final AuditService audit;
    private final Clock clock;
    private final boolean scheduleEnabled;

    public ReconciliationService(SettlementSource settlements, FineractMobileMoneyReader fineractReader,
                                 PaymentIntentRepository intents, ReconciliationRunRepository runs,
                                 ReconciliationFindingRepository findings, MobileMoneyPostingService posting,
                                 AccessPolicy access, AuditService audit, Clock clock,
                                 @Value("${app.reconciliation.enabled:true}") boolean scheduleEnabled) {
        this.settlements = settlements;
        this.fineractReader = fineractReader;
        this.intents = intents;
        this.runs = runs;
        this.findings = findings;
        this.posting = posting;
        this.access = access;
        this.audit = audit;
        this.clock = clock;
        this.scheduleEnabled = scheduleEnabled;
    }

    /** Every night: yesterday (Addis Ababa), once the provider's day is closed. */
    @Scheduled(cron = "${app.reconciliation.cron:0 30 0 * * *}", zone = "Africa/Addis_Ababa")
    public void nightly() {
        if (scheduleEnabled) {
            run(FineractDates.today().minusDays(1), SYSTEM_ACTOR);
        }
    }

    /** Manager-triggered run (demos, or after fixing something). Covers all offices; views are scoped. */
    public ReconciliationReport runAsManager(Officer manager, LocalDate date) {
        access.requireManager(manager);
        ReconciliationRun run = run(date, manager.username());
        return report(manager, run);
    }

    ReconciliationRun run(LocalDate date, String actor) {
        ReconciliationRun run = runs.save(new ReconciliationRun(date, actor, clock.instant()));
        try {
            List<ReconciliationMatcher.ProviderPayment> provider = settlements.forDate(date).stream()
                    .map(s -> new ReconciliationMatcher.ProviderPayment(s.chargeId(), s.reference(),
                            s.providerTransactionId(), s.amount(), s.settledAt()))
                    .toList();
            List<ReconciliationMatcher.IntentInfo> ours = provider.stream()
                    .map(p -> intents.findByReference(p.reference()).orElse(null))
                    .filter(Objects::nonNull)
                    .map(i -> new ReconciliationMatcher.IntentInfo(i.getId(), i.getReference(), i.getLoanId(),
                            i.getOfficeId(), i.getAmount(), i.getStatus().name()))
                    .toList();
            List<ReconciliationMatcher.FineractPayment> fineract = fineractReader.read(date);

            List<ReconciliationMatcher.Finding> result = ReconciliationMatcher.match(provider, ours, fineract);
            long runId = run.getId();
            result.forEach(f -> findings.save(carryResolution(new ReconciliationFinding(runId, f))));
            run.finished(clock.instant(), provider.size(), sum(provider, ReconciliationMatcher.ProviderPayment::amount),
                    fineract.size(), sum(fineract, ReconciliationMatcher.FineractPayment::amount));
            run = runs.save(run);
            String counts = countsText(result);
            audit.recordSystem(actor, AuditAction.RECONCILIATION_RUN, null, null, null, AuditOutcome.SUCCESS,
                    "date " + date + ": " + counts);
            log.info("Reconciliation {} for {} by {}: {}", run.getId(), date, actor, counts);
            return run;
        } catch (RuntimeException e) {
            run.failed(clock.instant(), e.getMessage());
            runs.save(run);
            audit.recordSystem(actor, AuditAction.RECONCILIATION_RUN, null, null, null, AuditOutcome.ERROR,
                    "date " + date + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * Every run re-creates its findings. If a manager already settled the same difference (same
     * type, same receipt) in an earlier run, the new finding inherits that resolution, so nobody has
     * to explain the same thing again every day.
     */
    private ReconciliationFinding carryResolution(ReconciliationFinding f) {
        if (f.getType() != FindingType.MATCHED && f.getReceiptNumber() != null) {
            findings.findFirstByTypeAndReceiptNumberAndResolvedAtIsNotNullOrderByIdDesc(f.getType(), f.getReceiptNumber())
                    .ifPresent(previous -> f.resolve(previous.getResolvedBy(), previous.getResolvedAt(),
                            "(carried over from finding " + previous.getId() + ") " + previous.getResolutionNote()));
        }
        return f;
    }

    public ReconciliationReport view(Officer manager, LocalDate date) {
        access.requireManager(manager);
        ReconciliationRun run = runs.findTopByBusinessDateAndStatusOrderByIdDesc(date, "COMPLETED")
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND",
                        "No completed reconciliation for " + date + ". Run it first."));
        return report(manager, run);
    }

    /**
     * PAID_NOT_POSTED with a known payment intent: post it now through the normal posting path.
     * Everything else: the manager explains what was done (note required) and it is marked resolved.
     */
    public ReconciliationReport.FindingResponse resolve(Officer manager, long findingId, ResolveRequest req) {
        return audit.record(manager, AuditAction.RECONCILIATION_RESOLVE, rec -> {
            access.requireManager(manager);
            ReconciliationFinding f = findings.findById(findingId).orElseThrow(() ->
                    new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "No finding " + findingId + "."));
            rec.loanId(f.getLoanId()).amount(f.getProviderAmount());
            if (f.getOfficeId() != null && !Objects.equals(f.getOfficeId(), manager.officeId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Finding " + findingId + " belongs to another office.");
            }
            if (f.isResolved()) {
                throw new ApiException(HttpStatus.CONFLICT, "FINDING_ALREADY_RESOLVED",
                        "Finding " + findingId + " was already resolved by " + f.getResolvedBy() + ".");
            }
            String note = req == null ? null : req.note();
            if (f.getType() == FindingType.PAID_NOT_POSTED && f.getIntentId() != null) {
                PaymentIntent intent = posting.acceptFromReconciliation(f.getIntentId(), f.getReceiptNumber(),
                        f.getProviderAmount(), f.getProviderPaidAt(),
                        "found by reconciliation run " + f.getRunId() + ", resolved by " + manager.username());
                rec.clientId(intent.getClientId());
                if (intent.getStatus() != PaymentStatus.POSTED) {
                    throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "POSTING_PENDING",
                            "Payment accepted but not posted yet (" + intent.getStatus() + ": " + intent.getLastError()
                                    + "). The posting worker will retry; resolve again later.");
                }
                f.resolve(manager.username(), clock.instant(), StringUtils.hasText(note) ? note
                        : "Posted via reconciliation as Fineract transaction " + intent.getFineractTransactionId());
            } else {
                if (!StringUtils.hasText(note)) {
                    throw new ApiException(HttpStatus.BAD_REQUEST, "RESOLUTION_NOTE_REQUIRED",
                            "Explain how this " + f.getType() + " finding was settled (note).");
                }
                f.resolve(manager.username(), clock.instant(), note);
            }
            return ReconciliationReport.FindingResponse.of(findings.save(f));
        });
    }

    private ReconciliationReport report(Officer manager, ReconciliationRun run) {
        List<ReconciliationFinding> mine = findings.findByRunIdOrderByIdAsc(run.getId()).stream()
                .filter(f -> f.getOfficeId() == null || Objects.equals(f.getOfficeId(), manager.officeId()))
                .toList();
        ReconciliationReport.Summary summary = new ReconciliationReport.Summary(
                count(mine, FindingType.MATCHED), count(mine, FindingType.PAID_NOT_POSTED),
                count(mine, FindingType.POSTED_NOT_PAID), count(mine, FindingType.AMOUNT_MISMATCH),
                (int) mine.stream().filter(f -> f.getType() != FindingType.MATCHED && !f.isResolved()).count(),
                sum(mine, ReconciliationFinding::getProviderAmount), sum(mine, ReconciliationFinding::getFineractAmount),
                sum(mine.stream().filter(f -> f.getType() == FindingType.MATCHED).toList(),
                        ReconciliationFinding::getProviderAmount));
        return new ReconciliationReport(run.getBusinessDate(), run.getId(), run.getStartedAt(), run.getTriggeredBy(),
                summary, mine.stream().map(ReconciliationReport.FindingResponse::of).toList());
    }

    private static int count(List<ReconciliationFinding> list, FindingType type) {
        return (int) list.stream().filter(f -> f.getType() == type).count();
    }

    private static <T> BigDecimal sum(List<T> list, Function<T, BigDecimal> amount) {
        return Money.orZero(list.stream().map(amount).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static String countsText(List<ReconciliationMatcher.Finding> result) {
        StringBuilder sb = new StringBuilder();
        for (FindingType t : FindingType.values()) {
            long n = result.stream().filter(f -> f.type() == t).count();
            sb.append(t).append('=').append(n).append(' ');
        }
        return sb.toString().trim();
    }
}
