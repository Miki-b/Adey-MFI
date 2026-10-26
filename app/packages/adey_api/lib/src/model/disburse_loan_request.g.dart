// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'disburse_loan_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DisburseLoanRequestCWProxy {
  DisburseLoanRequest actualDisbursementDate(DateTime? actualDisbursementDate);

  DisburseLoanRequest note(String? note);

  DisburseLoanRequest paymentTypeId(int? paymentTypeId);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `DisburseLoanRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// DisburseLoanRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  DisburseLoanRequest call({
    DateTime? actualDisbursementDate,
    String? note,
    int? paymentTypeId,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfDisburseLoanRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfDisburseLoanRequest.copyWith.fieldName(...)`
class _$DisburseLoanRequestCWProxyImpl implements _$DisburseLoanRequestCWProxy {
  const _$DisburseLoanRequestCWProxyImpl(this._value);

  final DisburseLoanRequest _value;

  @override
  DisburseLoanRequest actualDisbursementDate(
    DateTime? actualDisbursementDate,
  ) => this(actualDisbursementDate: actualDisbursementDate);

  @override
  DisburseLoanRequest note(String? note) => this(note: note);

  @override
  DisburseLoanRequest paymentTypeId(int? paymentTypeId) =>
      this(paymentTypeId: paymentTypeId);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `DisburseLoanRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// DisburseLoanRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  DisburseLoanRequest call({
    Object? actualDisbursementDate = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? paymentTypeId = const $CopyWithPlaceholder(),
  }) {
    return DisburseLoanRequest(
      actualDisbursementDate:
          actualDisbursementDate == const $CopyWithPlaceholder()
          ? _value.actualDisbursementDate
          // ignore: cast_nullable_to_non_nullable
          : actualDisbursementDate as DateTime?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      paymentTypeId: paymentTypeId == const $CopyWithPlaceholder()
          ? _value.paymentTypeId
          // ignore: cast_nullable_to_non_nullable
          : paymentTypeId as int?,
    );
  }
}

extension $DisburseLoanRequestCopyWith on DisburseLoanRequest {
  /// Returns a callable class that can be used as follows: `instanceOfDisburseLoanRequest.copyWith(...)` or like so:`instanceOfDisburseLoanRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DisburseLoanRequestCWProxy get copyWith =>
      _$DisburseLoanRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DisburseLoanRequest _$DisburseLoanRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DisburseLoanRequest', json, ($checkedConvert) {
      final val = DisburseLoanRequest(
        actualDisbursementDate: $checkedConvert(
          'actualDisbursementDate',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        note: $checkedConvert('note', (v) => v as String?),
        paymentTypeId: $checkedConvert(
          'paymentTypeId',
          (v) => (v as num?)?.toInt(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DisburseLoanRequestToJson(
  DisburseLoanRequest instance,
) => <String, dynamic>{
  'actualDisbursementDate': ?instance.actualDisbursementDate?.toIso8601String(),
  'note': ?instance.note,
  'paymentTypeId': ?instance.paymentTypeId,
};
