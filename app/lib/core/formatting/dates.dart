import 'package:intl/intl.dart';

/// THE place where dates become text. Every screen calls [AppDates.format], so Session D can add
/// the Ethiopian calendar (and Amharic month names) by changing only this file.
///
/// Backend dates are calendar dates without a time zone ("2026-10-23", Addis Ababa business days).
/// The generated client parses them as local midnight; we only ever format the date part, so no
/// time zone shift can move them to another day.
class AppDates {
  const AppDates._();

  static final DateFormat _day = DateFormat('d MMM yyyy', 'en_US');

  /// "23 Oct 2026"
  static String format(DateTime date) => _day.format(DateTime(date.year, date.month, date.day));

  static String formatOrDash(DateTime? date) => date == null ? '–' : format(date);
}
