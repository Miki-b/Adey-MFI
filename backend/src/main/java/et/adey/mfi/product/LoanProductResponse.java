package et.adey.mfi.product;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;

/** A loan product with its allowed ranges. Amounts in the product currency (ETB), 2 decimals. */
public record LoanProductResponse(
        Long id,
        String name,
        @Nullable String shortName,
        @Nullable String currency,
        @Nullable BigDecimal principalMin,
        @Nullable BigDecimal principalDefault,
        @Nullable BigDecimal principalMax,
        @Nullable Integer repaymentsMin,
        @Nullable Integer repaymentsDefault,
        @Nullable Integer repaymentsMax,
        @Nullable Integer repaymentEvery,
        @Nullable RepaymentFrequency repaymentFrequency,
        @Nullable BigDecimal annualInterestRate,
        @Nullable InterestMethod interestMethod,
        @Nullable Amortization amortization) {
}
