import 'package:decimal/decimal.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/auth/auth_controller.dart';
import '../../core/formatting/dates.dart';
import '../../core/formatting/money.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/state_views.dart';
import '../../core/widgets/brand.dart';
import '../../l10n/app_localizations.dart';
import 'portfolio_repository.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final portfolio = ref.watch(portfolioProvider);
    return Scaffold(
      appBar: AppBar(title: Text(l10n.tabDashboard)),
      body: RefreshIndicator(
        onRefresh: () => ref.refresh(portfolioProvider.future),
        child: AsyncValueView(
          value: portfolio,
          onRetry: () => ref.invalidate(portfolioProvider),
          data: (p) => _DashboardBody(portfolio: p),
        ),
      ),
    );
  }
}

class _DashboardBody extends ConsumerWidget {
  const _DashboardBody({required this.portfolio});

  final PortfolioView portfolio;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final user = ref.watch(authControllerProvider).value;
    final p = portfolio;
    return ListView(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
      physics: const AlwaysScrollableScrollPhysics(),
      children: [
        _Header(
          name: user?.displayName ?? '',
          office: p.scopeName,
          role: (user?.isManager ?? false) ? l10n.roleManager : l10n.roleOfficer,
          asOf: p.asOf,
        ),
        const SizedBox(height: 16),
        _ParCard(portfolio: p),
        const SizedBox(height: 12),
        GridView.count(
          crossAxisCount: 2,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: 1.9,
          children: [
            _CountCard(label: l10n.countActiveLoans, value: p.activeLoans, icon: Icons.account_balance_wallet_outlined),
            _CountCard(label: l10n.countPending, value: p.pending, icon: Icons.hourglass_top_outlined),
            _CountCard(
              label: l10n.countApprovedNotDisbursed,
              value: p.approvedNotDisbursed,
              icon: Icons.task_alt_outlined,
            ),
            _CountCard(
              label: l10n.countInArrears,
              value: p.inArrears,
              icon: Icons.warning_amber_outlined,
              alert: p.inArrears > 0,
            ),
          ],
        ),
        const SizedBox(height: 12),
        _AmountsCard(portfolio: p),
        const SizedBox(height: 24),
        _CollectionSection(
          title: l10n.dueToday,
          entries: p.dueToday,
          total: p.dueTodayCount,
          emptyText: l10n.nothingDueToday,
        ),
        const SizedBox(height: 16),
        _CollectionSection(
          title: l10n.overdue,
          entries: p.overdue,
          total: p.overdueCount,
          emptyText: l10n.nothingOverdue,
          showLateness: true,
        ),
        if (p.officers.isNotEmpty) ...[
          const SizedBox(height: 24),
          _OfficersSection(officers: p.officers),
        ],
      ],
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.name, required this.office, required this.role, required this.asOf});

  final String name;
  final String office;
  final String role;
  final DateTime? asOf;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(l10n.dashboardHello(name), style: theme.textTheme.headlineSmall),
        const SizedBox(height: 4),
        Text('$role · $office', style: theme.textTheme.bodyLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
        if (asOf != null)
          Text(
            l10n.dashboardAsOf(AppDates.format(asOf!)),
            style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
          ),
      ],
    );
  }
}

class _ParCard extends StatelessWidget {
  const _ParCard({required this.portfolio});

  final PortfolioView portfolio;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    final status = StatusColors.of(context);
    final level = parLevel(portfolio.par30);
    final (color, label) = switch (level) {
      ParLevel.healthy => (status.good, l10n.parHealthy),
      ParLevel.watch => (status.warning, l10n.parWatch),
      ParLevel.high => (status.danger, l10n.parHigh),
    };
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Text(
                  l10n.par30,
                  style: theme.textTheme.titleMedium
                      ?.copyWith(color: theme.colorScheme.primary, fontWeight: FontWeight.w700),
                ),
                const SizedBox(width: 8),
                StatusChip(label: label, color: color),
                const Spacer(),
                // A secondary action: tonal (yellow), compact.
                FilledButton.tonalIcon(
                  onPressed: () => _showParExplanation(context),
                  style: FilledButton.styleFrom(
                    minimumSize: const Size(0, 36),
                    padding: const EdgeInsets.symmetric(horizontal: 12),
                    textStyle: theme.textTheme.labelLarge,
                  ),
                  icon: const Icon(Icons.help_outline, size: 18),
                  label: Text(l10n.whatIsPar30),
                ),
              ],
            ),
            Text(
              formatPercent(portfolio.par30),
              key: const Key('par30Value'),
              style: theme.textTheme.displaySmall?.copyWith(color: color, fontWeight: FontWeight.w700),
            ),
            Text(l10n.par30AtRisk(formatEtb(portfolio.par30Principal)), style: theme.textTheme.bodyMedium),
            Text(
              l10n.par1Line(formatPercent(portfolio.par1)),
              style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.onSurfaceVariant),
            ),
          ],
        ),
      ),
    );
  }

  void _showParExplanation(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    showModalBottomSheet<void>(
      context: context,
      showDragHandle: true,
      isScrollControlled: true,
      useRootNavigator: true, // above the bottom tabs, not inside the tab's navigator
      builder: (context) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(l10n.par30ExplainTitle, style: Theme.of(context).textTheme.titleLarge),
              const SizedBox(height: 12),
              Text(l10n.par30ExplainBody, style: Theme.of(context).textTheme.bodyLarge),
              const SizedBox(height: 12),
              Text(l10n.par30ExplainThresholds, style: Theme.of(context).textTheme.labelLarge),
            ],
          ),
        ),
      ),
    );
  }
}

