import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/auth/auth_controller.dart';
import '../../l10n/app_localizations.dart';

/// Bottom tabs. Branch order in the router: dashboard, clients, collections, approvals, more.
/// Officers don't see Approvals, so tab positions and branch indexes differ: [_visibleBranches].
class HomeShell extends ConsumerWidget {
  const HomeShell({super.key, required this.shell});

  final StatefulNavigationShell shell;

  static const _approvalsBranch = 3;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final isManager = ref.watch(authControllerProvider).value?.isManager ?? false;
    final branches = _visibleBranches(isManager);
    final destinations = <int, NavigationDestination>{
      0: NavigationDestination(icon: const Icon(Icons.dashboard_outlined), selectedIcon: const Icon(Icons.dashboard), label: l10n.tabDashboard),
      1: NavigationDestination(icon: const Icon(Icons.people_outline), selectedIcon: const Icon(Icons.people), label: l10n.tabClients),
      2: NavigationDestination(icon: const Icon(Icons.directions_walk_outlined), selectedIcon: const Icon(Icons.directions_walk), label: l10n.tabCollections),
      3: NavigationDestination(icon: const Icon(Icons.gavel_outlined), selectedIcon: const Icon(Icons.gavel), label: l10n.tabApprovals),
      4: NavigationDestination(icon: const Icon(Icons.more_horiz), selectedIcon: const Icon(Icons.more_horiz), label: l10n.tabMore),
    };
    final selected = branches.indexOf(shell.currentIndex);
    return Scaffold(
      body: shell,
      bottomNavigationBar: NavigationBar(
        selectedIndex: selected < 0 ? 0 : selected,
        destinations: [for (final b in branches) destinations[b]!],
        onDestinationSelected: (i) {
          final branch = branches[i];
          // Tapping the current tab again goes back to its first screen.
          shell.goBranch(branch, initialLocation: branch == shell.currentIndex);
        },
      ),
    );
  }

  static List<int> _visibleBranches(bool isManager) => [
        for (var b = 0; b < 5; b++)
          if (b != _approvalsBranch || isManager) b,
      ];
}
