package et.adey.mfi.common;

import java.util.List;

/**
 * Every stable error `code` the API can return, in one list. The app switches on these; the
 * OpenAPI spec publishes them as the enum of Problem.code (OpenApiConfig). ErrorCodeCatalogTest
 * checks that every code used in the source is listed here and documented in backend/README.md.
 */
public final class ErrorCodes {

    private ErrorCodes() {
    }

    public static final List<String> ALL = List.of(
            // 400
            "VALIDATION_FAILED", "MALFORMED_REQUEST", "INVALID_PARAMETER", "INVALID_REQUEST",
            "FINERACT_VALIDATION", "IDEMPOTENCY_KEY_INVALID", "RESOLUTION_NOTE_REQUIRED", "PHONE_METHOD_MISMATCH",
            // 401
            "UNAUTHENTICATED", "INVALID_CREDENTIALS", "INVALID_REFRESH_TOKEN",
            "WEBHOOK_SIGNATURE_INVALID", "WEBHOOK_TIMESTAMP_INVALID",
            // 403
            "FORBIDDEN", "NOT_AN_OFFICER",
            // 404, 405, 406, 415
            "NOT_FOUND", "METHOD_NOT_ALLOWED", "NOT_ACCEPTABLE", "UNSUPPORTED_MEDIA_TYPE",
            // 409
            "OPEN_LOAN_EXISTS", "DUPLICATE_CLIENT", "IDEMPOTENCY_IN_PROGRESS", "IDEMPOTENCY_OUTCOME_UNKNOWN",
            "CONFLICT", "FINDING_ALREADY_RESOLVED",
            // 422
            "LOAN_WRONG_STATE", "FINERACT_RULE", "IDEMPOTENCY_KEY_REUSED", "AMOUNT_EXCEEDS_OUTSTANDING",
            "LOAN_NOT_ACTIVE",
            // 429
            "TOO_MANY_LOGIN_ATTEMPTS",
            // 5xx
            "INTERNAL_ERROR", "FINERACT_ERROR", "FINERACT_UNAVAILABLE", "PROVIDER_UNAVAILABLE", "POSTING_PENDING");
}
