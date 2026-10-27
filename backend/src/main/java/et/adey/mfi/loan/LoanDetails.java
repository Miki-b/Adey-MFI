package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

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
        @Nullable Long loanOfficerId,
        @Nullable String loanOfficerName,
        LoanStatus status,
        @Nullable String statusLabel,
        @Nullable String currency,
        BigDecimal principal,
        @Nullable BigDecimal approvedPrincipal,
        @Nullable BigDecimal annualInterestRate,
        int numberOfRepayments,
        @Nullable Dates dates,
        Summary summary,
        List<Installment> schedule,
        List<LoanTransactionResponse> transactions) {

    public record Dates(
            LocalDate submittedOn,
            @Nullable LocalDate approvedOn,
            @Nullable LocalDate rejectedOn,
            @Nullable LocalDate expectedDisbursement,
            @Nullable LocalDate disbursedOn,
            @Nullable LocalDate expectedMaturity,
            @Nullable LocalDate closedOn) {
    }

    /** Totals. Before disbursement everything outstanding is 0.00. */
    @Schema(name = "LoanTotals") // a unique schema name: ReconciliationReport has a Summary too
    public record Summary(
            BigDecimal principalDisbursed,
            BigDecimal interestCharged,
            BigDecimal principalPaid,
            BigDecimal interestPaid,
            BigDecimal principalOutstanding,
            BigDecimal interestOutstanding,
            BigDecimal totalOutstanding,
            BigDecimal totalOverdue,
            @Nullable LocalDate overdueSince,
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
