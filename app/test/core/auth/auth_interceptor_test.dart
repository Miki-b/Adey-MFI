import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';

import 'package:adey_field/core/auth/auth_interceptor.dart';
import 'package:adey_field/core/auth/token_store.dart';
import 'package:adey_field/core/errors/api_error.dart';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';

/// A fake backend: accepts only `Bearer <validToken>`, otherwise 401 UNAUTHENTICATED.
class FakeBackend implements HttpClientAdapter {
  String validToken = 'new-access';
  final List<String?> seenAuthHeaders = [];

  /// Override to answer differently (e.g. GitHub's tunnel 401).
  ResponseBody Function(RequestOptions options)? custom;

  @override
  Future<ResponseBody> fetch(RequestOptions options, Stream<Uint8List>? requestStream, Future<void>? cancelFuture) async {
    // let requests overlap; /slow answers late
    await Future<void>.delayed(Duration(milliseconds: options.path == '/slow' ? 120 : 5));
    final auth = options.headers['Authorization'] as String?;
    seenAuthHeaders.add(auth);
    if (custom != null) return custom!(options);
    if (auth == 'Bearer $validToken') return _json(200, {'path': options.path});
    return _json(401, {'status': 401, 'code': 'UNAUTHENTICATED', 'detail': 'Access token expired'});
  }

  static ResponseBody _json(int status, Map<String, Object?> body, {Map<String, List<String>> headers = const {}}) =>
      ResponseBody.fromString(jsonEncode(body), status, headers: {
        Headers.contentTypeHeader: [Headers.jsonContentType],
        ...headers,
      });

  @override
  void close({bool force = false}) {}
}

