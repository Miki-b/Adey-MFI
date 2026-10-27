import 'dart:convert';

import 'package:adey_api/adey_api.dart';
import 'package:adey_field/core/formatting/money.dart';
import 'package:adey_field/features/dashboard/portfolio_repository.dart';
import 'package:decimal/decimal.dart';
import 'package:flutter_test/flutter_test.dart';

/// Shape of GET /api/me/portfolio for the seeded Adama manager (numbers from notes/05).
const adamaJson = '''
{"asOf":"2026-10-26",
 "scope":{"role":"MANAGER","officeId":3,"officeName":"Adama Branch","staffId":6,"officerName":"Selamawit Tesfaye"},
 "counts":{"clients":4,"activeLoans":2,"pendingApplications":0,"approvedNotDisbursed":0,"loansInArrears":1},
 "amounts":{"currency":"ETB","principalOutstanding":56736.16,"totalOverdue":4071.40},
 "par":{"par1":64.76,"par1Principal":36745.16,"par30":64.76,"par30Principal":36745.16},
 "dueToday":[],"dueTodayCount":0,
 "overdue":[{"loanId":9,"accountNo":"000000009","clientId":9,"clientName":"Getachew Ayele","clientMobileNo":"0911000009",
   "installment":3,"dueDate":"2026-07-01","amount":2035.70,"daysOverdue":117}],
 "overdueCount":1,
 "officers":[{"staffId":3,"officerName":"Meron Alemu","clients":4,"activeLoans":2,"principalOutstanding":56736.16,
   "totalOverdue":4071.40,"par30":64.76}]}
''';

void main() {
  test('maps the generated Portfolio into Decimal figures', () {
    final p = PortfolioView.fromApi(Portfolio.fromJson(jsonDecode(adamaJson) as Map<String, dynamic>));

    expect(p.isOfficeScope, isTrue);
    expect(p.scopeName, 'Adama Branch');
    expect(p.inArrears, 1);
    expect(formatPercent(p.par30), '64.76%');
    expect(formatEtb(p.par30Principal), 'ETB 36,745.16');
    expect(formatEtb(p.principalOutstanding), 'ETB 56,736.16');
    expect(formatEtb(p.totalOverdue), 'ETB 4,071.40');
    expect(p.overdue.single.clientName, 'Getachew Ayele');
    expect(p.overdue.single.daysOverdue, 117);
    expect(formatEtb(p.overdue.single.amount), 'ETB 2,035.70');
    expect(p.officers.single.name, 'Meron Alemu');
    expect(p.asOf, DateTime(2026, 10, 26));
  });

  test('officers is optional (officers get none); everything else is required by the spec', () {
    final json = jsonDecode(adamaJson) as Map<String, dynamic>..remove('officers');
    expect(PortfolioView.fromApi(Portfolio.fromJson(json)).officers, isEmpty);

    final broken = jsonDecode(adamaJson) as Map<String, dynamic>..remove('par');
    expect(() => Portfolio.fromJson(broken), throwsA(anything)); // a backend bug would surface here
  });

  test('PAR colour bands: green < 5 %, amber 5–10 %, red > 10 %', () {
    expect(parLevel(Decimal.parse('0')), ParLevel.healthy);
    expect(parLevel(Decimal.parse('4.99')), ParLevel.healthy);
    expect(parLevel(Decimal.parse('5.00')), ParLevel.watch);
    expect(parLevel(Decimal.parse('10.00')), ParLevel.watch);
    expect(parLevel(Decimal.parse('10.01')), ParLevel.high);
    expect(parLevel(Decimal.parse('64.76')), ParLevel.high);
  });
}
