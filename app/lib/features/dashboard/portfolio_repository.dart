import 'package:adey_api/adey_api.dart';
import 'package:decimal/decimal.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../core/api/api_providers.dart';
import '../../core/errors/api_error.dart';
import '../../core/formatting/money.dart';

part 'portfolio_repository.g.dart';

/// An installment to collect (due today or overdue).
class CollectionEntry {
  const CollectionEntry({
    required this.loanId,
    required this.clientId,
    required this.clientName,
    required this.accountNo,
    required this.installment,
    required this.dueDate,
    required this.amount,
    required this.daysOverdue,
    this.mobileNo,
  });

  final int loanId;
  final int clientId;
  final String clientName;
  final String accountNo;
  final int installment;
  final DateTime? dueDate;
  final Decimal amount;
  final int daysOverdue;
  final String? mobileNo;

  static CollectionEntry fromApi(CollectionItem c) => CollectionEntry(
        loanId: c.loanId ?? 0,
        clientId: c.clientId ?? 0,
        clientName: c.clientName ?? '',
        accountNo: c.accountNo ?? '',
        installment: c.installment ?? 0,
        dueDate: c.dueDate,
        amount: toDecimal(c.amount) ?? Decimal.zero,
        daysOverdue: c.daysOverdue ?? 0,
        mobileNo: c.clientMobileNo,
      );
}

class OfficerFigures {
  const OfficerFigures({
    required this.name,
    required this.clients,
    required this.activeLoans,
    required this.principalOutstanding,
    required this.totalOverdue,
    required this.par30,
  });

  final String name;
  final int clients;
  final int activeLoans;
  final Decimal principalOutstanding;
  final Decimal totalOverdue;
  final Decimal par30;
}

/// GET /api/me/portfolio in app terms. All money/percent values are Decimal (see money.dart).
class PortfolioView {
  const PortfolioView({
    required this.asOf,
    required this.scopeName,
    required this.isOfficeScope,
    required this.clients,
    required this.activeLoans,
    required this.pending,
    required this.approvedNotDisbursed,
    required this.inArrears,
    required this.principalOutstanding,
    required this.totalOverdue,
    required this.par1,
    required this.par30,
    required this.par30Principal,
    required this.dueToday,
    required this.dueTodayCount,
    required this.overdue,
    required this.overdueCount,
    required this.officers,
  });

  final DateTime? asOf;

  /// Office name (manager) or the officer's own office.
  final String scopeName;
  final bool isOfficeScope;
  final int clients;
  final int activeLoans;
  final int pending;
  final int approvedNotDisbursed;
  final int inArrears;
  final Decimal principalOutstanding;
  final Decimal totalOverdue;
  final Decimal par1;
  final Decimal par30;
  final Decimal par30Principal;
  final List<CollectionEntry> dueToday;
  final int dueTodayCount;
  final List<CollectionEntry> overdue;
  final int overdueCount;

  /// Managers only (empty for officers).
  final List<OfficerFigures> officers;

  static PortfolioView fromApi(Portfolio p) {
    final zero = Decimal.zero;
    return PortfolioView(
      asOf: p.asOf,
      scopeName: p.scope?.officeName ?? '',
      isOfficeScope: (p.scope?.role ?? '').toUpperCase() == 'MANAGER',
      clients: p.counts?.clients ?? 0,
      activeLoans: p.counts?.activeLoans ?? 0,
      pending: p.counts?.pendingApplications ?? 0,
      approvedNotDisbursed: p.counts?.approvedNotDisbursed ?? 0,
      inArrears: p.counts?.loansInArrears ?? 0,
      principalOutstanding: toDecimal(p.amounts?.principalOutstanding) ?? zero,
      totalOverdue: toDecimal(p.amounts?.totalOverdue) ?? zero,
      par1: toDecimal(p.par?.par1) ?? zero,
      par30: toDecimal(p.par?.par30) ?? zero,
      par30Principal: toDecimal(p.par?.par30Principal) ?? zero,
      dueToday: [for (final c in p.dueToday ?? const <CollectionItem>[]) CollectionEntry.fromApi(c)],
      dueTodayCount: p.dueTodayCount ?? 0,
      overdue: [for (final c in p.overdue ?? const <CollectionItem>[]) CollectionEntry.fromApi(c)],
      overdueCount: p.overdueCount ?? 0,
      officers: [
        for (final o in p.officers ?? const <OfficerSummary>[])
          OfficerFigures(
            name: o.officerName ?? '',
            clients: o.clients ?? 0,
            activeLoans: o.activeLoans ?? 0,
            principalOutstanding: toDecimal(o.principalOutstanding) ?? zero,
            totalOverdue: toDecimal(o.totalOverdue) ?? zero,
            par30: toDecimal(o.par30) ?? zero,
          ),
      ],
    );
  }
}

enum ParLevel { healthy, watch, high }

/// Green below 5 %, amber 5–10 %, red above 10 % (common MFI targets, see notes/05).
ParLevel parLevel(Decimal percent) {
  if (percent < Decimal.fromInt(5)) return ParLevel.healthy;
  if (percent <= Decimal.fromInt(10)) return ParLevel.watch;
  return ParLevel.high;
}

class PortfolioRepository {
  const PortfolioRepository(this._api);

  final AdeyApi _api;

  Future<PortfolioView> load() async {
    try {
      final response = await _api.getPortfolioApi().portfolio();
      return PortfolioView.fromApi(response.data!);
    } catch (e) {
      throw ApiError.from(e);
    }
  }
}

@riverpod
PortfolioRepository portfolioRepository(Ref ref) => PortfolioRepository(ref.watch(adeyApiProvider));

/// The dashboard's data. Pull to refresh: `ref.refresh(portfolioProvider.future)`.
@riverpod
Future<PortfolioView> portfolio(Ref ref) => ref.watch(portfolioRepositoryProvider).load();
