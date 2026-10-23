package et.adey.mfi.audit;

/** State-changing actions we record. */
public enum AuditAction {
    LOAN_APPLY,
    LOAN_APPROVE,
    LOAN_UNDO_APPROVAL,
    LOAN_REJECT,
    LOAN_DISBURSE,
    LOAN_REPAY
}
