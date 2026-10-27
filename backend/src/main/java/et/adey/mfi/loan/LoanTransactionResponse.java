package et.adey.mfi.loan;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One money movement on a loan. For a repayment, the portions show how Fineract allocated
 * the amount: principalPortion + interestPortion + feesPortion + penaltiesPortion
 * (+ overpaymentPortion) = amount.
 * type is a stable code (REPAYMENT, DISBURSEMENT, ...); typeLabel is Fineract's text.
 */
public record LoanTransactionResponse(
        Long id,
        LocalDate date,
        String type,
        @Nullable String typeLabel,
        BigDecimal amount,
        BigDecimal principalPortion,
        BigDecimal interestPortion,
        BigDecimal feesPortion,
        BigDecimal penaltiesPortion,
        BigDecimal overpaymentPortion,
        BigDecimal outstandingBalanceAfter,
        @Nullable String paymentType,
        boolean reversed) {
}
