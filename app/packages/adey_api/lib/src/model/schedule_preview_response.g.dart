// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'schedule_preview_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SchedulePreviewResponseCWProxy {
  SchedulePreviewResponse currency(String? currency);

  SchedulePreviewResponse expectedDisbursementDate(
    DateTime? expectedDisbursementDate,
  );

  SchedulePreviewResponse installmentAmount(num? installmentAmount);

  SchedulePreviewResponse lastInstallmentAmount(num? lastInstallmentAmount);

  SchedulePreviewResponse numberOfRepayments(int numberOfRepayments);

  SchedulePreviewResponse periods(List<SchedulePeriod> periods);

  SchedulePreviewResponse principal(num? principal);

  SchedulePreviewResponse totalInterest(num totalInterest);

  SchedulePreviewResponse totalRepayable(num totalRepayable);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `SchedulePreviewResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// SchedulePreviewResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  SchedulePreviewResponse call({
    String? currency,
    DateTime? expectedDisbursementDate,
    num? installmentAmount,
    num? lastInstallmentAmount,
    int numberOfRepayments,
    List<SchedulePeriod> periods,
    num? principal,
    num totalInterest,
    num totalRepayable,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfSchedulePreviewResponse.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfSchedulePreviewResponse.copyWith.fieldName(...)`
class _$SchedulePreviewResponseCWProxyImpl
    implements _$SchedulePreviewResponseCWProxy {
  const _$SchedulePreviewResponseCWProxyImpl(this._value);

  final SchedulePreviewResponse _value;

  @override
  SchedulePreviewResponse currency(String? currency) =>
      this(currency: currency);

  @override
  SchedulePreviewResponse expectedDisbursementDate(
    DateTime? expectedDisbursementDate,
  ) => this(expectedDisbursementDate: expectedDisbursementDate);

  @override
  SchedulePreviewResponse installmentAmount(num? installmentAmount) =>
      this(installmentAmount: installmentAmount);

  @override
  SchedulePreviewResponse lastInstallmentAmount(num? lastInstallmentAmount) =>
      this(lastInstallmentAmount: lastInstallmentAmount);

  @override
  SchedulePreviewResponse numberOfRepayments(int numberOfRepayments) =>
      this(numberOfRepayments: numberOfRepayments);

  @override
  SchedulePreviewResponse periods(List<SchedulePeriod> periods) =>
      this(periods: periods);

  @override
  SchedulePreviewResponse principal(num? principal) =>
      this(principal: principal);

  @override
  SchedulePreviewResponse totalInterest(num totalInterest) =>
      this(totalInterest: totalInterest);

  @override
  SchedulePreviewResponse totalRepayable(num totalRepayable) =>
      this(totalRepayable: totalRepayable);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `SchedulePreviewResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// SchedulePreviewResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  SchedulePreviewResponse call({
    Object? currency = const $CopyWithPlaceholder(),
    Object? expectedDisbursementDate = const $CopyWithPlaceholder(),
    Object? installmentAmount = const $CopyWithPlaceholder(),
    Object? lastInstallmentAmount = const $CopyWithPlaceholder(),
    Object? numberOfRepayments = const $CopyWithPlaceholder(),
    Object? periods = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? totalInterest = const $CopyWithPlaceholder(),
    Object? totalRepayable = const $CopyWithPlaceholder(),
  }) {
    return SchedulePreviewResponse(
      currency: currency == const $CopyWithPlaceholder()
          ? _value.currency
          // ignore: cast_nullable_to_non_nullable
          : currency as String?,
      expectedDisbursementDate:
          expectedDisbursementDate == const $CopyWithPlaceholder()
          ? _value.expectedDisbursementDate
          // ignore: cast_nullable_to_non_nullable
          : expectedDisbursementDate as DateTime?,
      installmentAmount: installmentAmount == const $CopyWithPlaceholder()
          ? _value.installmentAmount
          // ignore: cast_nullable_to_non_nullable
          : installmentAmount as num?,
      lastInstallmentAmount:
          lastInstallmentAmount == const $CopyWithPlaceholder()
          ? _value.lastInstallmentAmount
          // ignore: cast_nullable_to_non_nullable
          : lastInstallmentAmount as num?,
      numberOfRepayments: numberOfRepayments == const $CopyWithPlaceholder()
          ? _value.numberOfRepayments
          // ignore: cast_nullable_to_non_nullable
          : numberOfRepayments as int,
      periods: periods == const $CopyWithPlaceholder()
          ? _value.periods
          // ignore: cast_nullable_to_non_nullable
          : periods as List<SchedulePeriod>,
      principal: principal == const $CopyWithPlaceholder()
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as num?,
      totalInterest: totalInterest == const $CopyWithPlaceholder()
          ? _value.totalInterest
          // ignore: cast_nullable_to_non_nullable
          : totalInterest as num,
      totalRepayable: totalRepayable == const $CopyWithPlaceholder()
          ? _value.totalRepayable
          // ignore: cast_nullable_to_non_nullable
          : totalRepayable as num,
    );
  }
}

extension $SchedulePreviewResponseCopyWith on SchedulePreviewResponse {
  /// Returns a callable class that can be used as follows: `instanceOfSchedulePreviewResponse.copyWith(...)` or like so:`instanceOfSchedulePreviewResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SchedulePreviewResponseCWProxy get copyWith =>
      _$SchedulePreviewResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SchedulePreviewResponse _$SchedulePreviewResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SchedulePreviewResponse', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'numberOfRepayments',
      'periods',
      'totalInterest',
      'totalRepayable',
    ],
  );
  final val = SchedulePreviewResponse(
    currency: $checkedConvert('currency', (v) => v as String?),
    expectedDisbursementDate: $checkedConvert(
      'expectedDisbursementDate',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    installmentAmount: $checkedConvert('installmentAmount', (v) => v as num?),
    lastInstallmentAmount: $checkedConvert(
      'lastInstallmentAmount',
      (v) => v as num?,
    ),
    numberOfRepayments: $checkedConvert(
      'numberOfRepayments',
      (v) => (v as num).toInt(),
    ),
    periods: $checkedConvert(
      'periods',
      (v) => (v as List<dynamic>)
          .map((e) => SchedulePeriod.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    principal: $checkedConvert('principal', (v) => v as num?),
    totalInterest: $checkedConvert('totalInterest', (v) => v as num),
    totalRepayable: $checkedConvert('totalRepayable', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$SchedulePreviewResponseToJson(
  SchedulePreviewResponse instance,
) => <String, dynamic>{
  'currency': ?instance.currency,
  'expectedDisbursementDate': ?instance.expectedDisbursementDate
      ?.toIso8601String(),
  'installmentAmount': ?instance.installmentAmount,
  'lastInstallmentAmount': ?instance.lastInstallmentAmount,
  'numberOfRepayments': instance.numberOfRepayments,
  'periods': instance.periods.map((e) => e.toJson()).toList(),
  'principal': ?instance.principal,
  'totalInterest': instance.totalInterest,
  'totalRepayable': instance.totalRepayable,
};
