package et.adey.mfi.portfolio;

import et.adey.mfi.loan.LoanStatus;

import java.util.Set;

/** Status filter of GET /api/loans, in loan-officer words. */
public enum LoanListStatus {
    PENDING(Set.of(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL)),
    APPROVED(Set.of(LoanStatus.APPROVED)),
    ACTIVE(Set.of(LoanStatus.ACTIVE)),
    /** No longer running: repaid, written off or replaced by a rescheduled loan. */
    CLOSED(Set.of(LoanStatus.CLOSED, LoanStatus.WRITTEN_OFF, LoanStatus.RESCHEDULED)),
    REJECTED(Set.of(LoanStatus.REJECTED, LoanStatus.WITHDRAWN_BY_CLIENT));

    private final Set<LoanStatus> statuses;

    LoanListStatus(Set<LoanStatus> statuses) {
        this.statuses = statuses;
    }

    public boolean matches(LoanStatus status) {
        return statuses.contains(status);
    }
}
