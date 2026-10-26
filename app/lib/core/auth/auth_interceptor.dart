import 'dart:async';

import 'package:dio/dio.dart';

import '../errors/api_error.dart';
import 'token_store.dart';

/// Asks the backend for a new token pair (POST /api/auth/refresh). Must use a Dio WITHOUT this
/// interceptor, or a failing refresh would try to refresh itself.
typedef RefreshTokens = Future<TokenPair> Function(String refreshToken);

/// Adds `Authorization: Bearer <access token>` and handles expiry.
///
/// Access tokens live 15 minutes. When one has expired, every request in flight gets
/// 401 UNAUTHENTICATED at about the same moment (the dashboard alone fires several). They must
/// NOT each refresh: refresh tokens are single-use and ROTATED, and the backend treats a reused
/// refresh token as theft and revokes the whole family (Phase 3, reuse detection). So:
///
/// 1. the first 401 starts ONE refresh; the others await the same Future ([_refreshing]);
/// 2. a request that was sent with an old token, after someone else already refreshed, just
///    retries with the new token;
/// 3. each request is retried at most once ([_retriedKey]);
/// 4. if the refresh itself is refused (INVALID_REFRESH_TOKEN), tokens are cleared and
///    [onSessionExpired] sends the user to the login screen. A network error during the refresh
///    does NOT log the user out: it is reported like any other network error.
class AuthInterceptor extends Interceptor {
  AuthInterceptor({
    required TokenStore tokens,
    required RefreshTokens refresh,
    required Dio retryClient,
    required void Function() onSessionExpired,
  })  : _tokens = tokens,
        _refresh = refresh,
        _retryClient = retryClient,
        _onSessionExpired = onSessionExpired;

  /// Set on requests that must go out without a token (login, refresh).
  static const skipAuthKey = 'adey.skipAuth';
  static const _retriedKey = 'adey.retried';
  static const _sentTokenKey = 'adey.sentToken';

  final TokenStore _tokens;
  final RefreshTokens _refresh;
  final Dio _retryClient;
  final void Function() _onSessionExpired;

  Future<String?>? _refreshing;

  @override
  Future<void> onRequest(RequestOptions options, RequestInterceptorHandler handler) async {
    if (options.extra[skipAuthKey] != true) {
      final token = await _tokens.accessToken();
      if (token != null) {
        options.headers['Authorization'] = 'Bearer $token';
        options.extra[_sentTokenKey] = token;
      }
    }
    handler.next(options);
  }

  @override
  Future<void> onError(DioException err, ErrorInterceptorHandler handler) async {
    final options = err.requestOptions;
    if (!_isExpiredAccessToken(err) || options.extra[skipAuthKey] == true || options.extra[_retriedKey] == true) {
      handler.next(err);
      return;
    }
    final String? newToken;
    try {
      newToken = await _freshToken(options.extra[_sentTokenKey] as String?);
    } on DioException catch (refreshError) {
      // Couldn't reach the backend to refresh: report THAT (network), keep the session.
      handler.next(refreshError);
      return;
    }
    if (newToken == null) {
      handler.next(err); // session over; onSessionExpired already called
      return;
    }
    try {
      options.headers['Authorization'] = 'Bearer $newToken';
      options.extra[_retriedKey] = true;
      options.extra[_sentTokenKey] = newToken;
      handler.resolve(await _retryClient.fetch<dynamic>(options));
    } on DioException catch (retryError) {
      handler.next(retryError);
    }
  }

  /// Only the backend's own "token expired/invalid" answer triggers a refresh, not every 401:
  /// e.g. GitHub's tunnel answers 401 for a private Codespace port, and refreshing won't help.
  static bool _isExpiredAccessToken(DioException err) {
    if (err.response?.statusCode != 401) return false;
    return ApiError.from(err).code == 'UNAUTHENTICATED';
  }

  /// A valid access token after a 401, refreshing at most once for all concurrent callers.
  /// null = the session is over (refresh token refused or missing).
  Future<String?> _freshToken(String? sentToken) async {
    final current = await _tokens.accessToken();
    if (current != sentToken) {
      // While this request was on its way, another one already refreshed (new token: retry with
      // it) or ended the session (null: nothing more to do, the user is already being signed out).
      return current;
    }
    final running = _refreshing;
    if (running != null) return running;
    final refreshing = _doRefresh();
    _refreshing = refreshing;
    try {
      return await refreshing;
    } finally {
      _refreshing = null;
    }
  }

  Future<String?> _doRefresh() async {
    final refreshToken = await _tokens.refreshToken();
    if (refreshToken == null) {
      await _expire();
      return null;
    }
    try {
      final pair = await _refresh(refreshToken);
      await _tokens.save(pair);
      return pair.accessToken;
    } on DioException catch (e) {
      final status = e.response?.statusCode;
      if (status == 401 || status == 400) {
        await _expire(); // INVALID_REFRESH_TOKEN (expired, revoked or already used)
        return null;
      }
      rethrow; // network / 5xx: not the user's fault, keep the tokens
    }
  }

  Future<void> _expire() async {
    await _tokens.clear();
    _onSessionExpired();
  }
}
