// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SummaryCWProxy {
  Summary amountMismatch(int? amountMismatch);

  Summary fineractTotal(num? fineractTotal);

  Summary matched(int? matched);

  Summary matchedTotal(num? matchedTotal);

  Summary paidNotPosted(int? paidNotPosted);

  Summary postedNotPaid(int? postedNotPaid);

  Summary providerTotal(num? providerTotal);

  Summary unresolved(int? unresolved);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Summary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Summary(...).copyWith(id: 12, name: "My name")
  /// ````
  Summary call({
    int? amountMismatch,
    num? fineractTotal,
    int? matched,
    num? matchedTotal,
    int? paidNotPosted,
    int? postedNotPaid,
    num? providerTotal,
    int? unresolved,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfSummary.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfSummary.copyWith.fieldName(...)`
class _$SummaryCWProxyImpl implements _$SummaryCWProxy {
  const _$SummaryCWProxyImpl(this._value);

  final Summary _value;

  @override
  Summary amountMismatch(int? amountMismatch) =>
      this(amountMismatch: amountMismatch);

  @override
  Summary fineractTotal(num? fineractTotal) =>
      this(fineractTotal: fineractTotal);

  @override
  Summary matched(int? matched) => this(matched: matched);

  @override
  Summary matchedTotal(num? matchedTotal) => this(matchedTotal: matchedTotal);

  @override
  Summary paidNotPosted(int? paidNotPosted) =>
      this(paidNotPosted: paidNotPosted);

  @override
  Summary postedNotPaid(int? postedNotPaid) =>
      this(postedNotPaid: postedNotPaid);

  @override
  Summary providerTotal(num? providerTotal) =>
      this(providerTotal: providerTotal);

  @override
  Summary unresolved(int? unresolved) => this(unresolved: unresolved);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Summary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Summary(...).copyWith(id: 12, name: "My name")
  /// ````
  Summary call({
    Object? amountMismatch = const $CopyWithPlaceholder(),
    Object? fineractTotal = const $CopyWithPlaceholder(),
    Object? matched = const $CopyWithPlaceholder(),
    Object? matchedTotal = const $CopyWithPlaceholder(),
    Object? paidNotPosted = const $CopyWithPlaceholder(),
    Object? postedNotPaid = const $CopyWithPlaceholder(),
    Object? providerTotal = const $CopyWithPlaceholder(),
    Object? unresolved = const $CopyWithPlaceholder(),
  }) {
    return Summary(
      amountMismatch: amountMismatch == const $CopyWithPlaceholder()
          ? _value.amountMismatch
          // ignore: cast_nullable_to_non_nullable
          : amountMismatch as int?,
      fineractTotal: fineractTotal == const $CopyWithPlaceholder()
          ? _value.fineractTotal
          // ignore: cast_nullable_to_non_nullable
          : fineractTotal as num?,
      matched: matched == const $CopyWithPlaceholder()
          ? _value.matched
          // ignore: cast_nullable_to_non_nullable
          : matched as int?,
      matchedTotal: matchedTotal == const $CopyWithPlaceholder()
          ? _value.matchedTotal
          // ignore: cast_nullable_to_non_nullable
          : matchedTotal as num?,
      paidNotPosted: paidNotPosted == const $CopyWithPlaceholder()
          ? _value.paidNotPosted
          // ignore: cast_nullable_to_non_nullable
          : paidNotPosted as int?,
      postedNotPaid: postedNotPaid == const $CopyWithPlaceholder()
          ? _value.postedNotPaid
          // ignore: cast_nullable_to_non_nullable
          : postedNotPaid as int?,
      providerTotal: providerTotal == const $CopyWithPlaceholder()
          ? _value.providerTotal
          // ignore: cast_nullable_to_non_nullable
          : providerTotal as num?,
      unresolved: unresolved == const $CopyWithPlaceholder()
          ? _value.unresolved
          // ignore: cast_nullable_to_non_nullable
          : unresolved as int?,
    );
  }
}

extension $SummaryCopyWith on Summary {
  /// Returns a callable class that can be used as follows: `instanceOfSummary.copyWith(...)` or like so:`instanceOfSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SummaryCWProxy get copyWith => _$SummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Summary _$SummaryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Summary', json, ($checkedConvert) {
      final val = Summary(
        amountMismatch: $checkedConvert(
          'amountMismatch',
          (v) => (v as num?)?.toInt(),
        ),
        fineractTotal: $checkedConvert('fineractTotal', (v) => v as num?),
        matched: $checkedConvert('matched', (v) => (v as num?)?.toInt()),
        matchedTotal: $checkedConvert('matchedTotal', (v) => v as num?),
        paidNotPosted: $checkedConvert(
          'paidNotPosted',
          (v) => (v as num?)?.toInt(),
        ),
        postedNotPaid: $checkedConvert(
          'postedNotPaid',
          (v) => (v as num?)?.toInt(),
        ),
        providerTotal: $checkedConvert('providerTotal', (v) => v as num?),
        unresolved: $checkedConvert('unresolved', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SummaryToJson(Summary instance) => <String, dynamic>{
  'amountMismatch': ?instance.amountMismatch,
  'fineractTotal': ?instance.fineractTotal,
  'matched': ?instance.matched,
  'matchedTotal': ?instance.matchedTotal,
  'paidNotPosted': ?instance.paidNotPosted,
  'postedNotPaid': ?instance.postedNotPaid,
  'providerTotal': ?instance.providerTotal,
  'unresolved': ?instance.unresolved,
};
