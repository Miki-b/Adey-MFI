// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'payment_type_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PaymentTypeResponseCWProxy {
  PaymentTypeResponse cash(bool cash);

  PaymentTypeResponse description(String? description);

  PaymentTypeResponse id(int id);

  PaymentTypeResponse name(String name);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `PaymentTypeResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// PaymentTypeResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  PaymentTypeResponse call({
    bool cash,
    String? description,
    int id,
    String name,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfPaymentTypeResponse.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfPaymentTypeResponse.copyWith.fieldName(...)`
class _$PaymentTypeResponseCWProxyImpl implements _$PaymentTypeResponseCWProxy {
  const _$PaymentTypeResponseCWProxyImpl(this._value);

  final PaymentTypeResponse _value;

  @override
  PaymentTypeResponse cash(bool cash) => this(cash: cash);

  @override
  PaymentTypeResponse description(String? description) =>
      this(description: description);

  @override
  PaymentTypeResponse id(int id) => this(id: id);

  @override
  PaymentTypeResponse name(String name) => this(name: name);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `PaymentTypeResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// PaymentTypeResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  PaymentTypeResponse call({
    Object? cash = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
  }) {
    return PaymentTypeResponse(
      cash: cash == const $CopyWithPlaceholder()
          ? _value.cash
          // ignore: cast_nullable_to_non_nullable
          : cash as bool,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
    );
  }
}

extension $PaymentTypeResponseCopyWith on PaymentTypeResponse {
  /// Returns a callable class that can be used as follows: `instanceOfPaymentTypeResponse.copyWith(...)` or like so:`instanceOfPaymentTypeResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PaymentTypeResponseCWProxy get copyWith =>
      _$PaymentTypeResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PaymentTypeResponse _$PaymentTypeResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PaymentTypeResponse', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['cash', 'id', 'name']);
      final val = PaymentTypeResponse(
        cash: $checkedConvert('cash', (v) => v as bool),
        description: $checkedConvert('description', (v) => v as String?),
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        name: $checkedConvert('name', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$PaymentTypeResponseToJson(
  PaymentTypeResponse instance,
) => <String, dynamic>{
  'cash': instance.cash,
  'description': ?instance.description,
  'id': instance.id,
  'name': instance.name,
};
