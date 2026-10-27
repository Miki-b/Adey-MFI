package et.adey.mfi.portfolio;

import org.jspecify.annotations.Nullable;

import et.adey.mfi.loan.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of GET /api/loans. principalOutstanding / totalOverdue / daysOverdue / nextDue are only
 * meaningful for ACTIVE loans (0.00 / 0 / null otherwise). nextDueAmount is what is still owed on
 * the oldest unpaid installment; for a loan in arrears that installment is already past due.
 */
public record LoanListItem(
        Long loanId,
        String accountNo,
        Long clientId,
        String clientName,
        @Nullable String clientMobileNo,
        String productName,
        @Nullable String loanOfficerName,
        LoanStatus status,
        @Nullable String statusLabel,
        BigDecimal principal,
        BigDecimal principalOutstanding,
        BigDecimal totalOverdue,
        long daysOverdue,
        boolean inArrears,
        @Nullable LocalDate nextDueDate,
        @Nullable BigDecimal nextDueAmount,
        @Nullable LocalDate submittedOn) {
}
