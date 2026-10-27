// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'schedule_period.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SchedulePeriodCWProxy {
  SchedulePeriod balanceAfter(num balanceAfter);

  SchedulePeriod dueDate(DateTime dueDate);

  SchedulePeriod interest(num interest);

  SchedulePeriod number(int number);

  SchedulePeriod principal(num principal);

  SchedulePeriod total(num total);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `SchedulePeriod(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// SchedulePeriod(...).copyWith(id: 12, name: "My name")
  /// ````
  SchedulePeriod call({
    num balanceAfter,
    DateTime dueDate,
    num interest,
    int number,
    num principal,
    num total,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfSchedulePeriod.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfSchedulePeriod.copyWith.fieldName(...)`
class _$SchedulePeriodCWProxyImpl implements _$SchedulePeriodCWProxy {
  const _$SchedulePeriodCWProxyImpl(this._value);

  final SchedulePeriod _value;

  @override
  SchedulePeriod balanceAfter(num balanceAfter) =>
      this(balanceAfter: balanceAfter);

  @override
  SchedulePeriod dueDate(DateTime dueDate) => this(dueDate: dueDate);

  @override
  SchedulePeriod interest(num interest) => this(interest: interest);

  @override
  SchedulePeriod number(int number) => this(number: number);

  @override
  SchedulePeriod principal(num principal) => this(principal: principal);

  @override
  SchedulePeriod total(num total) => this(total: total);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `SchedulePeriod(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// SchedulePeriod(...).copyWith(id: 12, name: "My name")
  /// ````
  SchedulePeriod call({
    Object? balanceAfter = const $CopyWithPlaceholder(),
    Object? dueDate = const $CopyWithPlaceholder(),
    Object? interest = const $CopyWithPlaceholder(),
    Object? number = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? total = const $CopyWithPlaceholder(),
  }) {
    return SchedulePeriod(
      balanceAfter: balanceAfter == const $CopyWithPlaceholder()
          ? _value.balanceAfter
          // ignore: cast_nullable_to_non_nullable
          : balanceAfter as num,
      dueDate: dueDate == const $CopyWithPlaceholder()
          ? _value.dueDate
          // ignore: cast_nullable_to_non_nullable
          : dueDate as DateTime,
      interest: interest == const $CopyWithPlaceholder()
          ? _value.interest
          // ignore: cast_nullable_to_non_nullable
          : interest as num,
      number: number == const $CopyWithPlaceholder()
          ? _value.number
          // ignore: cast_nullable_to_non_nullable
          : number as int,
      principal: principal == const $CopyWithPlaceholder()
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as num,
      total: total == const $CopyWithPlaceholder()
          ? _value.total
          // ignore: cast_nullable_to_non_nullable
          : total as num,
    );
  }
}

extension $SchedulePeriodCopyWith on SchedulePeriod {
  /// Returns a callable class that can be used as follows: `instanceOfSchedulePeriod.copyWith(...)` or like so:`instanceOfSchedulePeriod.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SchedulePeriodCWProxy get copyWith => _$SchedulePeriodCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SchedulePeriod _$SchedulePeriodFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SchedulePeriod', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'balanceAfter',
          'dueDate',
          'interest',
          'number',
          'principal',
          'total',
        ],
      );
      final val = SchedulePeriod(
        balanceAfter: $checkedConvert('balanceAfter', (v) => v as num),
        dueDate: $checkedConvert('dueDate', (v) => DateTime.parse(v as String)),
        interest: $checkedConvert('interest', (v) => v as num),
        number: $checkedConvert('number', (v) => (v as num).toInt()),
        principal: $checkedConvert('principal', (v) => v as num),
        total: $checkedConvert('total', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$SchedulePeriodToJson(SchedulePeriod instance) =>
    <String, dynamic>{
      'balanceAfter': instance.balanceAfter,
      'dueDate': instance.dueDate.toIso8601String(),
      'interest': instance.interest,
      'number': instance.number,
      'principal': instance.principal,
      'total': instance.total,
    };
