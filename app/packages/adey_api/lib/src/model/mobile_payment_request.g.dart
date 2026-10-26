// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mobile_payment_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MobilePaymentRequestCWProxy {
  MobilePaymentRequest amount(num amount);

  MobilePaymentRequest method(MobilePaymentRequestMethodEnum? method);

  MobilePaymentRequest phone(String phone);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `MobilePaymentRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// MobilePaymentRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  MobilePaymentRequest call({
    num amount,
    MobilePaymentRequestMethodEnum? method,
    String phone,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfMobilePaymentRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfMobilePaymentRequest.copyWith.fieldName(...)`
class _$MobilePaymentRequestCWProxyImpl
    implements _$MobilePaymentRequestCWProxy {
  const _$MobilePaymentRequestCWProxyImpl(this._value);

  final MobilePaymentRequest _value;

  @override
  MobilePaymentRequest amount(num amount) => this(amount: amount);

  @override
  MobilePaymentRequest method(MobilePaymentRequestMethodEnum? method) =>
      this(method: method);

  @override
  MobilePaymentRequest phone(String phone) => this(phone: phone);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `MobilePaymentRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// MobilePaymentRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  MobilePaymentRequest call({
    Object? amount = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? phone = const $CopyWithPlaceholder(),
  }) {
    return MobilePaymentRequest(
      amount: amount == const $CopyWithPlaceholder()
          ? _value.amount
          // ignore: cast_nullable_to_non_nullable
          : amount as num,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as MobilePaymentRequestMethodEnum?,
      phone: phone == const $CopyWithPlaceholder()
          ? _value.phone
          // ignore: cast_nullable_to_non_nullable
          : phone as String,
    );
  }
}

extension $MobilePaymentRequestCopyWith on MobilePaymentRequest {
  /// Returns a callable class that can be used as follows: `instanceOfMobilePaymentRequest.copyWith(...)` or like so:`instanceOfMobilePaymentRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MobilePaymentRequestCWProxy get copyWith =>
      _$MobilePaymentRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MobilePaymentRequest _$MobilePaymentRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MobilePaymentRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['amount', 'phone']);
  final val = MobilePaymentRequest(
    amount: $checkedConvert('amount', (v) => v as num),
    method: $checkedConvert(
      'method',
      (v) => $enumDecodeNullable(
        _$MobilePaymentRequestMethodEnumEnumMap,
        v,
        unknownValue: MobilePaymentRequestMethodEnum.unknownDefaultOpenApi,
      ),
    ),
    phone: $checkedConvert('phone', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$MobilePaymentRequestToJson(
  MobilePaymentRequest instance,
) => <String, dynamic>{
  'amount': instance.amount,
  'method': ?_$MobilePaymentRequestMethodEnumEnumMap[instance.method],
  'phone': instance.phone,
};

const _$MobilePaymentRequestMethodEnumEnumMap = {
  MobilePaymentRequestMethodEnum.TELEBIRR: 'TELEBIRR',
  MobilePaymentRequestMethodEnum.MPESA: 'MPESA',
  MobilePaymentRequestMethodEnum.unknownDefaultOpenApi:
      'unknown_default_open_api',
};
