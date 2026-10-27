import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/errors/error_messages.dart';
import '../../core/widgets/state_views.dart';
import '../../l10n/app_localizations.dart';
import 'clients_repository.dart';

class ClientsScreen extends ConsumerStatefulWidget {
  const ClientsScreen({super.key});

  @override
  ConsumerState<ClientsScreen> createState() => _ClientsScreenState();
}

class _ClientsScreenState extends ConsumerState<ClientsScreen> {
  final _search = TextEditingController();
  Timer? _debounce;
  String _query = '';

  @override
  void dispose() {
    _debounce?.cancel();
    _search.dispose();
    super.dispose();
  }

  void _onChanged(String text) {
    _debounce?.cancel();
    // Wait until typing pauses: one request per search, not one per key press.
    _debounce = Timer(const Duration(milliseconds: 400), () => setState(() => _query = text.trim()));
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final list = ref.watch(clientListProvider(_query));
    return Scaffold(
      appBar: AppBar(title: Text(l10n.clientsTitle)),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 4, 16, 8),
            child: TextField(
              controller: _search,
              onChanged: _onChanged,
              textInputAction: TextInputAction.search,
              decoration: InputDecoration(
                prefixIcon: const Icon(Icons.search),
                hintText: l10n.searchClients,
                helperText: l10n.searchHelp,
                helperMaxLines: 2,
                suffixIcon: _search.text.isEmpty
                    ? null
                    : IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _search.clear();
                          _onChanged('');
                        },
                      ),
              ),
            ),
          ),
          Expanded(
            child: RefreshIndicator(
              onRefresh: () => ref.refresh(clientListProvider(_query).future),
              child: AsyncValueView(
                value: list,
                onRetry: () => ref.invalidate(clientListProvider(_query)),
                data: (state) => state.items.isEmpty
                    ? EmptyView(
                        message: _query.isEmpty ? l10n.noClients : l10n.noClientsFound(_query),
                        icon: Icons.person_search_outlined,
                      )
                    : _ClientList(query: _query, state: state),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _ClientList extends ConsumerWidget {
  const _ClientList({required this.query, required this.state});

  final String query;
  final ClientListState state;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final l10n = AppLocalizations.of(context);
    final theme = Theme.of(context);
    final hasFooter = state.nextPage != null || state.moreError != null;
    return NotificationListener<ScrollNotification>(
      // Infinite scroll: fetch the next page when the user is close to the end.
      onNotification: (n) {
        if (n.metrics.extentAfter < 400) ref.read(clientListProvider(query).notifier).loadMore();
        return false;
      },
      child: ListView.separated(
        physics: const AlwaysScrollableScrollPhysics(),
        itemCount: state.items.length + 1 + (hasFooter ? 1 : 0),
        separatorBuilder: (_, i) => i == 0 ? const SizedBox.shrink() : const Divider(height: 1, indent: 72),
        itemBuilder: (context, i) {
          if (i == 0) {
            return Padding(
              padding: const EdgeInsets.fromLTRB(16, 0, 16, 4),
              child: Text(l10n.clientsTotal(state.total), style: theme.textTheme.bodySmall),
            );
          }
          final index = i - 1;
          if (index == state.items.length) {
            if (state.moreError != null) {
              return ListTile(
                leading: Icon(Icons.error_outline, color: theme.colorScheme.error),
                title: Text(errorMessage(l10n, state.moreError!)),
                trailing: TextButton(
                  onPressed: () => ref.read(clientListProvider(query).notifier).loadMore(),
                  child: Text(l10n.retry),
                ),
              );
            }
            return const Padding(padding: EdgeInsets.all(16), child: Center(child: CircularProgressIndicator()));
          }
          final c = state.items[index];
          return ListTile(
            leading: CircleAvatar(child: Text(_initials(c.displayName))),
            title: Text(c.displayName),
            subtitle: Text([l10n.accountNumber(c.accountNo), if (c.staffName != null) c.staffName!].join(' · ')),
            trailing: c.active
                ? const Icon(Icons.chevron_right)
                : Chip(label: Text(l10n.clientInactive), visualDensity: VisualDensity.compact),
            onTap: () => context.go('/clients/${c.id}'),
          );
        },
      ),
    );
  }

  static String _initials(String name) {
    final parts = name.split(' ').where((p) => p.isNotEmpty).toList();
    if (parts.isEmpty) return '?';
    return (parts.first[0] + (parts.length > 1 ? parts.last[0] : '')).toUpperCase();
  }
}
