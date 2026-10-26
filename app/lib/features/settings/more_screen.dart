import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/auth/auth_controller.dart';
import '../../l10n/app_localizations.dart';

class MoreScreen extends ConsumerWidget {
  const MoreScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final user = ref.watch(authControllerProvider).value;
    return Scaffold(
      appBar: AppBar(title: Text(l10n.moreTitle)),
      body: ListView(
        children: [
          if (user != null)
            ListTile(
              leading: CircleAvatar(child: Text(user.displayName.isEmpty ? '?' : user.displayName[0])),
              title: Text(user.displayName),
              subtitle: Text('${user.isManager ? l10n.roleManager : l10n.roleOfficer} · ${user.officeName}'),
            ),
          const Divider(),
          if (user?.isManager ?? false)
            ListTile(
              leading: const Icon(Icons.fact_check_outlined),
              title: Text(l10n.reconciliation),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => context.go('/more/reconciliation'),
            ),
          ListTile(
            leading: const Icon(Icons.settings_outlined),
            title: Text(l10n.settingsTitle),
            trailing: const Icon(Icons.chevron_right),
            onTap: () => context.push('/settings'),
          ),
          ListTile(
            key: const Key('logout'),
            leading: const Icon(Icons.logout),
            title: Text(l10n.logout),
            onTap: () => _confirmLogout(context, ref),
          ),
        ],
      ),
    );
  }

  Future<void> _confirmLogout(BuildContext context, WidgetRef ref) async {
    final l10n = AppLocalizations.of(context);
    final ok = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(l10n.logoutConfirmTitle),
        content: Text(l10n.logoutConfirmBody),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context, false), child: Text(l10n.cancel)),
          FilledButton(onPressed: () => Navigator.pop(context, true), child: Text(l10n.logout)),
        ],
      ),
    );
    if (ok ?? false) await ref.read(authControllerProvider.notifier).logout();
  }
}
