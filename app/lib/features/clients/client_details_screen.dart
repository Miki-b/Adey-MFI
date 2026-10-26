import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/formatting/dates.dart';
import '../../core/formatting/money.dart';
import '../../core/theme/app_theme.dart';
import '../../core/widgets/state_views.dart';
import '../../l10n/app_localizations.dart';
import 'clients_repository.dart';

class ClientDetailsScreen extends ConsumerWidget {
  const ClientDetailsScreen({super.key, required this.clientId});

  final int clientId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final profile = ref.watch(clientProfileProvider(clientId));
    return Scaffold(
      appBar: AppBar(title: Text(profile.value?.displayName ?? l10n.clientDetailsTitle)),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(clientLoansProvider(clientId));
          ref.invalidate(clientProfileProvider(clientId));
          await ref.read(clientProfileProvider(clientId).future);
        },
        child: AsyncValueView(
          value: profile,
          onRetry: () => ref.invalidate(clientProfileProvider(clientId)),
          data: (p) => ListView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 24),
            children: [
              _ProfileCard(profile: p),
              const SizedBox(height: 24),
              Text(l10n.loansTitle, style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              _Loans(clientId: clientId),
            ],
          ),
        ),
      ),
    );
  }
}

class _ProfileCard extends StatelessWidget {
  const _ProfileCard({required this.profile});

  final ClientProfile profile;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    Widget row(String label, String? value) => Padding(
          padding: const EdgeInsets.symmetric(vertical: 4),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SizedBox(width: 130, child: Text(label, style: theme.textTheme.bodyMedium?.copyWith(color: theme.colorScheme.onSurfaceVariant))),
              Expanded(child: Text(value == null || value.isEmpty ? l10n.notAvailable : value, style: theme.textTheme.bodyLarge)),
            ],
          ),
        );
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(profile.displayName, style: theme.textTheme.titleLarge),
            Text(l10n.accountNumber(profile.accountNo), style: theme.textTheme.bodyMedium),
            const Divider(height: 24),
            row(l10n.mobileNumber, profile.mobileNo),
            row(l10n.gender, profile.gender),
            row(l10n.dateOfBirth, AppDates.formatOrDash(profile.dateOfBirth)),
            row(l10n.nationalId, profile.nationalId),
            row(l10n.loanOfficer, profile.staffName),
            row(l10n.office, profile.officeName),
            row(l10n.clientSince, AppDates.formatOrDash(profile.activationDate)),
          ],
        ),
      ),
    );
  }
}

class _Loans extends ConsumerWidget {
  const _Loans({required this.clientId});

  final int clientId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final loans = ref.watch(clientLoansProvider(clientId));
    return switch (loans) {
      AsyncValue(hasValue: true, :final value?) when value.isEmpty =>
        Text(l10n.noLoans, style: Theme.of(context).textTheme.bodyMedium),
      AsyncValue(hasValue: true, :final value?) => Column(
          children: [
            for (final loan in value) Padding(padding: const EdgeInsets.only(bottom: 12), child: _LoanCard(loan: loan)),
          ],
        ),
      AsyncValue(:final error?) => SizedBox(
          height: 240,
          child: ErrorView(error: error, onRetry: () => ref.invalidate(clientLoansProvider(clientId))),
        ),
      _ => const Padding(padding: EdgeInsets.all(24), child: LoadingView()),
    };
  }
}

class _LoanCard extends StatelessWidget {
  const _LoanCard({required this.loan});

  final ClientLoan loan;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    final status = StatusColors.of(context);
    final (label, color) = _status(l10n, theme, status, loan.state);
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(loan.productName, style: theme.textTheme.titleMedium),
                      Text(l10n.accountNumber(loan.accountNo), style: theme.textTheme.bodySmall),
                    ],
                  ),
                ),
                if (loan.inArrears) ...[
                  Chip(
                    avatar: Icon(Icons.warning_amber, size: 16, color: status.danger),
                    label: Text(l10n.loanInArrears),
                    labelStyle: TextStyle(color: status.danger, fontWeight: FontWeight.w600),
                    side: BorderSide(color: status.danger),
                    visualDensity: VisualDensity.compact,
                  ),
                  const SizedBox(width: 6),
                ],
                Chip(
                  label: Text(label),
                  labelStyle: TextStyle(color: color, fontWeight: FontWeight.w600),
                  side: BorderSide(color: color),
                  visualDensity: VisualDensity.compact,
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(child: _Figure(label: l10n.loanPrincipal, value: formatEtb(loan.principal))),
                Expanded(
                  child: _Figure(
                    label: l10n.loanOutstanding,
                    // Before disbursement nothing is owed yet; "ETB 0.00" would read like "paid off".
                    value: loan.state == LoanState.pending || loan.state == LoanState.approved
                        ? l10n.notAvailable
                        : formatEtbOrDash(loan.totalOutstanding),
                  ),
                ),
              ],
            ),
            if (loan.installmentsTotal > 0) ...[
              const SizedBox(height: 12),
              LinearProgressIndicator(value: loan.progress, minHeight: 8, borderRadius: BorderRadius.circular(4)),
              const SizedBox(height: 4),
              Text(l10n.loanProgress(loan.installmentsPaid, loan.installmentsTotal), style: theme.textTheme.bodySmall),
            ],
            if (loan.nextDueDate != null && loan.nextDueAmount != null) ...[
              const SizedBox(height: 8),
              Text(
                l10n.loanNextDue(AppDates.format(loan.nextDueDate!), formatEtb(loan.nextDueAmount!)),
                style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w600),
              ),
            ],
          ],
        ),
      ),
    );
  }

  static (String, Color) _status(AppLocalizations l10n, ThemeData theme, StatusColors s, LoanState state) =>
      switch (state) {
        LoanState.pending => (l10n.loanStatusPending, s.warning),
        LoanState.approved => (l10n.loanStatusApproved, theme.colorScheme.secondary),
        LoanState.active => (l10n.loanStatusActive, s.good),
        LoanState.closed => (l10n.loanStatusClosed, theme.colorScheme.outline),
        LoanState.rejected => (l10n.loanStatusRejected, s.danger),
        LoanState.withdrawn => (l10n.loanStatusWithdrawn, theme.colorScheme.outline),
        LoanState.writtenOff => (l10n.loanStatusWrittenOff, s.danger),
        LoanState.rescheduled => (l10n.loanStatusRescheduled, theme.colorScheme.outline),
        LoanState.overpaid => (l10n.loanStatusOverpaid, theme.colorScheme.secondary),
        LoanState.unknown => (l10n.loanStatusUnknown, theme.colorScheme.outline),
      };
}

class _Figure extends StatelessWidget {
  const _Figure({required this.label, required this.value});

  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: theme.textTheme.bodySmall),
        Text(value, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
      ],
    );
  }
}
