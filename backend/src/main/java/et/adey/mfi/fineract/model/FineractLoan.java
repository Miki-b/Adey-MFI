package et.adey.mfi.fineract.model;

import java.math.BigDecimal;
import java.util.List;

/** GET /loans/{id}?associations=repaymentSchedule[,transactions] (only the fields we use). */
public record FineractLoan(
        Long id,
        String accountNo,
        Long clientId,
        String clientName,
        Long loanProductId,
        String loanProductName,
        Long loanOfficerId,
        String loanOfficerName,
        Status status,
        FineractCurrency currency,
        BigDecimal principal,
        BigDecimal approvedPrincipal,
        BigDecimal annualInterestRate,
        Integer numberOfRepayments,
        /* Top level in this Fineract build (the OpenAPI schema puts it in summary, where it is null). */
        Boolean inArrears,
        Timeline timeline,
        Summary summary,
        FineractLoanSchedule repaymentSchedule,
        List<FineractLoanTransaction> transactions) {

    /** In arrears = an installment is past due and not fully paid. Reads both places Fineract may use. */
    public boolean isInArrears() {
        return Boolean.TRUE.equals(inArrears) || (summary != null && Boolean.TRUE.equals(summary.inArrears()));
    }

    /** Loan status: id 100 = submitted, 200 = approved, 300 = active, 600 = closed, ... */
    public record Status(Integer id, String code, String value, Boolean active) {
    }

    /** Key dates in the loan's life, as [y, m, d] arrays. Missing until that step happens. */
    public record Timeline(
            List<Integer> submittedOnDate,
            List<Integer> approvedOnDate,
            List<Integer> rejectedOnDate,
            List<Integer> expectedDisbursementDate,
            List<Integer> actualDisbursementDate,
            List<Integer> expectedMaturityDate,
            List<Integer> closedOnDate) {
    }

    /** Running totals. Absent until the loan is disbursed. */
    public record Summary(
            BigDecimal principalDisbursed,
            BigDecimal principalPaid,
            BigDecimal principalOutstanding,
            BigDecimal interestCharged,
            BigDecimal interestPaid,
            BigDecimal interestOutstanding,
            BigDecimal feeChargesOutstanding,
            BigDecimal penaltyChargesOutstanding,
            BigDecimal totalExpectedRepayment,
            BigDecimal totalRepayment,
            BigDecimal totalOutstanding,
            BigDecimal totalOverdue,
            List<Integer> overdueSinceDate,
            Boolean inArrears) {
    }
}
