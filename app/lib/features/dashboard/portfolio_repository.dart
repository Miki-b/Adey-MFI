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
  final DateTime dueDate;
  final Decimal amount;
  final int daysOverdue;
  final String? mobileNo;

  static CollectionEntry fromApi(CollectionItem c) => CollectionEntry(
        loanId: c.loanId,
        clientId: c.clientId,
        clientName: c.clientName,
        accountNo: c.accountNo,
        installment: c.installment,
        dueDate: c.dueDate,
        amount: decimalOf(c.amount),
        daysOverdue: c.daysOverdue,
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

  final DateTime asOf;

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

  /// Everything the API marks required is used as is; only optional fields need a fallback.
  static PortfolioView fromApi(Portfolio p) => PortfolioView(
        asOf: p.asOf,
        scopeName: p.scope.officeName,
        isOfficeScope: p.scope.role == ScopeRoleEnum.MANAGER,
        clients: p.counts.clients,
        activeLoans: p.counts.activeLoans,
        pending: p.counts.pendingApplications,
        approvedNotDisbursed: p.counts.approvedNotDisbursed,
        inArrears: p.counts.loansInArrears,
        principalOutstanding: decimalOf(p.amounts.principalOutstanding),
        totalOverdue: decimalOf(p.amounts.totalOverdue),
        par1: decimalOf(p.par.par1),
        par30: decimalOf(p.par.par30),
        par30Principal: decimalOf(p.par.par30Principal),
        dueToday: [for (final c in p.dueToday) CollectionEntry.fromApi(c)],
        dueTodayCount: p.dueTodayCount,
        overdue: [for (final c in p.overdue) CollectionEntry.fromApi(c)],
        overdueCount: p.overdueCount,
        officers: [
          for (final o in p.officers ?? const <OfficerSummary>[]) // managers only
            OfficerFigures(
              name: o.officerName ?? '',
              clients: o.clients,
              activeLoans: o.activeLoans,
              principalOutstanding: decimalOf(o.principalOutstanding),
              totalOverdue: decimalOf(o.totalOverdue),
              par30: decimalOf(o.par30),
            ),
        ],
      );
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
      final response = await _api.getPortfolioApi().getPortfolio();
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
