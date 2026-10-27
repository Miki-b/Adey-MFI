import '../../l10n/app_localizations.dart';
import 'api_error.dart';

/// THE error mapper: backend `code` -> localized text. One place, so every screen says the same
/// thing for the same problem. Codes without their own text fall back to the backend's `detail`
/// (written for people, English), then to a generic message.
String errorMessage(AppLocalizations l10n, Object error) {
  final e = ApiError.from(error);
  switch (e.kind) {
    case ApiErrorKind.network:
      return l10n.errorNetwork;
    case ApiErrorKind.serverPrivate:
      return l10n.errorServerPrivate;
    case ApiErrorKind.unexpectedResponse:
      return l10n.errorUnexpectedResponse;
    case ApiErrorKind.unknown:
      return l10n.errorGeneric;
    case ApiErrorKind.backend:
      break;
  }
  final specific = switch (e.code) {
    'INVALID_CREDENTIALS' => l10n.errorInvalidCredentials,
    'TOO_MANY_LOGIN_ATTEMPTS' => e.retryAfter == null
        ? l10n.errorTooManyAttemptsNoTime
        : l10n.errorTooManyAttempts(_roundUpMinutes(e.retryAfter!)),
    'UNAUTHENTICATED' || 'INVALID_REFRESH_TOKEN' => l10n.errorSessionExpired,
    'FORBIDDEN' => l10n.errorForbidden,
    'NOT_AN_OFFICER' => l10n.errorNotOfficer,
    'NOT_FOUND' => l10n.errorNotFound,
    'FINERACT_UNAVAILABLE' => l10n.errorFineractUnavailable,
    'FINERACT_ERROR' => l10n.errorFineractError,
    'INTERNAL_ERROR' => l10n.errorInternal,
    'PROVIDER_UNAVAILABLE' => l10n.errorProviderUnavailable,
    'IDEMPOTENCY_IN_PROGRESS' => l10n.errorIdempotencyInProgress,
    'IDEMPOTENCY_OUTCOME_UNKNOWN' => l10n.errorIdempotencyOutcomeUnknown,
    'IDEMPOTENCY_KEY_REUSED' => l10n.errorIdempotencyReused,
    'OPEN_LOAN_EXISTS' => l10n.errorOpenLoanExists,
    'DUPLICATE_CLIENT' => l10n.errorDuplicateClient,
    'LOAN_WRONG_STATE' => l10n.errorLoanWrongState,
    'LOAN_NOT_ACTIVE' => l10n.errorLoanNotActive,
    'AMOUNT_EXCEEDS_OUTSTANDING' => l10n.errorAmountExceedsOutstanding,
    'PHONE_METHOD_MISMATCH' => l10n.errorPhoneMethodMismatch,
    // Validation-type codes: the backend's detail names the field and the rule, so prefer it.
    'VALIDATION_FAILED' => e.detail ?? l10n.errorValidation,
    _ => null,
  };
  return specific ?? e.detail ?? l10n.errorGeneric;
}

/// Login: the backend answers NOT_AN_OFFICER for a valid user without our roles; older backends
/// sent a plain 403 FORBIDDEN, which on the login call means the same.
String loginErrorMessage(AppLocalizations l10n, Object error) {
  final e = ApiError.from(error);
  if (e.code == 'FORBIDDEN') return l10n.errorNotOfficer;
  return errorMessage(l10n, e);
}

/// 61 s -> 2 minutes: never tell people to come back before the lock is really over.
int _roundUpMinutes(Duration d) => (d.inSeconds / 60).ceil().clamp(1, 1 << 30);
