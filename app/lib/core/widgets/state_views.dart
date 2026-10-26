import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../l10n/app_localizations.dart';
import '../errors/error_messages.dart';

/// Loading / error-with-retry / data for an [AsyncValue]: every screen shows the same three states.
class AsyncValueView<T> extends StatelessWidget {
  const AsyncValueView({super.key, required this.value, required this.data, required this.onRetry});

  final AsyncValue<T> value;
  final Widget Function(T data) data;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    // Keep showing old data while refreshing (pull to refresh shows its own spinner).
    if (value.hasValue) return data(value.requireValue);
    if (value.hasError) return ErrorView(error: value.error!, onRetry: onRetry);
    return const LoadingView();
  }
}

class LoadingView extends StatelessWidget {
  const LoadingView({super.key});

  @override
  Widget build(BuildContext context) => const Center(child: CircularProgressIndicator());
}

class ErrorView extends StatelessWidget {
  const ErrorView({super.key, required this.error, required this.onRetry});

  final Object error;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return _CenteredMessage(
      icon: Icons.cloud_off_outlined,
      iconColor: Theme.of(context).colorScheme.error,
      message: errorMessage(l10n, error),
      action: FilledButton.tonalIcon(onPressed: onRetry, icon: const Icon(Icons.refresh), label: Text(l10n.retry)),
    );
  }
}

class EmptyView extends StatelessWidget {
  const EmptyView({super.key, required this.message, this.icon = Icons.inbox_outlined});

  final String message;
  final IconData icon;

  @override
  Widget build(BuildContext context) =>
      _CenteredMessage(icon: icon, iconColor: Theme.of(context).colorScheme.outline, message: message);
}

/// Tabs and screens that later sessions build.
class ComingSoonView extends StatelessWidget {
  const ComingSoonView({super.key, required this.feature, this.icon = Icons.construction_outlined});

  final String feature;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return Scaffold(
      appBar: AppBar(title: Text(feature)),
      body: _CenteredMessage(
        icon: icon,
        iconColor: Theme.of(context).colorScheme.secondary,
        title: l10n.comingSoonTitle,
        message: l10n.comingSoonBody(feature),
      ),
    );
  }
}

class _CenteredMessage extends StatelessWidget {
  const _CenteredMessage({required this.icon, required this.iconColor, required this.message, this.title, this.action});

  final IconData icon;
  final Color iconColor;
  final String? title;
  final String message;
  final Widget? action;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    // Scrollable, so it also works inside RefreshIndicator and on small screens.
    return LayoutBuilder(
      builder: (context, constraints) => SingleChildScrollView(
        physics: const AlwaysScrollableScrollPhysics(),
        child: ConstrainedBox(
          constraints: BoxConstraints(minHeight: constraints.maxHeight),
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(32),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(icon, size: 56, color: iconColor),
                  const SizedBox(height: 16),
                  if (title != null) ...[
                    Text(title!, style: theme.textTheme.titleMedium, textAlign: TextAlign.center),
                    const SizedBox(height: 8),
                  ],
                  Text(message, style: theme.textTheme.bodyLarge, textAlign: TextAlign.center),
                  if (action != null) ...[const SizedBox(height: 24), action!],
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
