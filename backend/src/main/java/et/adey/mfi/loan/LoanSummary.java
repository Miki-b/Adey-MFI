package et.adey.mfi.loan;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One loan in a client's loan list.
 * nextDueDate / nextDueAmount are only set for ACTIVE loans (the first unpaid installment);
 * nextDueAmount is what is still owed on that installment.
 */
public record LoanSummary(
        Long loanId,
        String accountNo,
        String productName,
        LoanStatus status,
        @Nullable String statusLabel,
        BigDecimal principal,
        @Nullable BigDecimal annualInterestRate,
        int installmentsTotal,
        int installmentsPaid,
        BigDecimal principalOutstanding,
        BigDecimal totalOutstanding,
        @Nullable LocalDate nextDueDate,
        @Nullable BigDecimal nextDueAmount,
        boolean inArrears) {
}
