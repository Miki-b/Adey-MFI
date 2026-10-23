package et.adey.mfi.fineract.model;

import java.math.BigDecimal;

/** GET /loans/{id}?associations=repaymentSchedule (only the fields we use). */
public record FineractLoan(
        Long id,
        String accountNo,
        Long clientId,
        Long loanProductId,
        String loanProductName,
        Status status,
        FineractCurrency currency,
        BigDecimal principal,
        BigDecimal approvedPrincipal,
        BigDecimal annualInterestRate,
        Integer numberOfRepayments,
        Summary summary,
        FineractLoanSchedule repaymentSchedule) {

    /** Loan status: id 100 = submitted, 200 = approved, 300 = active, 600 = closed, ... */
    public record Status(Integer id, String code, String value, Boolean active) {
    }

    /** Running totals. Absent until the loan is disbursed. */
    public record Summary(
            BigDecimal principalOutstanding,
            BigDecimal totalOutstanding,
            BigDecimal totalOverdue,
            Boolean inArrears) {
    }
}
