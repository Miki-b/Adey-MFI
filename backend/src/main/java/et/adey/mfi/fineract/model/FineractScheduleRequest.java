package et.adey.mfi.fineract.model;

import java.math.BigDecimal;

/**
 * Body for POST /loans?command=calculateLoanSchedule (and later, loan submission).
 * Every time quantity is a number + unit-code pair; dates are text + dateFormat + locale.
 */
public record FineractScheduleRequest(
        Long clientId,
        Long productId,
        BigDecimal principal,
        Integer loanTermFrequency,
        Integer loanTermFrequencyType,
        Integer numberOfRepayments,
        Integer repaymentEvery,
        Integer repaymentFrequencyType,
        BigDecimal interestRatePerPeriod,
        Integer amortizationType,
        Integer interestType,
        Integer interestCalculationPeriodType,
        String transactionProcessingStrategyCode,
        String expectedDisbursementDate,
        String submittedOnDate,
        String loanType,
        String dateFormat,
        String locale) {
}
