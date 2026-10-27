// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reconciliation_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ReconciliationSummaryCWProxy {
  ReconciliationSummary amountMismatch(int amountMismatch);

  ReconciliationSummary fineractTotal(num fineractTotal);

  ReconciliationSummary matched(int matched);

  ReconciliationSummary matchedTotal(num matchedTotal);

  ReconciliationSummary paidNotPosted(int paidNotPosted);

  ReconciliationSummary postedNotPaid(int postedNotPaid);

  ReconciliationSummary providerTotal(num providerTotal);

  ReconciliationSummary unresolved(int unresolved);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ReconciliationSummary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ReconciliationSummary(...).copyWith(id: 12, name: "My name")
  /// ````
  ReconciliationSummary call({
    int amountMismatch,
    num fineractTotal,
    int matched,
    num matchedTotal,
    int paidNotPosted,
    int postedNotPaid,
    num providerTotal,
    int unresolved,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfReconciliationSummary.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfReconciliationSummary.copyWith.fieldName(...)`
class _$ReconciliationSummaryCWProxyImpl
    implements _$ReconciliationSummaryCWProxy {
  const _$ReconciliationSummaryCWProxyImpl(this._value);

  final ReconciliationSummary _value;

  @override
  ReconciliationSummary amountMismatch(int amountMismatch) =>
      this(amountMismatch: amountMismatch);

  @override
  ReconciliationSummary fineractTotal(num fineractTotal) =>
      this(fineractTotal: fineractTotal);

  @override
  ReconciliationSummary matched(int matched) => this(matched: matched);

  @override
  ReconciliationSummary matchedTotal(num matchedTotal) =>
      this(matchedTotal: matchedTotal);

  @override
  ReconciliationSummary paidNotPosted(int paidNotPosted) =>
      this(paidNotPosted: paidNotPosted);

  @override
  ReconciliationSummary postedNotPaid(int postedNotPaid) =>
      this(postedNotPaid: postedNotPaid);

  @override
  ReconciliationSummary providerTotal(num providerTotal) =>
      this(providerTotal: providerTotal);

  @override
  ReconciliationSummary unresolved(int unresolved) =>
      this(unresolved: unresolved);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ReconciliationSummary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ReconciliationSummary(...).copyWith(id: 12, name: "My name")
  /// ````
  ReconciliationSummary call({
    Object? amountMismatch = const $CopyWithPlaceholder(),
    Object? fineractTotal = const $CopyWithPlaceholder(),
    Object? matched = const $CopyWithPlaceholder(),
    Object? matchedTotal = const $CopyWithPlaceholder(),
    Object? paidNotPosted = const $CopyWithPlaceholder(),
    Object? postedNotPaid = const $CopyWithPlaceholder(),
    Object? providerTotal = const $CopyWithPlaceholder(),
    Object? unresolved = const $CopyWithPlaceholder(),
  }) {
    return ReconciliationSummary(
      amountMismatch: amountMismatch == const $CopyWithPlaceholder()
          ? _value.amountMismatch
          // ignore: cast_nullable_to_non_nullable
          : amountMismatch as int,
      fineractTotal: fineractTotal == const $CopyWithPlaceholder()
          ? _value.fineractTotal
          // ignore: cast_nullable_to_non_nullable
          : fineractTotal as num,
      matched: matched == const $CopyWithPlaceholder()
          ? _value.matched
          // ignore: cast_nullable_to_non_nullable
          : matched as int,
      matchedTotal: matchedTotal == const $CopyWithPlaceholder()
          ? _value.matchedTotal
          // ignore: cast_nullable_to_non_nullable
          : matchedTotal as num,
      paidNotPosted: paidNotPosted == const $CopyWithPlaceholder()
          ? _value.paidNotPosted
          // ignore: cast_nullable_to_non_nullable
          : paidNotPosted as int,
      postedNotPaid: postedNotPaid == const $CopyWithPlaceholder()
          ? _value.postedNotPaid
          // ignore: cast_nullable_to_non_nullable
          : postedNotPaid as int,
      providerTotal: providerTotal == const $CopyWithPlaceholder()
          ? _value.providerTotal
          // ignore: cast_nullable_to_non_nullable
          : providerTotal as num,
      unresolved: unresolved == const $CopyWithPlaceholder()
          ? _value.unresolved
          // ignore: cast_nullable_to_non_nullable
          : unresolved as int,
    );
  }
}

extension $ReconciliationSummaryCopyWith on ReconciliationSummary {
  /// Returns a callable class that can be used as follows: `instanceOfReconciliationSummary.copyWith(...)` or like so:`instanceOfReconciliationSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ReconciliationSummaryCWProxy get copyWith =>
      _$ReconciliationSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReconciliationSummary _$ReconciliationSummaryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ReconciliationSummary', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'amountMismatch',
      'fineractTotal',
      'matched',
      'matchedTotal',
      'paidNotPosted',
      'postedNotPaid',
      'providerTotal',
      'unresolved',
    ],
  );
  final val = ReconciliationSummary(
    amountMismatch: $checkedConvert(
      'amountMismatch',
      (v) => (v as num).toInt(),
    ),
    fineractTotal: $checkedConvert('fineractTotal', (v) => v as num),
    matched: $checkedConvert('matched', (v) => (v as num).toInt()),
    matchedTotal: $checkedConvert('matchedTotal', (v) => v as num),
    paidNotPosted: $checkedConvert('paidNotPosted', (v) => (v as num).toInt()),
    postedNotPaid: $checkedConvert('postedNotPaid', (v) => (v as num).toInt()),
    providerTotal: $checkedConvert('providerTotal', (v) => v as num),
    unresolved: $checkedConvert('unresolved', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$ReconciliationSummaryToJson(
  ReconciliationSummary instance,
) => <String, dynamic>{
  'amountMismatch': instance.amountMismatch,
  'fineractTotal': instance.fineractTotal,
  'matched': instance.matched,
  'matchedTotal': instance.matchedTotal,
  'paidNotPosted': instance.paidNotPosted,
  'postedNotPaid': instance.postedNotPaid,
  'providerTotal': instance.providerTotal,
  'unresolved': instance.unresolved,
};
