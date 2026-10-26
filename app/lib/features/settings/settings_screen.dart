import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_providers.dart';
import '../../core/auth/auth_controller.dart';
import '../../core/config/server_config.dart';
import '../../core/errors/api_error.dart';
import '../../core/errors/error_messages.dart';
import '../../l10n/app_localizations.dart';

const appVersion = String.fromEnvironment('APP_VERSION', defaultValue: '0.1.0 (Session A)');

/// Server URL (reachable when signed out too, from the login screen), account, about.
class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  late final TextEditingController _url = TextEditingController(text: ref.read(serverUrlProvider));
  String? _problem;
  bool _testing = false;

  @override
  void dispose() {
    _url.dispose();
    super.dispose();
  }

  String? _check(AppLocalizations l10n) => switch (checkServerUrl(_url.text, allowHttp: ServerConfig.allowHttp)) {
        ServerUrlProblem.invalid => l10n.serverInvalidUrl,
        ServerUrlProblem.httpNotAllowed => l10n.serverHttpNotAllowed,
        null => null,
      };

  Future<void> _save() async {
    final l10n = AppLocalizations.of(context);
    final problem = _check(l10n);
    setState(() => _problem = problem);
    if (problem != null) return;
    final url = normalizeServerUrl(_url.text);
    if (url == ref.read(serverUrlProvider)) return;
    // Tokens belong to one backend: sign out (revoking on the old server) before switching.
    if (ref.read(authControllerProvider).value != null) {
      await ref.read(authControllerProvider.notifier).logout();
    }
    await ref.read(serverUrlProvider.notifier).set(url);
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(l10n.serverSaved)));
    await Navigator.of(context).maybePop();
  }

  /// Any answer from OUR backend counts: /api/auth/me without a token gives 401 UNAUTHENTICATED.
  Future<void> _test() async {
    final l10n = AppLocalizations.of(context);
    final problem = _check(l10n);
    setState(() => _problem = problem);
    if (problem != null) return;
    setState(() => _testing = true);
    String message;
    try {
      await Dio(backendOptions(normalizeServerUrl(_url.text))).get<Object>('/api/auth/me');
      message = l10n.serverReachable;
    } catch (e) {
      final error = ApiError.from(e);
      message = error.code == 'UNAUTHENTICATED' ? l10n.serverReachable : errorMessage(l10n, error);
    }
    if (!mounted) return;
    setState(() => _testing = false);
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    final user = ref.watch(authControllerProvider).value;
    final presets = <(String, String)>[
      (l10n.serverPresetCodespace, ServerConfig.codespaceUrl),
      if (ServerConfig.allowHttp) (l10n.serverPresetEmulator, ServerConfig.emulatorUrl),
      if (ServerConfig.allowHttp) (l10n.serverPresetLan, ServerConfig.lanUrl),
    ];
    return Scaffold(
      appBar: AppBar(title: Text(l10n.settingsTitle)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text(l10n.serverSection, style: theme.textTheme.titleMedium),
          const SizedBox(height: 12),
          TextField(
            key: const Key('serverUrl'),
            controller: _url,
            keyboardType: TextInputType.url,
            autocorrect: false,
            decoration: InputDecoration(
              labelText: l10n.serverUrlLabel,
              helperText: l10n.serverUrlHelp,
              helperMaxLines: 2,
              errorText: _problem,
              errorMaxLines: 2,
            ),
            onChanged: (_) => setState(() => _problem = null),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              for (final (label, url) in presets)
                ActionChip(label: Text(label), onPressed: () => setState(() => _url.text = url)),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: OutlinedButton(
                  onPressed: _testing ? null : _test,
                  child: _testing
                      ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                      : Text(l10n.serverTest),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(child: FilledButton(onPressed: _save, child: Text(l10n.save))),
            ],
          ),
          if (user != null) ...[
            const Divider(height: 40),
            ListTile(
              contentPadding: EdgeInsets.zero,
              leading: const Icon(Icons.badge_outlined),
              title: Text(l10n.signedInAs(user.displayName)),
              subtitle: Text('${user.username} · ${user.officeName}'),
            ),
          ],
          const Divider(height: 40),
          Text(l10n.aboutSection, style: theme.textTheme.titleMedium),
          const SizedBox(height: 8),
          Text(l10n.appVersion(appVersion)),
        ],
      ),
    );
  }
}
