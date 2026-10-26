// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'reject_loan_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RejectLoanRequestCWProxy {
  RejectLoanRequest note(String? note);

  RejectLoanRequest rejectedOnDate(DateTime? rejectedOnDate);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `RejectLoanRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// RejectLoanRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  RejectLoanRequest call({String? note, DateTime? rejectedOnDate});
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfRejectLoanRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfRejectLoanRequest.copyWith.fieldName(...)`
class _$RejectLoanRequestCWProxyImpl implements _$RejectLoanRequestCWProxy {
  const _$RejectLoanRequestCWProxyImpl(this._value);

  final RejectLoanRequest _value;

  @override
  RejectLoanRequest note(String? note) => this(note: note);

  @override
  RejectLoanRequest rejectedOnDate(DateTime? rejectedOnDate) =>
      this(rejectedOnDate: rejectedOnDate);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `RejectLoanRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// RejectLoanRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  RejectLoanRequest call({
    Object? note = const $CopyWithPlaceholder(),
    Object? rejectedOnDate = const $CopyWithPlaceholder(),
  }) {
    return RejectLoanRequest(
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      rejectedOnDate: rejectedOnDate == const $CopyWithPlaceholder()
          ? _value.rejectedOnDate
          // ignore: cast_nullable_to_non_nullable
          : rejectedOnDate as DateTime?,
    );
  }
}

extension $RejectLoanRequestCopyWith on RejectLoanRequest {
  /// Returns a callable class that can be used as follows: `instanceOfRejectLoanRequest.copyWith(...)` or like so:`instanceOfRejectLoanRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RejectLoanRequestCWProxy get copyWith =>
      _$RejectLoanRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RejectLoanRequest _$RejectLoanRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RejectLoanRequest', json, ($checkedConvert) {
      final val = RejectLoanRequest(
        note: $checkedConvert('note', (v) => v as String?),
        rejectedOnDate: $checkedConvert(
          'rejectedOnDate',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$RejectLoanRequestToJson(RejectLoanRequest instance) =>
    <String, dynamic>{
      'note': ?instance.note,
      'rejectedOnDate': ?instance.rejectedOnDate?.toIso8601String(),
    };
