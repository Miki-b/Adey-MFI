import 'package:decimal/decimal.dart';

/// Money and percentages. Values come from the backend; the app only FORMATS them.
///
/// Why Decimal: the generated client decodes JSON numbers to Dart `num` (1833.60 arrives as the
/// double 1833.6). A double's shortest string ("1833.6") is exactly the decimal the backend wrote,
/// so we convert to Decimal at once ([toDecimal]) and never do arithmetic on doubles: in double,
/// 0.1 + 0.2 is 0.30000000000000004. If a screen ever needs a sum, it adds Decimals.
Decimal? toDecimal(num? value) => value == null ? null : Decimal.parse(value.toString());

/// [toDecimal] for fields the API always sends (required in the spec).
Decimal decimalOf(num value) => Decimal.parse(value.toString());

/// "ETB 1,833.60". Negative: "-ETB 50.00". Always 2 decimals (half-up rounding if more came in).
String formatEtb(Decimal amount, {String currency = 'ETB'}) {
  final negative = amount < Decimal.zero;
  final fixed = amount.abs().round(scale: 2).toStringAsFixed(2);
  final dot = fixed.indexOf('.');
  final grouped = _group(fixed.substring(0, dot));
  return '${negative ? '-' : ''}$currency $grouped${fixed.substring(dot)}';
}

/// Same as [formatEtb] but "–" for a missing value.
String formatEtbOrDash(Decimal? amount) => amount == null ? '–' : formatEtb(amount);

/// "64.76%" (2 decimals, as sent by the backend's PAR figures).
String formatPercent(Decimal percent) => '${percent.round(scale: 2).toStringAsFixed(2)}%';

String _group(String digits) {
  final out = StringBuffer();
  for (var i = 0; i < digits.length; i++) {
    if (i > 0 && (digits.length - i) % 3 == 0) out.write(',');
    out.write(digits[i]);
  }
  return out.toString();
}
