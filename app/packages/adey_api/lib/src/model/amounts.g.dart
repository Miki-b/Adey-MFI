// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'amounts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AmountsCWProxy {
  Amounts currency(String currency);

  Amounts principalOutstanding(num principalOutstanding);

  Amounts totalOverdue(num totalOverdue);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Amounts(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Amounts(...).copyWith(id: 12, name: "My name")
  /// ````
  Amounts call({String currency, num principalOutstanding, num totalOverdue});
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfAmounts.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfAmounts.copyWith.fieldName(...)`
class _$AmountsCWProxyImpl implements _$AmountsCWProxy {
  const _$AmountsCWProxyImpl(this._value);

  final Amounts _value;

  @override
  Amounts currency(String currency) => this(currency: currency);

  @override
  Amounts principalOutstanding(num principalOutstanding) =>
      this(principalOutstanding: principalOutstanding);

  @override
  Amounts totalOverdue(num totalOverdue) => this(totalOverdue: totalOverdue);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Amounts(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Amounts(...).copyWith(id: 12, name: "My name")
  /// ````
  Amounts call({
    Object? currency = const $CopyWithPlaceholder(),
    Object? principalOutstanding = const $CopyWithPlaceholder(),
    Object? totalOverdue = const $CopyWithPlaceholder(),
  }) {
    return Amounts(
      currency: currency == const $CopyWithPlaceholder()
          ? _value.currency
          // ignore: cast_nullable_to_non_nullable
          : currency as String,
      principalOutstanding: principalOutstanding == const $CopyWithPlaceholder()
          ? _value.principalOutstanding
          // ignore: cast_nullable_to_non_nullable
          : principalOutstanding as num,
      totalOverdue: totalOverdue == const $CopyWithPlaceholder()
          ? _value.totalOverdue
          // ignore: cast_nullable_to_non_nullable
          : totalOverdue as num,
    );
  }
}

extension $AmountsCopyWith on Amounts {
  /// Returns a callable class that can be used as follows: `instanceOfAmounts.copyWith(...)` or like so:`instanceOfAmounts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AmountsCWProxy get copyWith => _$AmountsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Amounts _$AmountsFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Amounts',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const ['currency', 'principalOutstanding', 'totalOverdue'],
    );
    final val = Amounts(
      currency: $checkedConvert('currency', (v) => v as String),
      principalOutstanding: $checkedConvert(
        'principalOutstanding',
        (v) => v as num,
      ),
      totalOverdue: $checkedConvert('totalOverdue', (v) => v as num),
    );
    return val;
  },
);

Map<String, dynamic> _$AmountsToJson(Amounts instance) => <String, dynamic>{
  'currency': instance.currency,
  'principalOutstanding': instance.principalOutstanding,
  'totalOverdue': instance.totalOverdue,
};
