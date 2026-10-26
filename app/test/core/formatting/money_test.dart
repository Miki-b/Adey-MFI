import 'package:adey_field/core/formatting/dates.dart';
import 'package:adey_field/core/formatting/money.dart';
import 'package:decimal/decimal.dart';
import 'package:flutter_test/flutter_test.dart';

Decimal d(String s) => Decimal.parse(s);

void main() {
  group('formatEtb', () {
    test('ETB, thousands separators, always 2 decimals', () {
      expect(formatEtb(d('1833.60')), 'ETB 1,833.60');
      expect(formatEtb(d('1833.6')), 'ETB 1,833.60');
      expect(formatEtb(d('0')), 'ETB 0.00');
      expect(formatEtb(d('999.99')), 'ETB 999.99');
      expect(formatEtb(d('1000')), 'ETB 1,000.00');
      expect(formatEtb(d('36745.16')), 'ETB 36,745.16');
      expect(formatEtb(d('1234567890.05')), 'ETB 1,234,567,890.05');
      expect(formatEtb(d('-50')), '-ETB 50.00');
    });

    test('rounds half up to cents if more decimals ever arrive', () {
      expect(formatEtb(d('2003.205')), 'ETB 2,003.21');
      expect(formatEtb(d('2003.204')), 'ETB 2,003.20');
    });

    test('dash for missing values', () {
      expect(formatEtbOrDash(null), '–');
    });
  });

  group('toDecimal (JSON number -> exact decimal)', () {
    test('keeps exactly what the backend wrote', () {
      // jsonDecode('1833.60') gives the double 1833.6; its shortest form is the exact decimal.
      expect(toDecimal(1833.6), d('1833.60'));
      expect(toDecimal(0.1), d('0.1'));
      expect(toDecimal(20000), d('20000'));
      expect(toDecimal(null), isNull);
    });

    test('sums are exact in Decimal (they would not be in double)', () {
      expect(0.1 + 0.2 == 0.3, isFalse); // why we never add doubles
      expect(toDecimal(0.1)! + toDecimal(0.2)!, d('0.3'));
      final installments = List.filled(11, toDecimal(1833.6)!) + [toDecimal(1833.61)!];
      expect(formatEtb(installments.reduce((a, b) => a + b)), 'ETB 22,003.21'); // 20,000 + 2,003.21 interest
    });
  });

  test('formatPercent', () {
    expect(formatPercent(d('64.76')), '64.76%');
    expect(formatPercent(d('0')), '0.00%');
    expect(formatPercent(d('4.999')), '5.00%');
  });

  test('dates: "23 Oct 2026", date part only', () {
    expect(AppDates.format(DateTime(2026, 10, 23)), '23 Oct 2026');
    expect(AppDates.format(DateTime(2026, 10, 23, 23, 59)), '23 Oct 2026');
    expect(AppDates.formatOrDash(null), '–');
  });
}
