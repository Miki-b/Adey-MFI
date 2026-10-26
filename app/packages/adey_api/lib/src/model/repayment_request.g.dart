// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'repayment_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RepaymentRequestCWProxy {
  RepaymentRequest amount(num amount);

  RepaymentRequest note(String? note);

  RepaymentRequest paymentTypeId(int? paymentTypeId);

  RepaymentRequest transactionDate(DateTime? transactionDate);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `RepaymentRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// RepaymentRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  RepaymentRequest call({
    num amount,
    String? note,
    int? paymentTypeId,
    DateTime? transactionDate,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfRepaymentRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfRepaymentRequest.copyWith.fieldName(...)`
class _$RepaymentRequestCWProxyImpl implements _$RepaymentRequestCWProxy {
  const _$RepaymentRequestCWProxyImpl(this._value);

  final RepaymentRequest _value;

  @override
  RepaymentRequest amount(num amount) => this(amount: amount);

  @override
  RepaymentRequest note(String? note) => this(note: note);

  @override
  RepaymentRequest paymentTypeId(int? paymentTypeId) =>
      this(paymentTypeId: paymentTypeId);

  @override
  RepaymentRequest transactionDate(DateTime? transactionDate) =>
      this(transactionDate: transactionDate);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `RepaymentRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// RepaymentRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  RepaymentRequest call({
    Object? amount = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? paymentTypeId = const $CopyWithPlaceholder(),
    Object? transactionDate = const $CopyWithPlaceholder(),
  }) {
    return RepaymentRequest(
      amount: amount == const $CopyWithPlaceholder()
          ? _value.amount
          // ignore: cast_nullable_to_non_nullable
          : amount as num,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      paymentTypeId: paymentTypeId == const $CopyWithPlaceholder()
          ? _value.paymentTypeId
          // ignore: cast_nullable_to_non_nullable
          : paymentTypeId as int?,
      transactionDate: transactionDate == const $CopyWithPlaceholder()
          ? _value.transactionDate
          // ignore: cast_nullable_to_non_nullable
          : transactionDate as DateTime?,
    );
  }
}

extension $RepaymentRequestCopyWith on RepaymentRequest {
  /// Returns a callable class that can be used as follows: `instanceOfRepaymentRequest.copyWith(...)` or like so:`instanceOfRepaymentRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RepaymentRequestCWProxy get copyWith => _$RepaymentRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RepaymentRequest _$RepaymentRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RepaymentRequest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['amount']);
      final val = RepaymentRequest(
        amount: $checkedConvert('amount', (v) => v as num),
        note: $checkedConvert('note', (v) => v as String?),
        paymentTypeId: $checkedConvert(
          'paymentTypeId',
          (v) => (v as num?)?.toInt(),
        ),
        transactionDate: $checkedConvert(
          'transactionDate',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RepaymentRequestToJson(RepaymentRequest instance) =>
    <String, dynamic>{
      'amount': instance.amount,
      'note': ?instance.note,
      'paymentTypeId': ?instance.paymentTypeId,
      'transactionDate': ?instance.transactionDate?.toIso8601String(),
    };
