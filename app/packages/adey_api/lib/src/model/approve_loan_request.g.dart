// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'approve_loan_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ApproveLoanRequestCWProxy {
  ApproveLoanRequest approvedAmount(num? approvedAmount);

  ApproveLoanRequest approvedOnDate(DateTime? approvedOnDate);

  ApproveLoanRequest note(String? note);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ApproveLoanRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ApproveLoanRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  ApproveLoanRequest call({
    num? approvedAmount,
    DateTime? approvedOnDate,
    String? note,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfApproveLoanRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfApproveLoanRequest.copyWith.fieldName(...)`
class _$ApproveLoanRequestCWProxyImpl implements _$ApproveLoanRequestCWProxy {
  const _$ApproveLoanRequestCWProxyImpl(this._value);

  final ApproveLoanRequest _value;

  @override
  ApproveLoanRequest approvedAmount(num? approvedAmount) =>
      this(approvedAmount: approvedAmount);

  @override
  ApproveLoanRequest approvedOnDate(DateTime? approvedOnDate) =>
      this(approvedOnDate: approvedOnDate);

  @override
  ApproveLoanRequest note(String? note) => this(note: note);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ApproveLoanRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ApproveLoanRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  ApproveLoanRequest call({
    Object? approvedAmount = const $CopyWithPlaceholder(),
    Object? approvedOnDate = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
  }) {
    return ApproveLoanRequest(
      approvedAmount: approvedAmount == const $CopyWithPlaceholder()
          ? _value.approvedAmount
          // ignore: cast_nullable_to_non_nullable
          : approvedAmount as num?,
      approvedOnDate: approvedOnDate == const $CopyWithPlaceholder()
          ? _value.approvedOnDate
          // ignore: cast_nullable_to_non_nullable
          : approvedOnDate as DateTime?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $ApproveLoanRequestCopyWith on ApproveLoanRequest {
  /// Returns a callable class that can be used as follows: `instanceOfApproveLoanRequest.copyWith(...)` or like so:`instanceOfApproveLoanRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ApproveLoanRequestCWProxy get copyWith =>
      _$ApproveLoanRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ApproveLoanRequest _$ApproveLoanRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ApproveLoanRequest', json, ($checkedConvert) {
      final val = ApproveLoanRequest(
        approvedAmount: $checkedConvert('approvedAmount', (v) => v as num?),
        approvedOnDate: $checkedConvert(
          'approvedOnDate',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        note: $checkedConvert('note', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ApproveLoanRequestToJson(ApproveLoanRequest instance) =>
    <String, dynamic>{
      'approvedAmount': ?instance.approvedAmount,
      'approvedOnDate': ?instance.approvedOnDate?.toIso8601String(),
      'note': ?instance.note,
    };
