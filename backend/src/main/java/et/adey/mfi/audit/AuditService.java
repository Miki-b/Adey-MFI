package et.adey.mfi.audit;

import et.adey.mfi.auth.AuthenticationFailedException;
import et.adey.mfi.common.BadRequestException;
import et.adey.mfi.common.ConflictException;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.security.Officer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.function.Function;

/**
 * Wraps a state-changing action so that exactly one audit row is written, whether it succeeds or
 * fails. The action gets an {@link AuditRecord} and fills in the client / loan / amount as soon as it
 * knows them, so even a refused attempt is logged with as much detail as was known at that point.
 *
 * Each row is saved in its own small transaction (the callers are not transactional), so a failed
 * action still leaves its audit row.
 */
@Slf4j
@Service
public class AuditService {

    private final AuditLogRepository repository;
    private final Clock clock;

    public AuditService(AuditLogRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** The details of one audited action, filled in while the action runs. */
    public static final class AuditRecord {
        private Long clientId;
        private Long loanId;
        private BigDecimal amount;

        public AuditRecord clientId(Long clientId) {
            this.clientId = clientId;
            return this;
        }

        public AuditRecord loanId(Long loanId) {
            this.loanId = loanId;
            return this;
        }

        public AuditRecord amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
    }

    public <T> T record(Officer officer, AuditAction action, Function<AuditRecord, T> work) {
        AuditRecord rec = new AuditRecord();
        try {
            T result = work.apply(rec);
            write(officer, action, rec, AuditOutcome.SUCCESS, null);
            return result;
        } catch (RuntimeException e) {
            write(officer, action, rec, outcomeOf(e), e.getMessage());
            throw e;
        }
    }

    /**
     * Login attempts have no logged-in officer yet, so they are recorded with what we know:
     * the username typed, and (on success) the user/staff/office. The IP goes into detail.
     */
    public void recordLogin(String username, Officer officer, String ip, AuditOutcome outcome, String reason) {
        try {
            repository.save(new AuditLogEntry(clock.instant(), truncateTo(username, 100),
                    officer == null ? null : officer.userId(), officer == null ? null : officer.staffId(),
                    officer == null ? null : officer.officeId(), AuditAction.LOGIN, null, null, null, outcome,
                    truncate("ip=" + ip + (reason == null ? "" : "; " + reason))));
        } catch (RuntimeException e) {
            log.error("AUDIT WRITE FAILED: LOGIN {} by {}: {}", outcome, username, e.getMessage());
        }
    }

    private static String truncateTo(String s, int max) {
        return s == null || s.length() <= max ? s : s.substring(0, max);
    }

    /** Also used by IdempotencyService: DENIED/REJECTED = nothing happened, ERROR = maybe something did. */
    public static AuditOutcome outcomeOf(RuntimeException e) {
        if (e instanceof AccessDeniedException || e instanceof AuthenticationFailedException) {
            return AuditOutcome.DENIED;
        }
        if (e instanceof ConflictException || e instanceof BadRequestException) {
            return AuditOutcome.REJECTED;
        }
        if (e instanceof FineractApiException f && f.getFineractStatus() < 500 && f.getFineractStatus() != 401) {
            return AuditOutcome.REJECTED;
        }
        return AuditOutcome.ERROR;
    }

    private void write(Officer o, AuditAction action, AuditRecord rec, AuditOutcome outcome, String detail) {
        try {
            repository.save(new AuditLogEntry(clock.instant(), o.username(), o.userId(), o.staffId(), o.officeId(),
                    action, rec.clientId, rec.loanId, rec.amount, outcome, truncate(detail)));
        } catch (RuntimeException e) {
            // Don't hide the action's real outcome because the audit write failed, but make it loud.
            log.error("AUDIT WRITE FAILED: {} loan {} by {} ({}): {}", action, rec.loanId, o.username(), outcome,
                    e.getMessage());
        }
    }

    private static String truncate(String s) {
        return s == null || s.length() <= 1000 ? s : s.substring(0, 1000);
    }
}
