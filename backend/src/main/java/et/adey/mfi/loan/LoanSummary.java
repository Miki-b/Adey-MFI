package et.adey.mfi.loan;

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
        BigDecimal principal,
        BigDecimal annualInterestRate,
        int installmentsTotal,
        int installmentsPaid,
        BigDecimal principalOutstanding,
        BigDecimal totalOutstanding,
        LocalDate nextDueDate,
        BigDecimal nextDueAmount,
        boolean inArrears) {
}