void main() {
  late FakeBackend backend;
  late MemoryTokenStore tokens;
  late int refreshCalls;
  late int expiredCalls;
  late Completer<void> refreshGate;
  late Future<TokenPair> Function(String) refreshBehaviour;

  Dio newDio({bool withInterceptor = true}) {
    final dio = Dio(BaseOptions(baseUrl: 'https://backend.test'))..httpClientAdapter = backend;
    if (withInterceptor) {
      dio.interceptors.add(
        AuthInterceptor(
          tokens: tokens,
          retryClient: Dio(BaseOptions(baseUrl: 'https://backend.test'))..httpClientAdapter = backend,
          refresh: (refreshToken) {
            refreshCalls++;
            return refreshBehaviour(refreshToken);
          },
          onSessionExpired: () => expiredCalls++,
        ),
      );
    }
    return dio;
  }

  setUp(() {
    backend = FakeBackend();
    tokens = MemoryTokenStore(const TokenPair(accessToken: 'old-access', refreshToken: 'refresh-1'));
    refreshCalls = 0;
    expiredCalls = 0;
    refreshGate = Completer<void>();
    refreshBehaviour = (refreshToken) async {
      await refreshGate.future; // hold the refresh open until every request has hit its 401
      expect(refreshToken, 'refresh-1');
      return const TokenPair(accessToken: 'new-access', refreshToken: 'refresh-2');
    };
  });

  test('adds the bearer token', () async {
    tokens = MemoryTokenStore(const TokenPair(accessToken: 'new-access', refreshToken: 'r'));
    final response = await newDio().get<Object>('/api/auth/me');
    expect(response.statusCode, 200);
    expect(backend.seenAuthHeaders, ['Bearer new-access']);
    expect(refreshCalls, 0);
  });

  test('concurrent 401s trigger exactly ONE refresh, and every request is retried and succeeds', () async {
    final dio = newDio();
    final requests = [
      dio.get<Object>('/api/me/portfolio'),
      dio.get<Object>('/api/clients'),
      dio.get<Object>('/api/auth/me'),
      dio.get<Object>('/api/loans'),
    ];
    // Let all four reach the backend and get their 401 before the refresh may finish.
    await Future<void>.delayed(const Duration(milliseconds: 50));
    refreshGate.complete();
    final responses = await Future.wait(requests);

    expect(refreshCalls, 1);
    expect(responses.map((r) => r.statusCode), everyElement(200));
    expect(await tokens.accessToken(), 'new-access');
    expect(await tokens.refreshToken(), 'refresh-2'); // rotated token stored
    expect(backend.seenAuthHeaders.where((h) => h == 'Bearer old-access'), hasLength(4));
    expect(backend.seenAuthHeaders.where((h) => h == 'Bearer new-access'), hasLength(4));
    expect(expiredCalls, 0);
  });

  test('a 401 that arrives after another request already refreshed reuses the new token (no 2nd refresh)', () async {
    refreshGate.complete();
    final dio = newDio();
    // Both leave with the old token; /first gets its 401 at once and refreshes, /slow's 401 comes
    // back after that. Refreshing again would send the already-rotated refresh-1 (= theft alarm).
    final results = await Future.wait([dio.get<Object>('/first'), dio.get<Object>('/slow')]);

    expect(results.map((r) => r.statusCode), [200, 200]);
    expect(refreshCalls, 1);
  });

  test('refused refresh token: tokens cleared, session expired once, original 401 returned', () async {
    refreshBehaviour = (_) async => throw DioException(
          requestOptions: RequestOptions(path: '/api/auth/refresh'),
          response: Response(
            requestOptions: RequestOptions(path: '/api/auth/refresh'),
            statusCode: 401,
            data: {'code': 'INVALID_REFRESH_TOKEN'},
          ),
          type: DioExceptionType.badResponse,
        );
    final dio = newDio();
    final results = await Future.wait([
      dio.get<Object>('/a').then<Object>((r) => r).catchError((Object e) => e),
      dio.get<Object>('/b').then<Object>((r) => r).catchError((Object e) => e),
    ]);

    expect(refreshCalls, 1);
    expect(expiredCalls, 1);
    expect(await tokens.accessToken(), isNull);
    expect(await tokens.refreshToken(), isNull);
    for (final r in results) {
      expect(ApiError.from(r).code, 'UNAUTHENTICATED');
    }
  });

  test('network failure during refresh keeps the session and reports a network error', () async {
    refreshBehaviour = (_) async => throw DioException(
          requestOptions: RequestOptions(path: '/api/auth/refresh'),
          type: DioExceptionType.connectionError,
        );
    final error = await newDio().get<Object>('/a').then<Object>((r) => r).catchError((Object e) => e);

    expect(ApiError.from(error).kind, ApiErrorKind.network);
    expect(expiredCalls, 0);
    expect(await tokens.refreshToken(), 'refresh-1');
  });

  test('a retried request that is still 401 is not retried again', () async {
    refreshGate.complete();
    backend.validToken = 'something-else'; // even the new token is refused
    final error = await newDio().get<Object>('/a').then<Object>((r) => r).catchError((Object e) => e);

    expect(ApiError.from(error).code, 'UNAUTHENTICATED');
    expect(refreshCalls, 1);
    expect(backend.seenAuthHeaders, ['Bearer old-access', 'Bearer new-access']);
  });

  test("GitHub's tunnel 401 (private Codespace port) never triggers a refresh", () async {
    backend.custom = (_) => ResponseBody.fromString('', 401, headers: {
          'www-authenticate': ['tunnel'],
        });
    final error = await newDio().get<Object>('/a').then<Object>((r) => r).catchError((Object e) => e);

    expect(ApiError.from(error).kind, ApiErrorKind.serverPrivate);
    expect(refreshCalls, 0);
  });

  test('skipAuth requests go out without a token and never refresh', () async {
    backend.custom = (_) => FakeBackend._json(401, {'code': 'INVALID_CREDENTIALS'});
    final error = await newDio()
        .post<Object>('/api/auth/login', options: Options(extra: {AuthInterceptor.skipAuthKey: true}))
        .then<Object>((r) => r)
        .catchError((Object e) => e);

    expect(backend.seenAuthHeaders, [null]);
    expect(ApiError.from(error).code, 'INVALID_CREDENTIALS');
    expect(refreshCalls, 0);
  });
}
