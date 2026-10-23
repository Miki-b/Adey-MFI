package et.adey.mfi.product;

import java.math.BigDecimal;

/** A loan product with its allowed ranges. Amounts in the product currency (ETB), 2 decimals. */
public record LoanProductResponse(
        Long id,
        String name,
        String shortName,
        String currency,
        BigDecimal principalMin,
        BigDecimal principalDefault,
        BigDecimal principalMax,
        Integer repaymentsMin,
        Integer repaymentsDefault,
        Integer repaymentsMax,
        Integer repaymentEvery,
        RepaymentFrequency repaymentFrequency,
        BigDecimal annualInterestRate,
        InterestMethod interestMethod,
        Amortization amortization) {
}
