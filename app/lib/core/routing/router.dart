import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../features/auth/login_screen.dart';
import '../../features/auth/splash_screen.dart';
import '../../features/clients/client_details_screen.dart';
import '../../features/clients/clients_screen.dart';
import '../../features/dashboard/dashboard_screen.dart';
import '../../features/home/home_shell.dart';
import '../../features/settings/more_screen.dart';
import '../../features/settings/settings_screen.dart';
import '../../l10n/app_localizations.dart';
import '../auth/auth_controller.dart';
import '../widgets/state_views.dart';

part 'router.g.dart';

/// Routes only managers may open. UI convenience only: the backend refuses the calls anyway.
const managerOnlyPrefixes = ['/approvals', '/more/reconciliation'];

/// Where to go for a location, given the auth state. Pure, so it is easy to read and test.
String? authRedirect({required String location, required AsyncValue<Object?> auth, required bool isManager}) {
  const open = ['/login', '/settings'];
  if (!auth.hasValue) {
    // Restoring the session (or that failed: the splash offers "try again"). Settings stay reachable.
    return location == '/splash' || location == '/settings' ? null : '/splash';
  }
  final signedIn = auth.value != null;
  if (!signedIn) return open.contains(location) ? null : '/login';
  if (location == '/login' || location == '/splash') return '/dashboard';
  if (!isManager && managerOnlyPrefixes.any(location.startsWith)) return '/dashboard';
  return null;
}

@Riverpod(keepAlive: true)
GoRouter router(Ref ref) {
  // The router is created once; auth changes only make it re-run its redirect.
  final refresh = ValueNotifier(0);
  ref.listen(authControllerProvider, (_, _) => refresh.value++);
  ref.onDispose(refresh.dispose);

  return GoRouter(
    initialLocation: '/splash',
    refreshListenable: refresh,
    redirect: (context, state) {
      final auth = ref.read(authControllerProvider);
      return authRedirect(
        location: state.matchedLocation,
        auth: auth,
        isManager: auth.value?.isManager ?? false,
      );
    },
    routes: [
      GoRoute(path: '/splash', builder: (_, _) => const SplashScreen()),
      GoRoute(path: '/login', builder: (_, _) => const LoginScreen()),
      GoRoute(path: '/settings', builder: (_, _) => const SettingsScreen()),
      StatefulShellRoute.indexedStack(
        builder: (context, state, shell) => HomeShell(shell: shell),
        branches: [
          StatefulShellBranch(routes: [GoRoute(path: '/dashboard', builder: (_, _) => const DashboardScreen())]),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/clients',
                builder: (_, _) => const ClientsScreen(),
                routes: [
                  GoRoute(
                    path: ':clientId',
                    builder: (_, state) => ClientDetailsScreen(clientId: int.parse(state.pathParameters['clientId']!)),
                  ),
                ],
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/collections',
                builder: (context, _) => ComingSoonView(
                  feature: AppLocalizations.of(context).tabCollections,
                  icon: Icons.directions_walk,
                ),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/approvals',
                builder: (context, _) => ComingSoonView(
                  feature: AppLocalizations.of(context).tabApprovals,
                  icon: Icons.gavel_outlined,
                ),
              ),
            ],
          ),
          StatefulShellBranch(
            routes: [
              GoRoute(
                path: '/more',
                builder: (_, _) => const MoreScreen(),
                routes: [
                  GoRoute(
                    path: 'reconciliation',
                    builder: (context, _) => ComingSoonView(
                      feature: AppLocalizations.of(context).reconciliation,
                      icon: Icons.fact_check_outlined,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    ],
  );
}
