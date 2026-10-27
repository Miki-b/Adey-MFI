// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'installment.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InstallmentCWProxy {
  Installment balanceAfter(num balanceAfter);

  Installment complete(bool complete);

  Installment dueDate(DateTime dueDate);

  Installment fees(num fees);

  Installment interest(num interest);

  Installment number(int number);

  Installment outstanding(num outstanding);

  Installment paid(num paid);

  Installment penalties(num penalties);

  Installment principal(num principal);

  Installment total(num total);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Installment(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Installment(...).copyWith(id: 12, name: "My name")
  /// ````
  Installment call({
    num balanceAfter,
    bool complete,
    DateTime dueDate,
    num fees,
    num interest,
    int number,
    num outstanding,
    num paid,
    num penalties,
    num principal,
    num total,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfInstallment.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfInstallment.copyWith.fieldName(...)`
class _$InstallmentCWProxyImpl implements _$InstallmentCWProxy {
  const _$InstallmentCWProxyImpl(this._value);

  final Installment _value;

  @override
  Installment balanceAfter(num balanceAfter) =>
      this(balanceAfter: balanceAfter);

  @override
  Installment complete(bool complete) => this(complete: complete);

  @override
  Installment dueDate(DateTime dueDate) => this(dueDate: dueDate);

  @override
  Installment fees(num fees) => this(fees: fees);

  @override
  Installment interest(num interest) => this(interest: interest);

  @override
  Installment number(int number) => this(number: number);

  @override
  Installment outstanding(num outstanding) => this(outstanding: outstanding);

  @override
  Installment paid(num paid) => this(paid: paid);

  @override
  Installment penalties(num penalties) => this(penalties: penalties);

  @override
  Installment principal(num principal) => this(principal: principal);

  @override
  Installment total(num total) => this(total: total);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Installment(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Installment(...).copyWith(id: 12, name: "My name")
  /// ````
  Installment call({
    Object? balanceAfter = const $CopyWithPlaceholder(),
    Object? complete = const $CopyWithPlaceholder(),
    Object? dueDate = const $CopyWithPlaceholder(),
    Object? fees = const $CopyWithPlaceholder(),
    Object? interest = const $CopyWithPlaceholder(),
    Object? number = const $CopyWithPlaceholder(),
    Object? outstanding = const $CopyWithPlaceholder(),
    Object? paid = const $CopyWithPlaceholder(),
    Object? penalties = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? total = const $CopyWithPlaceholder(),
  }) {
    return Installment(
      balanceAfter: balanceAfter == const $CopyWithPlaceholder()
          ? _value.balanceAfter
          // ignore: cast_nullable_to_non_nullable
          : balanceAfter as num,
      complete: complete == const $CopyWithPlaceholder()
          ? _value.complete
          // ignore: cast_nullable_to_non_nullable
          : complete as bool,
      dueDate: dueDate == const $CopyWithPlaceholder()
          ? _value.dueDate
          // ignore: cast_nullable_to_non_nullable
          : dueDate as DateTime,
      fees: fees == const $CopyWithPlaceholder()
          ? _value.fees
          // ignore: cast_nullable_to_non_nullable
          : fees as num,
      interest: interest == const $CopyWithPlaceholder()
          ? _value.interest
          // ignore: cast_nullable_to_non_nullable
          : interest as num,
      number: number == const $CopyWithPlaceholder()
          ? _value.number
          // ignore: cast_nullable_to_non_nullable
          : number as int,
      outstanding: outstanding == const $CopyWithPlaceholder()
          ? _value.outstanding
          // ignore: cast_nullable_to_non_nullable
          : outstanding as num,
      paid: paid == const $CopyWithPlaceholder()
          ? _value.paid
          // ignore: cast_nullable_to_non_nullable
          : paid as num,
      penalties: penalties == const $CopyWithPlaceholder()
          ? _value.penalties
          // ignore: cast_nullable_to_non_nullable
          : penalties as num,
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

extension $InstallmentCopyWith on Installment {
  /// Returns a callable class that can be used as follows: `instanceOfInstallment.copyWith(...)` or like so:`instanceOfInstallment.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InstallmentCWProxy get copyWith => _$InstallmentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Installment _$InstallmentFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Installment', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'balanceAfter',
          'complete',
          'dueDate',
          'fees',
          'interest',
          'number',
          'outstanding',
          'paid',
          'penalties',
          'principal',
          'total',
        ],
      );
      final val = Installment(
        balanceAfter: $checkedConvert('balanceAfter', (v) => v as num),
        complete: $checkedConvert('complete', (v) => v as bool),
        dueDate: $checkedConvert('dueDate', (v) => DateTime.parse(v as String)),
        fees: $checkedConvert('fees', (v) => v as num),
        interest: $checkedConvert('interest', (v) => v as num),
        number: $checkedConvert('number', (v) => (v as num).toInt()),
        outstanding: $checkedConvert('outstanding', (v) => v as num),
        paid: $checkedConvert('paid', (v) => v as num),
        penalties: $checkedConvert('penalties', (v) => v as num),
        principal: $checkedConvert('principal', (v) => v as num),
        total: $checkedConvert('total', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$InstallmentToJson(Installment instance) =>
    <String, dynamic>{
      'balanceAfter': instance.balanceAfter,
      'complete': instance.complete,
      'dueDate': instance.dueDate.toIso8601String(),
      'fees': instance.fees,
      'interest': instance.interest,
      'number': instance.number,
      'outstanding': instance.outstanding,
      'paid': instance.paid,
      'penalties': instance.penalties,
      'principal': instance.principal,
      'total': instance.total,
    };
