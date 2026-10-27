import 'package:adey_api/adey_api.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../api/api_providers.dart';
import '../errors/api_error.dart';
import 'auth_interceptor.dart';
import 'officer.dart';
import 'token_store.dart';

part 'auth_controller.g.dart';

/// Who is signed in. `AsyncData(null)` = signed out, `AsyncData(officer)` = signed in,
/// loading = restoring the session at app start, error = couldn't check (e.g. no network);
/// the router turns these into the login screen, the app, or the splash screen.
@Riverpod(keepAlive: true)
class AuthController extends _$AuthController {
  /// Calls that must go out without our bearer token (and must never trigger a refresh).
  static const _noAuth = {AuthInterceptor.skipAuthKey: true};

  @override
  Future<Officer?> build() async {
    // App start: a stored refresh token means "maybe still signed in". /me tells for sure;
    // if the access token expired, the interceptor refreshes it on the way.
    final tokens = ref.watch(tokenStoreProvider);
    if (await tokens.refreshToken() == null) return null;
    try {
      return await _loadMe();
    } on ApiError catch (e) {
      if (e.code == 'UNAUTHENTICATED' || e.code == 'INVALID_REFRESH_TOKEN') {
        await tokens.clear();
        return null;
      }
      rethrow; // network etc.: keep the tokens, the splash screen offers "try again"
    }
  }

  /// Throws [ApiError] (wrong password, lockout, not an officer, network...).
  Future<void> login(String username, String password) async {
    final api = ref.read(adeyApiProvider).getAuthApi();
    try {
      final response = await api.login(
        loginRequest: LoginRequest(username: username.trim(), password: password),
        extra: _noAuth,
      );
      final body = response.data!;
      await ref.read(tokenStoreProvider).save(TokenPair(accessToken: body.accessToken, refreshToken: body.refreshToken));
      ref.read(sessionNoticeProvider.notifier).clear();
      // The login answer already contains the officer, but /me is the source of truth for the
      // signed-in user (and proves the new token works end to end).
      state = AsyncData(await _loadMe());
    } catch (e) {
      throw ApiError.from(e);
    }
  }

  Future<void> logout() async {
    final tokens = ref.read(tokenStoreProvider);
    final refreshToken = await tokens.refreshToken();
    if (refreshToken != null) {
      try {
        // Revokes the refresh token (and its family) on the server. If this fails (offline), the
        // token still expires within 7 days; locally we sign out regardless.
        await ref.read(adeyApiProvider).getAuthApi().logout(
              refreshRequest: RefreshRequest(refreshToken: refreshToken),
              extra: _noAuth,
            );
      } catch (_) {}
    }
    await tokens.clear();
    state = const AsyncData(null);
  }

  /// Called by the interceptor when the refresh token was refused: back to the login screen.
  void sessionExpired() {
    ref.read(sessionNoticeProvider.notifier).expired();
    state = const AsyncData(null);
  }

  /// "Try again" on the splash screen.
  void retry() => ref.invalidateSelf();

  Future<Officer> _loadMe() async {
    try {
      final me = await ref.read(adeyApiProvider).getAuthApi().getCurrentOfficer();
      return Officer.fromApi(me.data!);
    } catch (e) {
      throw ApiError.from(e);
    }
  }
}

/// A one-time message for the login screen ("your session has ended").
@Riverpod(keepAlive: true)
class SessionNotice extends _$SessionNotice {
  @override
  bool build() => false;

  void expired() => state = true;

  void clear() => state = false;
}
