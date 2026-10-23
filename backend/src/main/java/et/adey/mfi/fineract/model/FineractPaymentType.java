package et.adey.mfi.fineract.model;

/**
 * GET /paymenttypes item. isSystemDefined = created by Fineract itself for internal
 * adjustments (chargeback/refund); not something a loan officer should choose.
 */
public record FineractPaymentType(
        Long id,
        String name,
        String description,
        Boolean isCashPayment,
        Integer position,
        Boolean isSystemDefined) {
}
