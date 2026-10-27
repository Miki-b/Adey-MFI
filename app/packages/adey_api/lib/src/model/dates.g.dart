// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dates.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DatesCWProxy {
  Dates approvedOn(DateTime? approvedOn);

  Dates closedOn(DateTime? closedOn);

  Dates disbursedOn(DateTime? disbursedOn);

  Dates expectedDisbursement(DateTime? expectedDisbursement);

  Dates expectedMaturity(DateTime? expectedMaturity);

  Dates rejectedOn(DateTime? rejectedOn);

  Dates submittedOn(DateTime submittedOn);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Dates(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Dates(...).copyWith(id: 12, name: "My name")
  /// ````
  Dates call({
    DateTime? approvedOn,
    DateTime? closedOn,
    DateTime? disbursedOn,
    DateTime? expectedDisbursement,
    DateTime? expectedMaturity,
    DateTime? rejectedOn,
    DateTime submittedOn,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfDates.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfDates.copyWith.fieldName(...)`
class _$DatesCWProxyImpl implements _$DatesCWProxy {
  const _$DatesCWProxyImpl(this._value);

  final Dates _value;

  @override
  Dates approvedOn(DateTime? approvedOn) => this(approvedOn: approvedOn);

  @override
  Dates closedOn(DateTime? closedOn) => this(closedOn: closedOn);

  @override
  Dates disbursedOn(DateTime? disbursedOn) => this(disbursedOn: disbursedOn);

  @override
  Dates expectedDisbursement(DateTime? expectedDisbursement) =>
      this(expectedDisbursement: expectedDisbursement);

  @override
  Dates expectedMaturity(DateTime? expectedMaturity) =>
      this(expectedMaturity: expectedMaturity);

  @override
  Dates rejectedOn(DateTime? rejectedOn) => this(rejectedOn: rejectedOn);

  @override
  Dates submittedOn(DateTime submittedOn) => this(submittedOn: submittedOn);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Dates(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Dates(...).copyWith(id: 12, name: "My name")
  /// ````
  Dates call({
    Object? approvedOn = const $CopyWithPlaceholder(),
    Object? closedOn = const $CopyWithPlaceholder(),
    Object? disbursedOn = const $CopyWithPlaceholder(),
    Object? expectedDisbursement = const $CopyWithPlaceholder(),
    Object? expectedMaturity = const $CopyWithPlaceholder(),
    Object? rejectedOn = const $CopyWithPlaceholder(),
    Object? submittedOn = const $CopyWithPlaceholder(),
  }) {
    return Dates(
      approvedOn: approvedOn == const $CopyWithPlaceholder()
          ? _value.approvedOn
          // ignore: cast_nullable_to_non_nullable
          : approvedOn as DateTime?,
      closedOn: closedOn == const $CopyWithPlaceholder()
          ? _value.closedOn
          // ignore: cast_nullable_to_non_nullable
          : closedOn as DateTime?,
      disbursedOn: disbursedOn == const $CopyWithPlaceholder()
          ? _value.disbursedOn
          // ignore: cast_nullable_to_non_nullable
          : disbursedOn as DateTime?,
      expectedDisbursement: expectedDisbursement == const $CopyWithPlaceholder()
          ? _value.expectedDisbursement
          // ignore: cast_nullable_to_non_nullable
          : expectedDisbursement as DateTime?,
      expectedMaturity: expectedMaturity == const $CopyWithPlaceholder()
          ? _value.expectedMaturity
          // ignore: cast_nullable_to_non_nullable
          : expectedMaturity as DateTime?,
      rejectedOn: rejectedOn == const $CopyWithPlaceholder()
          ? _value.rejectedOn
          // ignore: cast_nullable_to_non_nullable
          : rejectedOn as DateTime?,
      submittedOn: submittedOn == const $CopyWithPlaceholder()
          ? _value.submittedOn
          // ignore: cast_nullable_to_non_nullable
          : submittedOn as DateTime,
    );
  }
}

extension $DatesCopyWith on Dates {
  /// Returns a callable class that can be used as follows: `instanceOfDates.copyWith(...)` or like so:`instanceOfDates.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DatesCWProxy get copyWith => _$DatesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Dates _$DatesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Dates', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['submittedOn']);
      final val = Dates(
        approvedOn: $checkedConvert(
          'approvedOn',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        closedOn: $checkedConvert(
          'closedOn',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        disbursedOn: $checkedConvert(
          'disbursedOn',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        expectedDisbursement: $checkedConvert(
          'expectedDisbursement',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        expectedMaturity: $checkedConvert(
          'expectedMaturity',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        rejectedOn: $checkedConvert(
          'rejectedOn',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        submittedOn: $checkedConvert(
          'submittedOn',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DatesToJson(Dates instance) => <String, dynamic>{
  'approvedOn': ?instance.approvedOn?.toIso8601String(),
  'closedOn': ?instance.closedOn?.toIso8601String(),
  'disbursedOn': ?instance.disbursedOn?.toIso8601String(),
  'expectedDisbursement': ?instance.expectedDisbursement?.toIso8601String(),
  'expectedMaturity': ?instance.expectedMaturity?.toIso8601String(),
  'rejectedOn': ?instance.rejectedOn?.toIso8601String(),
  'submittedOn': instance.submittedOn.toIso8601String(),
};
