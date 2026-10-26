// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'portfolio_repository.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(portfolioRepository)
final portfolioRepositoryProvider = PortfolioRepositoryProvider._();

final class PortfolioRepositoryProvider
    extends
        $FunctionalProvider<
          PortfolioRepository,
          PortfolioRepository,
          PortfolioRepository
        >
    with $Provider<PortfolioRepository> {
  PortfolioRepositoryProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'portfolioRepositoryProvider',
        isAutoDispose: true,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$portfolioRepositoryHash();

  @$internal
  @override
  $ProviderElement<PortfolioRepository> $createElement(
    $ProviderPointer pointer,
  ) => $ProviderElement(pointer);

  @override
  PortfolioRepository create(Ref ref) {
    return portfolioRepository(ref);
  }

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(PortfolioRepository value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<PortfolioRepository>(value),
    );
  }
}

String _$portfolioRepositoryHash() =>
    r'98706cb464d0c53afdcfac91ab85f5b1220f5d0a';

/// The dashboard's data. Pull to refresh: `ref.refresh(portfolioProvider.future)`.

@ProviderFor(portfolio)
final portfolioProvider = PortfolioProvider._();

/// The dashboard's data. Pull to refresh: `ref.refresh(portfolioProvider.future)`.

final class PortfolioProvider
    extends
        $FunctionalProvider<
          AsyncValue<PortfolioView>,
          PortfolioView,
          FutureOr<PortfolioView>
        >
    with $FutureModifier<PortfolioView>, $FutureProvider<PortfolioView> {
  /// The dashboard's data. Pull to refresh: `ref.refresh(portfolioProvider.future)`.
  PortfolioProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'portfolioProvider',
        isAutoDispose: true,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$portfolioHash();

  @$internal
  @override
  $FutureProviderElement<PortfolioView> $createElement(
    $ProviderPointer pointer,
  ) => $FutureProviderElement(pointer);

  @override
  FutureOr<PortfolioView> create(Ref ref) {
    return portfolio(ref);
  }
}

String _$portfolioHash() => r'3fba2f8cf3f4e8a53b5346bc1d6c7a235dd82201';
