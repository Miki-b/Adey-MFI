import 'package:adey_api/adey_api.dart';
import 'package:decimal/decimal.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../core/api/api_providers.dart';
import '../../core/errors/api_error.dart';
import '../../core/formatting/money.dart';

part 'clients_repository.g.dart';

class ClientListItem {
  const ClientListItem({
    required this.id,
    required this.displayName,
    required this.accountNo,
    required this.active,
    this.officeName,
    this.staffName,
  });

  final int id;
  final String displayName;
  final String accountNo;
  final bool active;
  final String? officeName;
  final String? staffName;
}

class ClientPage {
  const ClientPage({required this.items, required this.page, required this.totalPages, required this.totalElements});

  final List<ClientListItem> items;
  final int page;
  final int totalPages;
  final int totalElements;

  bool get isLast => page + 1 >= totalPages;
}

class ClientProfile {
  const ClientProfile({
    required this.id,
    required this.displayName,
    required this.accountNo,
    required this.active,
    this.mobileNo,
    this.gender,
    this.dateOfBirth,
    this.nationalId,
    this.officeName,
    this.staffName,
    this.activationDate,
  });

  final int id;
  final String displayName;
  final String accountNo;
  final bool active;
  final String? mobileNo;
  final String? gender;
  final DateTime? dateOfBirth;
  final String? nationalId;
  final String? officeName;
  final String? staffName;
  final DateTime? activationDate;
}

enum LoanState { pending, approved, active, closed, rejected, withdrawn, writtenOff, rescheduled, overpaid, unknown }

class ClientLoan {
  const ClientLoan({
    required this.loanId,
    required this.accountNo,
    required this.productName,
    required this.state,
    required this.principal,
    required this.totalOutstanding,
    required this.installmentsPaid,
    required this.installmentsTotal,
    required this.inArrears,
    this.nextDueDate,
    this.nextDueAmount,
  });

  final int loanId;
  final String accountNo;
  final String productName;
  final LoanState state;
  final Decimal principal;
  final Decimal? totalOutstanding;
  final int installmentsPaid;
  final int installmentsTotal;
  final bool inArrears;
  final DateTime? nextDueDate;
  final Decimal? nextDueAmount;

  /// 0..1 for a progress bar (installments, not money: "7 of 12 paid" is what officers say).
  double get progress => installmentsTotal == 0 ? 0 : installmentsPaid / installmentsTotal;
}

LoanState loanStateFromApi(LoanSummaryStatusEnum? s) => switch (s) {
      LoanSummaryStatusEnum.SUBMITTED_AND_PENDING_APPROVAL => LoanState.pending,
      LoanSummaryStatusEnum.APPROVED => LoanState.approved,
      LoanSummaryStatusEnum.ACTIVE => LoanState.active,
      LoanSummaryStatusEnum.CLOSED => LoanState.closed,
      LoanSummaryStatusEnum.REJECTED => LoanState.rejected,
      LoanSummaryStatusEnum.WITHDRAWN_BY_CLIENT => LoanState.withdrawn,
      LoanSummaryStatusEnum.WRITTEN_OFF => LoanState.writtenOff,
      LoanSummaryStatusEnum.RESCHEDULED => LoanState.rescheduled,
      LoanSummaryStatusEnum.OVERPAID => LoanState.overpaid,
      _ => LoanState.unknown,
    };

class ClientsRepository {
  const ClientsRepository(this._api);

  static const pageSize = 20;
  final AdeyApi _api;

  /// [query] matches part of the display name, ignoring case (the backend filters it).
  Future<ClientPage> search({String? query, int page = 0}) => _call(() async {
        final q = query?.trim();
        final r = (await _api
                .getClientsApi()
                .listClients(search: q == null || q.isEmpty ? null : q, page: page, size: pageSize))
            .data!;
        return ClientPage(
          items: [
            for (final c in r.content)
              ClientListItem(
                id: c.id,
                displayName: c.displayName,
                accountNo: c.accountNo,
                active: c.active,
                officeName: c.officeName,
                staffName: c.staffName,
              ),
          ],
          page: r.page,
          totalPages: r.totalPages,
          totalElements: r.totalElements,
        );
      });