class _CountCard extends StatelessWidget {
  const _CountCard({required this.label, required this.value, required this.icon, this.alert = false});

  final String label;
  final int value;
  final IconData icon;
  final bool alert;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final color = alert ? StatusColors.of(context).danger : theme.colorScheme.primary;
    return Card(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Row(
              children: [
                Icon(icon, size: 20, color: color),
                const SizedBox(width: 6),
                Text('$value', style: theme.textTheme.headlineSmall?.copyWith(color: color, fontWeight: FontWeight.w700)),
              ],
            ),
            const SizedBox(height: 2),
            Text(label, maxLines: 2, overflow: TextOverflow.ellipsis, style: theme.textTheme.bodySmall),
          ],
        ),
      ),
    );
  }
}

class _AmountsCard extends StatelessWidget {
  const _AmountsCard({required this.portfolio});

  final PortfolioView portfolio;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    Widget amount(String label, String value, {Color? color}) => Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label, style: theme.textTheme.bodySmall),
              const SizedBox(height: 2),
              FittedBox(
                fit: BoxFit.scaleDown,
                alignment: Alignment.centerLeft,
                child: Text(value, style: theme.textTheme.titleMedium?.copyWith(color: color, fontWeight: FontWeight.w600)),
              ),
            ],
          ),
        );
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            amount(l10n.principalOutstanding, formatEtb(portfolio.principalOutstanding)),
            const SizedBox(width: 16),
            amount(
              l10n.totalOverdue,
              formatEtb(portfolio.totalOverdue),
              color: portfolio.totalOverdue > Decimal.zero ? StatusColors.of(context).danger : null,
            ),
          ],
        ),
      ),
    );
  }
}

class _CollectionSection extends StatelessWidget {
  const _CollectionSection({
    required this.title,
    required this.entries,
    required this.total,
    required this.emptyText,
    this.showLateness = false,
  });

  final String title;
  final List<CollectionEntry> entries;
  final int total;
  final String emptyText;
  final bool showLateness;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(
          title,
          trailing: total > 0 ? Text(l10n.sectionCount(entries.length, total), style: theme.textTheme.bodySmall) : null,
        ),
        const SizedBox(height: 8),
        if (entries.isEmpty)
          Text(emptyText, style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant))
        else
          Card(
            child: Column(
              children: [
                for (final (i, e) in entries.indexed) ...[
                  if (i > 0) const Divider(height: 1),
                  ListTile(
                    title: Text(e.clientName),
                    subtitle: Text(
                      '${l10n.installmentNumber(e.installment)} · ${AppDates.formatOrDash(e.dueDate)}'
                      '${showLateness ? ' · ${l10n.daysLate(e.daysOverdue)}' : ''}',
                    ),
                    trailing: Text(
                      formatEtb(e.amount),
                      style: theme.textTheme.titleSmall?.copyWith(
                        color: showLateness ? StatusColors.of(context).danger : null,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    onTap: () => context.go('/clients/${e.clientId}'),
                  ),
                ],
              ],
            ),
          ),
      ],
    );
  }
}

class _OfficersSection extends StatelessWidget {
  const _OfficersSection({required this.officers});

  final List<OfficerFigures> officers;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    final status = StatusColors.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        SectionHeader(l10n.officersTitle),
        const SizedBox(height: 8),
        Card(
          child: Column(
            children: [
              for (final (i, o) in officers.indexed) ...[
                if (i > 0) const Divider(height: 1),
                ListTile(
                  title: Text(o.name),
                  subtitle: Text(
                    '${l10n.officerLine(o.clients, o.activeLoans)}\n'
                    '${l10n.officerOutstanding(formatEtb(o.principalOutstanding))} · '
                    '${l10n.officerOverdue(formatEtb(o.totalOverdue))}',
                  ),
                  isThreeLine: true,
                  trailing: Text(
                    formatPercent(o.par30),
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w700,
                      color: switch (parLevel(o.par30)) {
                        ParLevel.healthy => status.good,
                        ParLevel.watch => status.warning,
                        ParLevel.high => status.danger,
                      },
                    ),
                  ),
                ),
              ],
            ],
          ),
        ),
      ],
    );
  }
}
