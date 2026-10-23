package et.adey.mfi.portfolio;

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
        String clientMobileNo,
        String productName,
        String loanOfficerName,
        LoanStatus status,
        String statusLabel,
        BigDecimal principal,
        BigDecimal principalOutstanding,
        BigDecimal totalOverdue,
        long daysOverdue,
        boolean inArrears,
        LocalDate nextDueDate,
        BigDecimal nextDueAmount,
        LocalDate submittedOn) {
}
