// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'api_providers.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(tokenStore)
final tokenStoreProvider = TokenStoreProvider._();

final class TokenStoreProvider
    extends $FunctionalProvider<TokenStore, TokenStore, TokenStore>
    with $Provider<TokenStore> {
  TokenStoreProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'tokenStoreProvider',
        isAutoDispose: false,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$tokenStoreHash();

  @$internal
  @override
  $ProviderElement<TokenStore> $createElement($ProviderPointer pointer) =>
      $ProviderElement(pointer);

  @override
  TokenStore create(Ref ref) {
    return tokenStore(ref);
  }

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(TokenStore value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<TokenStore>(value),
    );
  }
}

String _$tokenStoreHash() => r'e1af499041ec8e3fd653ab4576cba8185f657d98';

/// Dio WITHOUT the auth interceptor: used for refresh and for the one retry after a refresh.

@ProviderFor(bareDio)
final bareDioProvider = BareDioProvider._();

/// Dio WITHOUT the auth interceptor: used for refresh and for the one retry after a refresh.

final class BareDioProvider extends $FunctionalProvider<Dio, Dio, Dio>
    with $Provider<Dio> {
  /// Dio WITHOUT the auth interceptor: used for refresh and for the one retry after a refresh.
  BareDioProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'bareDioProvider',
        isAutoDispose: false,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$bareDioHash();

  @$internal
  @override
  $ProviderElement<Dio> $createElement($ProviderPointer pointer) =>
      $ProviderElement(pointer);

  @override
  Dio create(Ref ref) {
    return bareDio(ref);
  }

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(Dio value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<Dio>(value),
    );
  }
}

String _$bareDioHash() => r'5acc4abed28f7f00b9e41a9f2fc259d42e09755b';

/// The Dio every repository uses: bearer token + refresh-once-on-401.

@ProviderFor(dio)
final dioProvider = DioProvider._();

/// The Dio every repository uses: bearer token + refresh-once-on-401.

final class DioProvider extends $FunctionalProvider<Dio, Dio, Dio>
    with $Provider<Dio> {
  /// The Dio every repository uses: bearer token + refresh-once-on-401.
  DioProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'dioProvider',
        isAutoDispose: false,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$dioHash();

  @$internal
  @override
  $ProviderElement<Dio> $createElement($ProviderPointer pointer) =>
      $ProviderElement(pointer);

  @override
  Dio create(Ref ref) {
    return dio(ref);
  }

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(Dio value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<Dio>(value),
    );
  }
}

String _$dioHash() => r'14834f67dcfba354bbcdfcfa3f28f9962ab9ccd4';

/// The generated client, on our Dio (its own auth interceptors are not installed).

@ProviderFor(adeyApi)
final adeyApiProvider = AdeyApiProvider._();

/// The generated client, on our Dio (its own auth interceptors are not installed).

final class AdeyApiProvider
    extends $FunctionalProvider<AdeyApi, AdeyApi, AdeyApi>
    with $Provider<AdeyApi> {
  /// The generated client, on our Dio (its own auth interceptors are not installed).
  AdeyApiProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'adeyApiProvider',
        isAutoDispose: false,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$adeyApiHash();

  @$internal
  @override
  $ProviderElement<AdeyApi> $createElement($ProviderPointer pointer) =>
      $ProviderElement(pointer);

  @override
  AdeyApi create(Ref ref) {
    return adeyApi(ref);
  }

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(AdeyApi value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<AdeyApi>(value),
    );
  }
}

String _$adeyApiHash() => r'29d1c9bce6e2c6640f2cc98965212526bec9aaac';
