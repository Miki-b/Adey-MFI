// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'schedule_preview_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SchedulePreviewRequestCWProxy {
  SchedulePreviewRequest clientId(int clientId);

  SchedulePreviewRequest expectedDisbursementDate(
    DateTime? expectedDisbursementDate,
  );

  SchedulePreviewRequest numberOfRepayments(int numberOfRepayments);

  SchedulePreviewRequest principal(num principal);

  SchedulePreviewRequest productId(int productId);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `SchedulePreviewRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// SchedulePreviewRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  SchedulePreviewRequest call({
    int clientId,
    DateTime? expectedDisbursementDate,
    int numberOfRepayments,
    num principal,
    int productId,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfSchedulePreviewRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfSchedulePreviewRequest.copyWith.fieldName(...)`
class _$SchedulePreviewRequestCWProxyImpl
    implements _$SchedulePreviewRequestCWProxy {
  const _$SchedulePreviewRequestCWProxyImpl(this._value);

  final SchedulePreviewRequest _value;

  @override
  SchedulePreviewRequest clientId(int clientId) => this(clientId: clientId);

  @override
  SchedulePreviewRequest expectedDisbursementDate(
    DateTime? expectedDisbursementDate,
  ) => this(expectedDisbursementDate: expectedDisbursementDate);

  @override
  SchedulePreviewRequest numberOfRepayments(int numberOfRepayments) =>
      this(numberOfRepayments: numberOfRepayments);

  @override
  SchedulePreviewRequest principal(num principal) => this(principal: principal);

  @override
  SchedulePreviewRequest productId(int productId) => this(productId: productId);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `SchedulePreviewRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// SchedulePreviewRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  SchedulePreviewRequest call({
    Object? clientId = const $CopyWithPlaceholder(),
    Object? expectedDisbursementDate = const $CopyWithPlaceholder(),
    Object? numberOfRepayments = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? productId = const $CopyWithPlaceholder(),
  }) {
    return SchedulePreviewRequest(
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

extension $SchedulePreviewRequestCopyWith on SchedulePreviewRequest {
  /// Returns a callable class that can be used as follows: `instanceOfSchedulePreviewRequest.copyWith(...)` or like so:`instanceOfSchedulePreviewRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SchedulePreviewRequestCWProxy get copyWith =>
      _$SchedulePreviewRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SchedulePreviewRequest _$SchedulePreviewRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SchedulePreviewRequest', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'clientId',
      'numberOfRepayments',
      'principal',
      'productId',
    ],
  );
  final val = SchedulePreviewRequest(
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

Map<String, dynamic> _$SchedulePreviewRequestToJson(
  SchedulePreviewRequest instance,
) => <String, dynamic>{
  'clientId': instance.clientId,
  'expectedDisbursementDate': ?instance.expectedDisbursementDate
      ?.toIso8601String(),
  'numberOfRepayments': instance.numberOfRepayments,
  'principal': instance.principal,
  'productId': instance.productId,
};
