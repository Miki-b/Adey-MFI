package et.adey.mfi.loan;

/** Loan lifecycle, from Fineract's numeric status ids. */
public enum LoanStatus {
    SUBMITTED_AND_PENDING_APPROVAL,
    APPROVED,
    ACTIVE,
    WITHDRAWN_BY_CLIENT,
    REJECTED,
    CLOSED,
    WRITTEN_OFF,
    RESCHEDULED,
    OVERPAID,
    UNKNOWN;

    public static LoanStatus fromFineractId(Integer id) {
        if (id == null) return UNKNOWN;
        return switch (id) {
            case 100 -> SUBMITTED_AND_PENDING_APPROVAL;
            case 200 -> APPROVED;
            case 300 -> ACTIVE;
            case 400 -> WITHDRAWN_BY_CLIENT;
            case 500 -> REJECTED;
            case 600 -> CLOSED;
            case 601 -> WRITTEN_OFF;
            case 602 -> RESCHEDULED;
            case 700 -> OVERPAID;
            default -> UNKNOWN;
        };
    }
}
