import 'package:adey_field/core/routing/router.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  String? go(String location, AsyncValue<Object?> auth, {bool manager = false}) =>
      authRedirect(location: location, auth: auth, isManager: manager);

  const signedOut = AsyncData<Object?>(null);
  const signedIn = AsyncData<Object?>('officer');

  test('while the session is being restored: splash (settings stay reachable)', () {
    expect(go('/dashboard', const AsyncLoading()), '/splash');
    expect(go('/splash', const AsyncLoading()), isNull);
    expect(go('/settings', const AsyncLoading()), isNull);
    expect(go('/clients', AsyncError<Object?>(Exception('offline'), StackTrace.empty)), '/splash');
  });

  test('signed out: only login and settings', () {
    expect(go('/dashboard', signedOut), '/login');
    expect(go('/clients/3', signedOut), '/login');
    expect(go('/login', signedOut), isNull);
    expect(go('/settings', signedOut), isNull);
  });

  test('signed in: login and splash go to the dashboard', () {
    expect(go('/login', signedIn), '/dashboard');
    expect(go('/splash', signedIn), '/dashboard');
    expect(go('/clients/3', signedIn), isNull);
  });

  test('manager-only routes', () {
    expect(go('/approvals', signedIn), '/dashboard');
    expect(go('/more/reconciliation', signedIn), '/dashboard');
    expect(go('/approvals', signedIn, manager: true), isNull);
    expect(go('/more/reconciliation', signedIn, manager: true), isNull);
    expect(go('/more', signedIn), isNull);
  });
}
