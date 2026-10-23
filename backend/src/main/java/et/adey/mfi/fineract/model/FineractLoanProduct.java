package et.adey.mfi.fineract.model;

import java.math.BigDecimal;

/**
 * GET /loanproducts and GET /loanproducts/{id}. Field names are Fineract's.
 * Note: "principal" and "numberOfRepayments" are the product DEFAULTS.
 */
public record FineractLoanProduct(
        Long id,
        String name,
        String shortName,
        FineractCurrency currency,
        BigDecimal principal,
        BigDecimal minPrincipal,
        BigDecimal maxPrincipal,
        Integer numberOfRepayments,
        Integer minNumberOfRepayments,
        Integer maxNumberOfRepayments,
        Integer repaymentEvery,
        FineractCodeValue repaymentFrequencyType,
        BigDecimal interestRatePerPeriod,
        FineractCodeValue interestRateFrequencyType,
        BigDecimal annualInterestRate,
        FineractCodeValue amortizationType,
        FineractCodeValue interestType,
        FineractCodeValue interestCalculationPeriodType,
        String transactionProcessingStrategyCode) {
}
