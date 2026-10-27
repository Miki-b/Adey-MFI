// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'loan_totals.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LoanTotalsCWProxy {
  LoanTotals inArrears(bool inArrears);

  LoanTotals interestCharged(num interestCharged);

  LoanTotals interestOutstanding(num interestOutstanding);

  LoanTotals interestPaid(num interestPaid);

  LoanTotals overdueSince(DateTime? overdueSince);

  LoanTotals principalDisbursed(num principalDisbursed);

  LoanTotals principalOutstanding(num principalOutstanding);

  LoanTotals principalPaid(num principalPaid);

  LoanTotals totalOutstanding(num totalOutstanding);

  LoanTotals totalOverdue(num totalOverdue);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `LoanTotals(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// LoanTotals(...).copyWith(id: 12, name: "My name")
  /// ````
  LoanTotals call({
    bool inArrears,
    num interestCharged,
    num interestOutstanding,
    num interestPaid,
    DateTime? overdueSince,
    num principalDisbursed,
    num principalOutstanding,
    num principalPaid,
    num totalOutstanding,
    num totalOverdue,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfLoanTotals.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfLoanTotals.copyWith.fieldName(...)`
class _$LoanTotalsCWProxyImpl implements _$LoanTotalsCWProxy {
  const _$LoanTotalsCWProxyImpl(this._value);

  final LoanTotals _value;

  @override
  LoanTotals inArrears(bool inArrears) => this(inArrears: inArrears);

  @override
  LoanTotals interestCharged(num interestCharged) =>
      this(interestCharged: interestCharged);

  @override
  LoanTotals interestOutstanding(num interestOutstanding) =>
      this(interestOutstanding: interestOutstanding);

  @override
  LoanTotals interestPaid(num interestPaid) => this(interestPaid: interestPaid);

  @override
  LoanTotals overdueSince(DateTime? overdueSince) =>
      this(overdueSince: overdueSince);

  @override
  LoanTotals principalDisbursed(num principalDisbursed) =>
      this(principalDisbursed: principalDisbursed);

  @override
  LoanTotals principalOutstanding(num principalOutstanding) =>
      this(principalOutstanding: principalOutstanding);

  @override
  LoanTotals principalPaid(num principalPaid) =>
      this(principalPaid: principalPaid);

  @override
  LoanTotals totalOutstanding(num totalOutstanding) =>
      this(totalOutstanding: totalOutstanding);

  @override
  LoanTotals totalOverdue(num totalOverdue) => this(totalOverdue: totalOverdue);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `LoanTotals(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// LoanTotals(...).copyWith(id: 12, name: "My name")
  /// ````
  LoanTotals call({
    Object? inArrears = const $CopyWithPlaceholder(),
    Object? interestCharged = const $CopyWithPlaceholder(),
    Object? interestOutstanding = const $CopyWithPlaceholder(),
    Object? interestPaid = const $CopyWithPlaceholder(),
    Object? overdueSince = const $CopyWithPlaceholder(),
    Object? principalDisbursed = const $CopyWithPlaceholder(),
    Object? principalOutstanding = const $CopyWithPlaceholder(),
    Object? principalPaid = const $CopyWithPlaceholder(),
    Object? totalOutstanding = const $CopyWithPlaceholder(),
    Object? totalOverdue = const $CopyWithPlaceholder(),
  }) {
    return LoanTotals(
      inArrears: inArrears == const $CopyWithPlaceholder()
          ? _value.inArrears
          // ignore: cast_nullable_to_non_nullable
          : inArrears as bool,
      interestCharged: interestCharged == const $CopyWithPlaceholder()
          ? _value.interestCharged
          // ignore: cast_nullable_to_non_nullable
          : interestCharged as num,
      interestOutstanding: interestOutstanding == const $CopyWithPlaceholder()
          ? _value.interestOutstanding
          // ignore: cast_nullable_to_non_nullable
          : interestOutstanding as num,
      interestPaid: interestPaid == const $CopyWithPlaceholder()
          ? _value.interestPaid
          // ignore: cast_nullable_to_non_nullable
          : interestPaid as num,
      overdueSince: overdueSince == const $CopyWithPlaceholder()
          ? _value.overdueSince
          // ignore: cast_nullable_to_non_nullable
          : overdueSince as DateTime?,
      principalDisbursed: principalDisbursed == const $CopyWithPlaceholder()
          ? _value.principalDisbursed
          // ignore: cast_nullable_to_non_nullable
          : principalDisbursed as num,
      principalOutstanding: principalOutstanding == const $CopyWithPlaceholder()
          ? _value.principalOutstanding
          // ignore: cast_nullable_to_non_nullable
          : principalOutstanding as num,
      principalPaid: principalPaid == const $CopyWithPlaceholder()
          ? _value.principalPaid
          // ignore: cast_nullable_to_non_nullable
          : principalPaid as num,
      totalOutstanding: totalOutstanding == const $CopyWithPlaceholder()
          ? _value.totalOutstanding
          // ignore: cast_nullable_to_non_nullable
          : totalOutstanding as num,
      totalOverdue: totalOverdue == const $CopyWithPlaceholder()
          ? _value.totalOverdue
          // ignore: cast_nullable_to_non_nullable
          : totalOverdue as num,
    );
  }
}

extension $LoanTotalsCopyWith on LoanTotals {
  /// Returns a callable class that can be used as follows: `instanceOfLoanTotals.copyWith(...)` or like so:`instanceOfLoanTotals.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LoanTotalsCWProxy get copyWith => _$LoanTotalsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LoanTotals _$LoanTotalsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LoanTotals', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'inArrears',
          'interestCharged',
          'interestOutstanding',
          'interestPaid',
          'principalDisbursed',
          'principalOutstanding',
          'principalPaid',
          'totalOutstanding',
          'totalOverdue',
        ],
      );
      final val = LoanTotals(
        inArrears: $checkedConvert('inArrears', (v) => v as bool),
        interestCharged: $checkedConvert('interestCharged', (v) => v as num),
        interestOutstanding: $checkedConvert(
          'interestOutstanding',
          (v) => v as num,
        ),
        interestPaid: $checkedConvert('interestPaid', (v) => v as num),
        overdueSince: $checkedConvert(
          'overdueSince',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        principalDisbursed: $checkedConvert(
          'principalDisbursed',
          (v) => v as num,
        ),
        principalOutstanding: $checkedConvert(
          'principalOutstanding',
          (v) => v as num,
        ),
        principalPaid: $checkedConvert('principalPaid', (v) => v as num),
        totalOutstanding: $checkedConvert('totalOutstanding', (v) => v as num),
        totalOverdue: $checkedConvert('totalOverdue', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$LoanTotalsToJson(LoanTotals instance) =>
    <String, dynamic>{
      'inArrears': instance.inArrears,
      'interestCharged': instance.interestCharged,
      'interestOutstanding': instance.interestOutstanding,
      'interestPaid': instance.interestPaid,
      'overdueSince': ?instance.overdueSince?.toIso8601String(),
      'principalDisbursed': instance.principalDisbursed,
      'principalOutstanding': instance.principalOutstanding,
      'principalPaid': instance.principalPaid,
      'totalOutstanding': instance.totalOutstanding,
      'totalOverdue': instance.totalOverdue,
    };
