import 'package:adey_field/core/errors/api_error.dart';
import 'package:adey_field/core/errors/error_messages.dart';
import 'package:adey_field/l10n/app_localizations.dart';
import 'package:dio/dio.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';

DioException problem(int status, Map<String, Object?> body, {Map<String, List<String>> headers = const {}}) {
  final request = RequestOptions(path: '/api/x');
  return DioException(
    requestOptions: request,
    type: DioExceptionType.badResponse,
    response: Response(requestOptions: request, statusCode: status, data: body, headers: Headers.fromMap(headers)),
  );
}

void main() {
  final l10n = lookupAppLocalizations(const Locale('en'));

  group('ApiError.from', () {
    test('reads code, detail, field errors and Retry-After from a problem response', () {
      final e = ApiError.from(problem(
        429,
        {
          'code': 'TOO_MANY_LOGIN_ATTEMPTS',
          'detail': 'Too many attempts',
          'errors': [
            {'field': 'username', 'message': 'locked'},
          ],
        },
        headers: {
          'retry-after': ['610'],
        },
      ));
      expect(e.kind, ApiErrorKind.backend);
      expect(e.code, 'TOO_MANY_LOGIN_ATTEMPTS');
      expect(e.status, 429);
      expect(e.retryAfter, const Duration(seconds: 610));
      expect(e.fieldErrors.single.field, 'username');
    });

    test('timeouts and connection errors are network errors', () {
      for (final type in [DioExceptionType.connectionTimeout, DioExceptionType.receiveTimeout, DioExceptionType.connectionError]) {
        expect(ApiError.from(DioException(requestOptions: RequestOptions(), type: type)).kind, ApiErrorKind.network);
      }
    });

    test('private Codespace port: tunnel 401 or redirect to GitHub sign-in', () {
      final tunnel = problem(401, {}, headers: {
        'www-authenticate': ['tunnel'],
      });
      final redirect = problem(302, {}, headers: {
        'location': ['https://github.dev/pf-signin?port=8080'],
      });
      expect(ApiError.from(tunnel).kind, ApiErrorKind.serverPrivate);
      expect(ApiError.from(redirect).kind, ApiErrorKind.serverPrivate);
    });

    test('an answer without our code (HTML page, other server) is unexpected', () {
      final request = RequestOptions();
      final html = DioException(
        requestOptions: request,
        type: DioExceptionType.badResponse,
        response: Response(requestOptions: request, statusCode: 404, data: '<html>Not found</html>'),
      );
      expect(ApiError.from(html).kind, ApiErrorKind.unexpectedResponse);
    });
  });

  group('errorMessage', () {
    test('maps stable codes to friendly text', () {
      expect(errorMessage(l10n, problem(401, {'code': 'INVALID_CREDENTIALS'})), 'Wrong username or password.');
      expect(errorMessage(l10n, problem(409, {'code': 'OPEN_LOAN_EXISTS', 'detail': 'x'})), l10n.errorOpenLoanExists);
      expect(errorMessage(l10n, problem(503, {'code': 'FINERACT_UNAVAILABLE'})), l10n.errorFineractUnavailable);
      expect(errorMessage(l10n, problem(409, {'code': 'IDEMPOTENCY_OUTCOME_UNKNOWN'})), l10n.errorIdempotencyOutcomeUnknown);
    });

    test('lockout shows the wait from Retry-After, rounded UP to whole minutes', () {
      String lockout(String seconds) => errorMessage(
            l10n,
            problem(429, {'code': 'TOO_MANY_LOGIN_ATTEMPTS'}, headers: {
              'retry-after': [seconds],
            }),
          );
      expect(lockout('900'), 'Too many failed sign-ins. Try again in 15 minutes.');
      expect(lockout('61'), 'Too many failed sign-ins. Try again in 2 minutes.');
      expect(lockout('5'), 'Too many failed sign-ins. Try again in 1 minute.');
      expect(errorMessage(l10n, problem(429, {'code': 'TOO_MANY_LOGIN_ATTEMPTS'})), l10n.errorTooManyAttemptsNoTime);
    });

    test('unknown codes fall back to the backend detail, then to a generic text', () {
      expect(errorMessage(l10n, problem(422, {'code': 'SOMETHING_NEW', 'detail': 'Server says why'})), 'Server says why');
      expect(errorMessage(l10n, problem(422, {'code': 'SOMETHING_NEW'})), l10n.errorGeneric);
      expect(errorMessage(l10n, problem(400, {'code': 'VALIDATION_FAILED', 'detail': 'principal: too big'})), 'principal: too big');
    });

    test('non-backend failures', () {
      expect(errorMessage(l10n, DioException(requestOptions: RequestOptions(), type: DioExceptionType.connectionTimeout)),
          l10n.errorNetwork);
      expect(errorMessage(l10n, StateError('bug')), l10n.errorGeneric);
    });

    test('login: FORBIDDEN means "not an officer account"; elsewhere it is plain forbidden', () {
      final forbidden = problem(403, {'code': 'FORBIDDEN'});
      expect(loginErrorMessage(l10n, forbidden), l10n.errorNotOfficer);
      expect(errorMessage(l10n, forbidden), l10n.errorForbidden);
      expect(loginErrorMessage(l10n, problem(401, {'code': 'INVALID_CREDENTIALS'})), l10n.errorInvalidCredentials);
    });
  });
}
