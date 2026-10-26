import 'package:adey_api/adey_api.dart';
import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../auth/auth_controller.dart';
import '../auth/auth_interceptor.dart';
import '../auth/token_store.dart';
import '../config/server_config.dart';

part 'api_providers.g.dart';

/// Options shared by every Dio we create for the backend.
BaseOptions backendOptions(String baseUrl) => BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      // The dashboard does ~1.5 s of Fineract calls; the Codespace adds internet latency.
      receiveTimeout: const Duration(seconds: 30),
      headers: {'Accept': 'application/json'},
      // A private Codespace port redirects to GitHub's sign-in page; following it would give us an
      // HTML "200". Not following turns it into a recognisable error (ApiErrorKind.serverPrivate).
      followRedirects: false,
      validateStatus: (status) => status != null && status >= 200 && status < 300,
    );

@Riverpod(keepAlive: true)
TokenStore tokenStore(Ref ref) => SecureTokenStore();

/// Dio WITHOUT the auth interceptor: used for refresh and for the one retry after a refresh.
@Riverpod(keepAlive: true)
Dio bareDio(Ref ref) => Dio(backendOptions(ref.watch(serverUrlProvider)));

/// The Dio every repository uses: bearer token + refresh-once-on-401.
@Riverpod(keepAlive: true)
Dio dio(Ref ref) {
  final bare = ref.watch(bareDioProvider);
  final authApi = AuthApi(bare);
  final dio = Dio(backendOptions(ref.watch(serverUrlProvider)));
  dio.interceptors.add(
    AuthInterceptor(
      tokens: ref.watch(tokenStoreProvider),
      retryClient: bare,
      refresh: (refreshToken) async {
        final response = await authApi.refresh(refreshRequest: RefreshRequest(refreshToken: refreshToken));
        final body = response.data!;
        return TokenPair(accessToken: body.accessToken!, refreshToken: body.refreshToken!);
      },
      // Read lazily (at call time), so dio and the auth controller don't depend on each other at build time.
      onSessionExpired: () => ref.read(authControllerProvider.notifier).sessionExpired(),
    ),
  );
  return dio;
}

/// The generated client, on our Dio (its own auth interceptors are not installed).
@Riverpod(keepAlive: true)
AdeyApi adeyApi(Ref ref) => AdeyApi(dio: ref.watch(dioProvider), interceptors: const []);
