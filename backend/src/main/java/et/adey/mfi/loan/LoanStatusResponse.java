package et.adey.mfi.loan;

import org.jspecify.annotations.Nullable;

/**
 * Result of a lifecycle step: the loan and the status it is now in.
 * statusLabel is Fineract's human text, e.g. "Submitted and pending approval".
 */
public record LoanStatusResponse(Long loanId, @Nullable String accountNo, LoanStatus status, @Nullable String statusLabel) {
}
