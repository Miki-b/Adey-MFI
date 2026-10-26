import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/auth/auth_controller.dart';
import '../../core/widgets/state_views.dart';
import '../../l10n/app_localizations.dart';

/// Shown while the stored session is checked at start, or when that check failed (offline).
class SplashScreen extends ConsumerWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final auth = ref.watch(authControllerProvider);
    return Scaffold(
      body: SafeArea(
        child: auth.hasError
            ? Column(
                children: [
                  Expanded(
                    child: ErrorView(
                      error: auth.error!,
                      onRetry: () => ref.read(authControllerProvider.notifier).retry(),
                    ),
                  ),
                  TextButton(onPressed: () => context.push('/settings'), child: Text(l10n.settingsTitle)),
                  const SizedBox(height: 16),
                ],
              )
            : Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const CircularProgressIndicator(),
                    const SizedBox(height: 16),
                    Text(l10n.restoringSession),
                  ],
                ),
              ),
      ),
    );
  }
}
