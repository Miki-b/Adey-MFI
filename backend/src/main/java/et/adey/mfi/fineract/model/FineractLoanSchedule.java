package et.adey.mfi.fineract.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * A repayment schedule. Same shape from POST /loans?command=calculateLoanSchedule
 * and from the "repaymentSchedule" part of GET /loans/{id}.
 */
public record FineractLoanSchedule(
        FineractCurrency currency,
        BigDecimal totalPrincipalDisbursed,
        BigDecimal totalPrincipalExpected,
        BigDecimal totalInterestCharged,
        BigDecimal totalFeeChargesCharged,
        BigDecimal totalPenaltyChargesCharged,
        BigDecimal totalRepaymentExpected,
        List<Period> periods) {

    /**
     * One row. The first row is the disbursement itself: it has no "period" number,
     * only dueDate + principalDisbursed. Installment rows have period = 1, 2, 3, ...
     */
    public record Period(
            Integer period,
            List<Integer> fromDate,
            List<Integer> dueDate,
            BigDecimal principalDisbursed,
            BigDecimal principalDue,
            BigDecimal interestDue,
            BigDecimal feeChargesDue,
            BigDecimal penaltyChargesDue,
            BigDecimal totalDueForPeriod,
            BigDecimal totalPaidForPeriod,
            BigDecimal totalOutstandingForPeriod,
            BigDecimal principalLoanBalanceOutstanding,
            Boolean complete) {

        public boolean isInstallment() {
            return period != null;
        }
    }

    public List<Period> installments() {
        return periods == null ? List.of() : periods.stream().filter(Period::isInstallment).toList();
    }
}
