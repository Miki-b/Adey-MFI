package et.adey.mfi.fineract.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * One loan transaction (disbursement, repayment, ...), from GET /loans/{id}/transactions/{txId}
 * or the "transactions" association of GET /loans/{id}.
 * The *Portion fields say how the amount was allocated.
 */
public record FineractLoanTransaction(
        Long id,
        Type type,
        List<Integer> date,
        BigDecimal amount,
        BigDecimal principalPortion,
        BigDecimal interestPortion,
        BigDecimal feeChargesPortion,
        BigDecimal penaltyChargesPortion,
        BigDecimal overpaymentPortion,
        BigDecimal outstandingLoanBalance,
        Boolean manuallyReversed,
        PaymentDetail paymentDetailData) {

    /**
     * e.g. {"id": 2, "code": "loanTransactionType.repayment", "value": "Repayment", "accrual": false, ...}.
     * The accrual flags mark bookkeeping entries (interest recognised as income), not money movements.
     */
    public record Type(Integer id, String code, String value,
                       Boolean accrual, Boolean accrualActivity, Boolean accrualAdjustment) {

        public boolean isAccrualBookkeeping() {
            return Boolean.TRUE.equals(accrual) || Boolean.TRUE.equals(accrualActivity)
                    || Boolean.TRUE.equals(accrualAdjustment);
        }
    }

    public record PaymentDetail(FineractPaymentType paymentType, String receiptNumber) {
    }
}
