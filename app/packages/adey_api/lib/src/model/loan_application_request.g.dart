// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'loan_application_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LoanApplicationRequestCWProxy {
  LoanApplicationRequest clientId(int clientId);

  LoanApplicationRequest expectedDisbursementDate(
    DateTime? expectedDisbursementDate,
  );

  LoanApplicationRequest numberOfRepayments(int numberOfRepayments);

  LoanApplicationRequest principal(num principal);

  LoanApplicationRequest productId(int productId);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `LoanApplicationRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// LoanApplicationRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  LoanApplicationRequest call({
    int clientId,
    DateTime? expectedDisbursementDate,
    int numberOfRepayments,
    num principal,
    int productId,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfLoanApplicationRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfLoanApplicationRequest.copyWith.fieldName(...)`
class _$LoanApplicationRequestCWProxyImpl
    implements _$LoanApplicationRequestCWProxy {
  const _$LoanApplicationRequestCWProxyImpl(this._value);

  final LoanApplicationRequest _value;

  @override
  LoanApplicationRequest clientId(int clientId) => this(clientId: clientId);

  @override
  LoanApplicationRequest expectedDisbursementDate(
    DateTime? expectedDisbursementDate,
  ) => this(expectedDisbursementDate: expectedDisbursementDate);

  @override
  LoanApplicationRequest numberOfRepayments(int numberOfRepayments) =>
      this(numberOfRepayments: numberOfRepayments);

  @override
  LoanApplicationRequest principal(num principal) => this(principal: principal);

  @override
  LoanApplicationRequest productId(int productId) => this(productId: productId);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `LoanApplicationRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// LoanApplicationRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  LoanApplicationRequest call({
    Object? clientId = const $CopyWithPlaceholder(),
    Object? expectedDisbursementDate = const $CopyWithPlaceholder(),
    Object? numberOfRepayments = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? productId = const $CopyWithPlaceholder(),
  }) {
    return LoanApplicationRequest(
      clientId: clientId == const $CopyWithPlaceholder()
          ? _value.clientId
          // ignore: cast_nullable_to_non_nullable
          : clientId as int,
      expectedDisbursementDate:
          expectedDisbursementDate == const $CopyWithPlaceholder()
          ? _value.expectedDisbursementDate
          // ignore: cast_nullable_to_non_nullable
          : expectedDisbursementDate as DateTime?,
      numberOfRepayments: numberOfRepayments == const $CopyWithPlaceholder()
          ? _value.numberOfRepayments
          // ignore: cast_nullable_to_non_nullable
          : numberOfRepayments as int,
      principal: principal == const $CopyWithPlaceholder()
          ? _value.principal
          // ignore: cast_nullable_to_non_nullable
          : principal as num,
      productId: productId == const $CopyWithPlaceholder()
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as int,
    );
  }
}

extension $LoanApplicationRequestCopyWith on LoanApplicationRequest {
  /// Returns a callable class that can be used as follows: `instanceOfLoanApplicationRequest.copyWith(...)` or like so:`instanceOfLoanApplicationRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LoanApplicationRequestCWProxy get copyWith =>
      _$LoanApplicationRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LoanApplicationRequest _$LoanApplicationRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LoanApplicationRequest', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'clientId',
      'numberOfRepayments',
      'principal',
      'productId',
    ],
  );
  final val = LoanApplicationRequest(
    clientId: $checkedConvert('clientId', (v) => (v as num).toInt()),
    expectedDisbursementDate: $checkedConvert(
      'expectedDisbursementDate',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    numberOfRepayments: $checkedConvert(
      'numberOfRepayments',
      (v) => (v as num).toInt(),
    ),
    principal: $checkedConvert('principal', (v) => v as num),
    productId: $checkedConvert('productId', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$LoanApplicationRequestToJson(
  LoanApplicationRequest instance,
) => <String, dynamic>{
  'clientId': instance.clientId,
  'expectedDisbursementDate': ?instance.expectedDisbursementDate
      ?.toIso8601String(),
  'numberOfRepayments': instance.numberOfRepayments,
  'principal': instance.principal,
  'productId': instance.productId,
};
