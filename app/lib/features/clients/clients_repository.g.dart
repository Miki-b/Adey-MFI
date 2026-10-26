// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'clients_repository.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning

@ProviderFor(clientsRepository)
final clientsRepositoryProvider = ClientsRepositoryProvider._();

final class ClientsRepositoryProvider
    extends
        $FunctionalProvider<
          ClientsRepository,
          ClientsRepository,
          ClientsRepository
        >
    with $Provider<ClientsRepository> {
  ClientsRepositoryProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'clientsRepositoryProvider',
        isAutoDispose: true,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$clientsRepositoryHash();

  @$internal
  @override
  $ProviderElement<ClientsRepository> $createElement(
    $ProviderPointer pointer,
  ) => $ProviderElement(pointer);

  @override
  ClientsRepository create(Ref ref) {
    return clientsRepository(ref);
  }

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(ClientsRepository value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<ClientsRepository>(value),
    );
  }
}

String _$clientsRepositoryHash() => r'cbcade0a7e117061a518b1780a9fe77311c0e1b9';

@ProviderFor(clientProfile)
final clientProfileProvider = ClientProfileFamily._();

final class ClientProfileProvider
    extends
        $FunctionalProvider<
          AsyncValue<ClientProfile>,
          ClientProfile,
          FutureOr<ClientProfile>
        >
    with $FutureModifier<ClientProfile>, $FutureProvider<ClientProfile> {
  ClientProfileProvider._({
    required ClientProfileFamily super.from,
    required int super.argument,
  }) : super(
         retry: null,
         name: r'clientProfileProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$clientProfileHash();

  @override
  String toString() {
    return r'clientProfileProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  $FutureProviderElement<ClientProfile> $createElement(
    $ProviderPointer pointer,
  ) => $FutureProviderElement(pointer);

  @override
  FutureOr<ClientProfile> create(Ref ref) {
    final argument = this.argument as int;
    return clientProfile(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is ClientProfileProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$clientProfileHash() => r'22a48269f317f4e2e6fb20bdd7c9885bcdc6100e';

final class ClientProfileFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<ClientProfile>, int> {
  ClientProfileFamily._()
    : super(
        retry: null,
        name: r'clientProfileProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  ClientProfileProvider call(int clientId) =>
      ClientProfileProvider._(argument: clientId, from: this);

  @override
  String toString() => r'clientProfileProvider';
}

@ProviderFor(clientLoans)
final clientLoansProvider = ClientLoansFamily._();

final class ClientLoansProvider
    extends
        $FunctionalProvider<
          AsyncValue<List<ClientLoan>>,
          List<ClientLoan>,
          FutureOr<List<ClientLoan>>
        >
    with $FutureModifier<List<ClientLoan>>, $FutureProvider<List<ClientLoan>> {
  ClientLoansProvider._({
    required ClientLoansFamily super.from,
    required int super.argument,
  }) : super(
         retry: null,
         name: r'clientLoansProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$clientLoansHash();

  @override
  String toString() {
    return r'clientLoansProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  $FutureProviderElement<List<ClientLoan>> $createElement(
    $ProviderPointer pointer,
  ) => $FutureProviderElement(pointer);

  @override
  FutureOr<List<ClientLoan>> create(Ref ref) {
    final argument = this.argument as int;
    return clientLoans(ref, argument);
  }

  @override
  bool operator ==(Object other) {
    return other is ClientLoansProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$clientLoansHash() => r'c124667b6a8899a057c3a470036c42908c53aa0c';

final class ClientLoansFamily extends $Family
    with $FunctionalFamilyOverride<FutureOr<List<ClientLoan>>, int> {
  ClientLoansFamily._()
    : super(
        retry: null,
        name: r'clientLoansProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  ClientLoansProvider call(int clientId) =>
      ClientLoansProvider._(argument: clientId, from: this);

  @override
  String toString() => r'clientLoansProvider';
}

/// First page in [build]; [loadMore] appends the next one (called near the end of the list).

@ProviderFor(ClientList)
final clientListProvider = ClientListFamily._();

/// First page in [build]; [loadMore] appends the next one (called near the end of the list).
final class ClientListProvider
    extends $AsyncNotifierProvider<ClientList, ClientListState> {
  /// First page in [build]; [loadMore] appends the next one (called near the end of the list).
  ClientListProvider._({
    required ClientListFamily super.from,
    required String super.argument,
  }) : super(
         retry: null,
         name: r'clientListProvider',
         isAutoDispose: true,
         dependencies: null,
         $allTransitiveDependencies: null,
       );

  @override
  String debugGetCreateSourceHash() => _$clientListHash();

  @override
  String toString() {
    return r'clientListProvider'
        ''
        '($argument)';
  }

  @$internal
  @override
  ClientList create() => ClientList();

  @override
  bool operator ==(Object other) {
    return other is ClientListProvider && other.argument == argument;
  }

  @override
  int get hashCode {
    return argument.hashCode;
  }
}

String _$clientListHash() => r'afb85335a8af4af12fdb5788d69c4cde660e376d';

/// First page in [build]; [loadMore] appends the next one (called near the end of the list).

final class ClientListFamily extends $Family
    with
        $ClassFamilyOverride<
          ClientList,
          AsyncValue<ClientListState>,
          ClientListState,
          FutureOr<ClientListState>,
          String
        > {
  ClientListFamily._()
    : super(
        retry: null,
        name: r'clientListProvider',
        dependencies: null,
        $allTransitiveDependencies: null,
        isAutoDispose: true,
      );

  /// First page in [build]; [loadMore] appends the next one (called near the end of the list).

  ClientListProvider call(String query) =>
      ClientListProvider._(argument: query, from: this);

  @override
  String toString() => r'clientListProvider';
}

/// First page in [build]; [loadMore] appends the next one (called near the end of the list).

abstract class _$ClientList extends $AsyncNotifier<ClientListState> {
  late final _$args = ref.$arg as String;
  String get query => _$args;

  FutureOr<ClientListState> build(String query);
  @$mustCallSuper
  @override
  void runBuild() {
    final ref = this.ref as $Ref<AsyncValue<ClientListState>, ClientListState>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<AsyncValue<ClientListState>, ClientListState>,
              AsyncValue<ClientListState>,
              Object?,
              Object?
            >;
    element.handleCreate(ref, () => build(_$args));
  }
}
