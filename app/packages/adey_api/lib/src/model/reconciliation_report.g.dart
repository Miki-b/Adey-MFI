// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reconciliation_report.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReconciliationReportCWProxy {
  ReconciliationReport date(DateTime? date);

  ReconciliationReport findings(List<FindingResponse>? findings);

  ReconciliationReport runAt(DateTime? runAt);

  ReconciliationReport runId(int? runId);

  ReconciliationReport summary(Summary? summary);

  ReconciliationReport triggeredBy(String? triggeredBy);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ReconciliationReport(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ReconciliationReport(...).copyWith(id: 12, name: "My name")
  /// ````
  ReconciliationReport call({
    DateTime? date,
    List<FindingResponse>? findings,
    DateTime? runAt,
    int? runId,
    Summary? summary,
    String? triggeredBy,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfReconciliationReport.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfReconciliationReport.copyWith.fieldName(...)`
class _$ReconciliationReportCWProxyImpl
    implements _$ReconciliationReportCWProxy {
  const _$ReconciliationReportCWProxyImpl(this._value);

  final ReconciliationReport _value;

  @override
  ReconciliationReport date(DateTime? date) => this(date: date);

  @override
  ReconciliationReport findings(List<FindingResponse>? findings) =>
      this(findings: findings);

  @override
  ReconciliationReport runAt(DateTime? runAt) => this(runAt: runAt);

  @override
  ReconciliationReport runId(int? runId) => this(runId: runId);

  @override
  ReconciliationReport summary(Summary? summary) => this(summary: summary);

  @override
  ReconciliationReport triggeredBy(String? triggeredBy) =>
      this(triggeredBy: triggeredBy);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ReconciliationReport(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ReconciliationReport(...).copyWith(id: 12, name: "My name")
  /// ````
  ReconciliationReport call({
    Object? date = const $CopyWithPlaceholder(),
    Object? findings = const $CopyWithPlaceholder(),
    Object? runAt = const $CopyWithPlaceholder(),
    Object? runId = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? triggeredBy = const $CopyWithPlaceholder(),
  }) {
    return ReconciliationReport(
      date: date == const $CopyWithPlaceholder()
          ? _value.date
          // ignore: cast_nullable_to_non_nullable
          : date as DateTime?,
      findings: findings == const $CopyWithPlaceholder()
          ? _value.findings
          // ignore: cast_nullable_to_non_nullable
          : findings as List<FindingResponse>?,
      runAt: runAt == const $CopyWithPlaceholder()
          ? _value.runAt
          // ignore: cast_nullable_to_non_nullable
          : runAt as DateTime?,
      runId: runId == const $CopyWithPlaceholder()
          ? _value.runId
          // ignore: cast_nullable_to_non_nullable
          : runId as int?,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as Summary?,
      triggeredBy: triggeredBy == const $CopyWithPlaceholder()
          ? _value.triggeredBy
          // ignore: cast_nullable_to_non_nullable
          : triggeredBy as String?,
    );
  }
}

extension $ReconciliationReportCopyWith on ReconciliationReport {
  /// Returns a callable class that can be used as follows: `instanceOfReconciliationReport.copyWith(...)` or like so:`instanceOfReconciliationReport.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReconciliationReportCWProxy get copyWith =>
      _$ReconciliationReportCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReconciliationReport _$ReconciliationReportFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReconciliationReport', json, ($checkedConvert) {
  final val = ReconciliationReport(
    date: $checkedConvert(
      'date',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    findings: $checkedConvert(
      'findings',
      (v) => (v as List<dynamic>?)
          ?.map((e) => FindingResponse.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    runAt: $checkedConvert(
      'runAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    runId: $checkedConvert('runId', (v) => (v as num?)?.toInt()),
    summary: $checkedConvert(
      'summary',
      (v) => v == null ? null : Summary.fromJson(v as Map<String, dynamic>),
    ),
    triggeredBy: $checkedConvert('triggeredBy', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ReconciliationReportToJson(
  ReconciliationReport instance,
) => <String, dynamic>{
  'date': ?instance.date?.toIso8601String(),
  'findings': ?instance.findings?.map((e) => e.toJson()).toList(),
  'runAt': ?instance.runAt?.toIso8601String(),
  'runId': ?instance.runId,
  'summary': ?instance.summary?.toJson(),
  'triggeredBy': ?instance.triggeredBy,
};
