// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'auth_controller.dart';

// **************************************************************************
// RiverpodGenerator
// **************************************************************************

// GENERATED CODE - DO NOT MODIFY BY HAND
// ignore_for_file: type=lint, type=warning
/// Who is signed in. `AsyncData(null)` = signed out, `AsyncData(officer)` = signed in,
/// loading = restoring the session at app start, error = couldn't check (e.g. no network);
/// the router turns these into the login screen, the app, or the splash screen.

@ProviderFor(AuthController)
final authControllerProvider = AuthControllerProvider._();

/// Who is signed in. `AsyncData(null)` = signed out, `AsyncData(officer)` = signed in,
/// loading = restoring the session at app start, error = couldn't check (e.g. no network);
/// the router turns these into the login screen, the app, or the splash screen.
final class AuthControllerProvider
    extends $AsyncNotifierProvider<AuthController, Officer?> {
  /// Who is signed in. `AsyncData(null)` = signed out, `AsyncData(officer)` = signed in,
  /// loading = restoring the session at app start, error = couldn't check (e.g. no network);
  /// the router turns these into the login screen, the app, or the splash screen.
  AuthControllerProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'authControllerProvider',
        isAutoDispose: false,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$authControllerHash();

  @$internal
  @override
  AuthController create() => AuthController();
}

String _$authControllerHash() => r'e531649887829d1dcaa207a884c16316c0908392';

/// Who is signed in. `AsyncData(null)` = signed out, `AsyncData(officer)` = signed in,
/// loading = restoring the session at app start, error = couldn't check (e.g. no network);
/// the router turns these into the login screen, the app, or the splash screen.

abstract class _$AuthController extends $AsyncNotifier<Officer?> {
  FutureOr<Officer?> build();
  @$mustCallSuper
  @override
  void runBuild() {
    final ref = this.ref as $Ref<AsyncValue<Officer?>, Officer?>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<AsyncValue<Officer?>, Officer?>,
              AsyncValue<Officer?>,
              Object?,
              Object?
            >;
    element.handleCreate(ref, build);
  }
}

/// A one-time message for the login screen ("your session has ended").

@ProviderFor(SessionNotice)
final sessionNoticeProvider = SessionNoticeProvider._();

/// A one-time message for the login screen ("your session has ended").
final class SessionNoticeProvider
    extends $NotifierProvider<SessionNotice, bool> {
  /// A one-time message for the login screen ("your session has ended").
  SessionNoticeProvider._()
    : super(
        from: null,
        argument: null,
        retry: null,
        name: r'sessionNoticeProvider',
        isAutoDispose: false,
        dependencies: null,
        $allTransitiveDependencies: null,
      );

  @override
  String debugGetCreateSourceHash() => _$sessionNoticeHash();

  @$internal
  @override
  SessionNotice create() => SessionNotice();

  /// {@macro riverpod.override_with_value}
  Override overrideWithValue(bool value) {
    return $ProviderOverride(
      origin: this,
      providerOverride: $SyncValueProvider<bool>(value),
    );
  }
}

String _$sessionNoticeHash() => r'cf51afcca9309f989fb6f0321ef7671e8c7b9bfb';

/// A one-time message for the login screen ("your session has ended").

abstract class _$SessionNotice extends $Notifier<bool> {
  bool build();
  @$mustCallSuper
  @override
  void runBuild() {
    final ref = this.ref as $Ref<bool, bool>;
    final element =
        ref.element
            as $ClassProviderElement<
              AnyNotifier<bool, bool>,
              bool,
              Object?,
              Object?
            >;
    element.handleCreate(ref, build);
  }
}