  Future<ClientProfile> get(int clientId) => _call(() async {
        final c = (await _api.getClientsApi().getClient(clientId: clientId)).data!;
        return ClientProfile(
          id: c.id,
          displayName: c.displayName,
          accountNo: c.accountNo,
          active: c.active,
          mobileNo: c.mobileNo,
          gender: c.gender,
          dateOfBirth: c.dateOfBirth,
          nationalId: c.externalId,
          officeName: c.officeName,
          staffName: c.staffName,
          activationDate: c.activationDate,
        );
      });

  Future<List<ClientLoan>> loans(int clientId) => _call(() async {
        final list = (await _api.getLoansApi().getClientLoans(clientId: clientId)).data!;
        return [
          for (final l in list)
            ClientLoan(
              loanId: l.loanId,
              accountNo: l.accountNo,
              productName: l.productName,
              state: loanStateFromApi(l.status),
              principal: decimalOf(l.principal),
              totalOutstanding: decimalOf(l.totalOutstanding),
              installmentsPaid: l.installmentsPaid,
              installmentsTotal: l.installmentsTotal,
              inArrears: l.inArrears,
              nextDueDate: l.nextDueDate,
              nextDueAmount: toDecimal(l.nextDueAmount),
            ),
        ];
      });

  static Future<T> _call<T>(Future<T> Function() body) async {
    try {
      return await body();
    } catch (e) {
      throw ApiError.from(e);
    }
  }
}

@riverpod
ClientsRepository clientsRepository(Ref ref) => ClientsRepository(ref.watch(adeyApiProvider));

@riverpod
Future<ClientProfile> clientProfile(Ref ref, int clientId) => ref.watch(clientsRepositoryProvider).get(clientId);

@riverpod
Future<List<ClientLoan>> clientLoans(Ref ref, int clientId) => ref.watch(clientsRepositoryProvider).loans(clientId);

/// State of the infinite-scroll client list for one search text.
class ClientListState {
  const ClientListState({required this.items, required this.total, required this.nextPage, this.loadingMore = false, this.moreError});

  final List<ClientListItem> items;
  final int total;

  /// null = no more pages.
  final int? nextPage;
  final bool loadingMore;
  final Object? moreError;

  ClientListState copyWith({List<ClientListItem>? items, int? total, int? Function()? nextPage, bool? loadingMore, Object? Function()? moreError}) =>
      ClientListState(
        items: items ?? this.items,
        total: total ?? this.total,
        nextPage: nextPage != null ? nextPage() : this.nextPage,
        loadingMore: loadingMore ?? this.loadingMore,
        moreError: moreError != null ? moreError() : this.moreError,
      );
}

/// First page in [build]; [loadMore] appends the next one (called near the end of the list).
@riverpod
class ClientList extends _$ClientList {
  @override
  Future<ClientListState> build(String query) async {
    final first = await ref.watch(clientsRepositoryProvider).search(query: query);
    return ClientListState(items: first.items, total: first.totalElements, nextPage: first.isLast ? null : first.page + 1);
  }

  Future<void> loadMore() async {
    final current = state.value;
    if (current == null || current.nextPage == null || current.loadingMore) return;
    state = AsyncData(current.copyWith(loadingMore: true, moreError: () => null));
    try {
      final next = await ref.read(clientsRepositoryProvider).search(query: query, page: current.nextPage!);
      if (!ref.mounted) return;
      state = AsyncData(current.copyWith(
        items: [...current.items, ...next.items],
        total: next.totalElements,
        nextPage: () => next.isLast ? null : next.page + 1,
        loadingMore: false,
      ));
    } catch (e) {
      if (!ref.mounted) return;
      state = AsyncData(current.copyWith(loadingMore: false, moreError: () => e));
    }
  }
}
