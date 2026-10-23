package et.adey.mfi.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Everything about one loan: terms, key dates, totals, full schedule and transactions. */
public record LoanDetails(
        Long loanId,
        String accountNo,
        Long clientId,
        String clientName,
        Long productId,
        String productName,
        Long loanOfficerId,
        String loanOfficerName,
        LoanStatus status,
        String statusLabel,
        String currency,
        BigDecimal principal,
        BigDecimal approvedPrincipal,
        BigDecimal annualInterestRate,
        int numberOfRepayments,
        Dates dates,
        Summary summary,
        List<Installment> schedule,
        List<LoanTransactionResponse> transactions) {

    public record Dates(
            LocalDate submittedOn,
            LocalDate approvedOn,
            LocalDate rejectedOn,
            LocalDate expectedDisbursement,
            LocalDate disbursedOn,
            LocalDate expectedMaturity,
            LocalDate closedOn) {
    }

    /** Totals. Before disbursement everything outstanding is 0.00. */
    public record Summary(
            BigDecimal principalDisbursed,
            BigDecimal interestCharged,
            BigDecimal principalPaid,
            BigDecimal interestPaid,
            BigDecimal principalOutstanding,
            BigDecimal interestOutstanding,
            BigDecimal totalOutstanding,
            BigDecimal totalOverdue,
            LocalDate overdueSince,
            boolean inArrears) {
    }

    /** One installment of the actual loan, with what has been paid against it. */
    public record Installment(
            int number,
            LocalDate dueDate,
            BigDecimal principal,
            BigDecimal interest,
            BigDecimal fees,
            BigDecimal penalties,
            BigDecimal total,
            BigDecimal paid,
            BigDecimal outstanding,
            BigDecimal balanceAfter,
            boolean complete) {
    }
}
