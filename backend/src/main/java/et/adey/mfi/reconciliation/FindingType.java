package et.adey.mfi.reconciliation;

/** What reconciliation found for one payment. */
public enum FindingType {
    /** Paid at the provider AND posted in Fineract, same amount. */
    MATCHED,
    /** Paid at the provider, but no Fineract repayment with that receipt (e.g. webhook lost). */
    PAID_NOT_POSTED,
    /** A Mobile Money repayment in Fineract that the provider has no payment for. */
    POSTED_NOT_PAID,
    /** The amounts differ between provider, our intent and/or Fineract. */
    AMOUNT_MISMATCH
}
