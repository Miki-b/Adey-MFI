package et.adey.mfi.fineract.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Loan terms in Fineract's request shape. The same body is used for
 * POST /loans?command=calculateLoanSchedule (preview) and POST /loans (submit an application).
 * Every time quantity is a number + unit-code pair; dates are text + dateFormat + locale.
 * NON_NULL: optional fields we don't set (e.g. loanOfficerId) are left out of the JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FineractLoanRequest(
        Long clientId,
        Long productId,
        Long loanOfficerId,
        String externalId,
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
