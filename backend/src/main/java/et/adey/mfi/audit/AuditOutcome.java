package et.adey.mfi.audit;

/**
 * SUCCESS  - done.
 * DENIED   - the user may not do this (wrong role or someone else's client): HTTP 403.
 * REJECTED - allowed, but refused by a business rule or by Fineract (409 / 422 / 400 / 404).
 * ERROR    - technical failure (Fineract down, unexpected error).
 */
public enum AuditOutcome {
    SUCCESS, DENIED, REJECTED, ERROR
}
