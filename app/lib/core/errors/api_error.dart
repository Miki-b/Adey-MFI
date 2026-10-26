import 'package:dio/dio.dart';

/// Kinds of failure the UI distinguishes. [backend] carries one of the backend's stable `code`s.
enum ApiErrorKind {
  /// The backend answered with an RFC 7807 problem and a `code` (see backend/README.md "Errors").
  backend,

  /// No answer: DNS, connection refused, timeout, no network.
  network,

  /// The Codespace port is private: GitHub's tunnel answered instead of our backend.
  serverPrivate,

  /// An answer that isn't ours (HTML page, unexpected JSON): wrong server URL?
  unexpectedResponse,

  /// Anything else (a bug).
  unknown,
}

/// One field-level problem from the backend's `errors[]`.
class FieldError {
  const FieldError(this.field, this.message);

  final String field;
  final String message;
}

/// Every failure the app shows, in one shape. Build it with [ApiError.from] from whatever was
/// thrown; the UI then turns it into text with `errorMessage` (error_messages.dart).
class ApiError implements Exception {
  const ApiError({
    required this.kind,
    this.code,
    this.status,
    this.detail,
    this.fieldErrors = const [],
    this.retryAfter,
  });

  final ApiErrorKind kind;

  /// Stable backend code, e.g. `INVALID_CREDENTIALS`. Switch on this, never on [detail].
  final String? code;
  final int? status;

  /// Human text from the backend (may change between versions; shown only as a fallback).
  final String? detail;
  final List<FieldError> fieldErrors;

  /// From the `Retry-After` header (429 login lockout).
  final Duration? retryAfter;

  bool get isNetwork => kind == ApiErrorKind.network;

  static ApiError from(Object error) {
    if (error is ApiError) return error;
    if (error is DioException) return _fromDio(error);
    return ApiError(kind: ApiErrorKind.unknown, detail: error.toString());
  }

  static ApiError _fromDio(DioException e) {
    // Our own interceptors may already have produced an ApiError.
    final inner = e.error;
    if (inner is ApiError) return inner;

    switch (e.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.transformTimeout:
      case DioExceptionType.connectionError:
        return ApiError(kind: ApiErrorKind.network, detail: e.message);
      case DioExceptionType.cancel:
      case DioExceptionType.badCertificate:
      case DioExceptionType.unknown:
        // Socket errors on some platforms arrive as "unknown" with no response.
        if (e.response == null) return ApiError(kind: ApiErrorKind.network, detail: e.message);
      case DioExceptionType.badResponse:
        break;
    }

    final response = e.response;
    if (response == null) return ApiError(kind: ApiErrorKind.unknown, detail: e.message);
    final status = response.statusCode;

    // A private Codespace port: GitHub answers 401 "WWW-Authenticate: tunnel" (API calls) or
    // redirects to its sign-in page (we don't follow redirects, so that's a 302 here).
    final wwwAuth = response.headers.value('www-authenticate') ?? '';
    final location = response.headers.value('location') ?? '';
    if ((status == 401 && wwwAuth.toLowerCase().contains('tunnel')) ||
        ((status == 302 || status == 307) && location.contains('github.dev'))) {
      return ApiError(kind: ApiErrorKind.serverPrivate, status: status);
    }

    final body = response.data;
    if (body is Map && body['code'] is String) {
      final errors = body['errors'];
      return ApiError(
        kind: ApiErrorKind.backend,
        code: body['code'] as String,
        status: status,
        detail: body['detail'] is String ? body['detail'] as String : null,
        fieldErrors: errors is List
            ? [
                for (final f in errors)
                  if (f is Map) FieldError('${f['field'] ?? ''}', '${f['message'] ?? ''}'),
              ]
            : const [],
        retryAfter: _retryAfter(response.headers.value('retry-after')),
      );
    }
    return ApiError(kind: ApiErrorKind.unexpectedResponse, status: status);
  }

  static Duration? _retryAfter(String? header) {
    final seconds = int.tryParse(header?.trim() ?? '');
    return seconds == null ? null : Duration(seconds: seconds);
  }

  @override
  String toString() => 'ApiError($kind, code: $code, status: $status, detail: $detail)';
}
