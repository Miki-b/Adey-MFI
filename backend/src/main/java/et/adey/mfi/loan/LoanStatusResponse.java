package et.adey.mfi.loan;

/**
 * Result of a lifecycle step: the loan and the status it is now in.
 * statusLabel is Fineract's human text, e.g. "Submitted and pending approval".
 */
public record LoanStatusResponse(Long loanId, String accountNo, LoanStatus status, String statusLabel) {
}
