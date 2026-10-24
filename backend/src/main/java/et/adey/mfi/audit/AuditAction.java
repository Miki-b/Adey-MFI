package et.adey.mfi.audit;

/** State-changing actions we record. */
public enum AuditAction {
    LOGIN,
    CLIENT_CREATE,
    CLIENT_UPDATE,
    LOAN_APPLY,
    LOAN_APPROVE,
    LOAN_UNDO_APPROVAL,
    LOAN_REJECT,
    LOAN_DISBURSE,
    LOAN_REPAY,
    /** Officer asked the provider to collect from a client's phone. */
    MOBILE_PAYMENT_REQUEST,
    /** A provider webhook arrived: SUCCESS = applied, DENIED = bad signature/timestamp, REJECTED = mismatch/duplicate/unknown. */
    WEBHOOK,
    /** We asked the provider's verify API about a pending payment and acted on the answer (Chapa). */
    MOBILE_PAYMENT_VERIFY,
    MOBILE_PAYMENT_POSTED,
    MOBILE_PAYMENT_RETRY,
    MOBILE_PAYMENT_NEEDS_ATTENTION,
    RECONCILIATION_RUN,
    RECONCILIATION_RESOLVE
}
