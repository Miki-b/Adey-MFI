import 'package:uuid/uuid.dart';

import '../errors/api_error.dart';

/// IDEMPOTENCY KEYS for one user action (one repayment form, one "create client" form...).
///
/// The problem: the officer taps "Record repayment", the request reaches the backend, Fineract
/// books it, but the answer is lost (bad network in the field). The officer taps again. Without
/// protection the client pays twice. The backend (Phase 4) stores the result per
/// `Idempotency-Key`: the same key again returns the stored result instead of booking again.
///
/// So: create the key when the user CONFIRMS, and send the SAME key on every retry of that same
/// request. Rules (backend/README.md "Safe retries"):
/// - success                        -> done; the next submission gets a new key ([run] resets)
/// - network error, timeout, 5xx,
///   409 IDEMPOTENCY_IN_PROGRESS    -> outcome unknown or still running: KEEP the key for the retry
/// - 400/403/409/422 refusals       -> the backend released the key; keeping it is harmless
/// - IDEMPOTENCY_OUTCOME_UNKNOWN,
///   IDEMPOTENCY_KEY_REUSED         -> the backend won't accept this key again: forget it
/// - the request changed (user edited the amount) -> new key, it is a different action
class IdempotentAction {
  IdempotentAction({String Function()? newKey}) : _newKey = newKey ?? _uuid.v4;

  static const _uuid = Uuid();
  final String Function() _newKey;

  String? _key;
  String? _fingerprint;

  /// The key in use (null before the first [keyFor] / after success).
  String? get currentKey => _key;

  /// Key for this request. [fingerprint] identifies the request's content (e.g. its JSON):
  /// the same content reuses the key, different content starts a new action.
  String keyFor(String fingerprint) {
    if (_key == null || _fingerprint != fingerprint) {
      _key = _newKey();
      _fingerprint = fingerprint;
    }
    return _key!;
  }

  /// Runs [send] with the right key and applies the rules above. Rethrows failures as [ApiError].
  Future<T> run<T>(String fingerprint, Future<T> Function(String idempotencyKey) send) async {
    final key = keyFor(fingerprint);
    try {
      final result = await send(key);
      reset();
      return result;
    } catch (error) {
      final e = ApiError.from(error);
      if (e.code == 'IDEMPOTENCY_OUTCOME_UNKNOWN' || e.code == 'IDEMPOTENCY_KEY_REUSED') {
        reset();
      }
      throw e;
    }
  }

  /// Forget the key: the next submission is a new action.
  void reset() {
    _key = null;
    _fingerprint = null;
  }
}
