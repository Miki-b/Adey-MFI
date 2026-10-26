// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'loan_status_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LoanStatusResponseCWProxy {
  LoanStatusResponse accountNo(String? accountNo);

  LoanStatusResponse loanId(int? loanId);

  LoanStatusResponse status(LoanStatusResponseStatusEnum? status);

  LoanStatusResponse statusLabel(String? statusLabel);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `LoanStatusResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// LoanStatusResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  LoanStatusResponse call({
    String? accountNo,
    int? loanId,
    LoanStatusResponseStatusEnum? status,
    String? statusLabel,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfLoanStatusResponse.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfLoanStatusResponse.copyWith.fieldName(...)`
class _$LoanStatusResponseCWProxyImpl implements _$LoanStatusResponseCWProxy {
  const _$LoanStatusResponseCWProxyImpl(this._value);

  final LoanStatusResponse _value;

  @override
  LoanStatusResponse accountNo(String? accountNo) => this(accountNo: accountNo);

  @override
  LoanStatusResponse loanId(int? loanId) => this(loanId: loanId);

  @override
  LoanStatusResponse status(LoanStatusResponseStatusEnum? status) =>
      this(status: status);

  @override
  LoanStatusResponse statusLabel(String? statusLabel) =>
      this(statusLabel: statusLabel);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `LoanStatusResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// LoanStatusResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  LoanStatusResponse call({
    Object? accountNo = const $CopyWithPlaceholder(),
    Object? loanId = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusLabel = const $CopyWithPlaceholder(),
  }) {
    return LoanStatusResponse(
      accountNo: accountNo == const $CopyWithPlaceholder()
          ? _value.accountNo
          // ignore: cast_nullable_to_non_nullable
          : accountNo as String?,
      loanId: loanId == const $CopyWithPlaceholder()
          ? _value.loanId
          // ignore: cast_nullable_to_non_nullable
          : loanId as int?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as LoanStatusResponseStatusEnum?,
      statusLabel: statusLabel == const $CopyWithPlaceholder()
          ? _value.statusLabel
          // ignore: cast_nullable_to_non_nullable
          : statusLabel as String?,
    );
  }
}

extension $LoanStatusResponseCopyWith on LoanStatusResponse {
  /// Returns a callable class that can be used as follows: `instanceOfLoanStatusResponse.copyWith(...)` or like so:`instanceOfLoanStatusResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LoanStatusResponseCWProxy get copyWith =>
      _$LoanStatusResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LoanStatusResponse _$LoanStatusResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LoanStatusResponse', json, ($checkedConvert) {
      final val = LoanStatusResponse(
        accountNo: $checkedConvert('accountNo', (v) => v as String?),
        loanId: $checkedConvert('loanId', (v) => (v as num?)?.toInt()),
        status: $checkedConvert(
          'status',
          (v) => $enumDecodeNullable(
            _$LoanStatusResponseStatusEnumEnumMap,
            v,
            unknownValue: LoanStatusResponseStatusEnum.unknownDefaultOpenApi,
          ),
        ),
        statusLabel: $checkedConvert('statusLabel', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$LoanStatusResponseToJson(LoanStatusResponse instance) =>
    <String, dynamic>{
      'accountNo': ?instance.accountNo,
      'loanId': ?instance.loanId,
      'status': ?_$LoanStatusResponseStatusEnumEnumMap[instance.status],
      'statusLabel': ?instance.statusLabel,
    };

const _$LoanStatusResponseStatusEnumEnumMap = {
  LoanStatusResponseStatusEnum.SUBMITTED_AND_PENDING_APPROVAL:
      'SUBMITTED_AND_PENDING_APPROVAL',
  LoanStatusResponseStatusEnum.APPROVED: 'APPROVED',
  LoanStatusResponseStatusEnum.ACTIVE: 'ACTIVE',
  LoanStatusResponseStatusEnum.WITHDRAWN_BY_CLIENT: 'WITHDRAWN_BY_CLIENT',
  LoanStatusResponseStatusEnum.REJECTED: 'REJECTED',
  LoanStatusResponseStatusEnum.CLOSED: 'CLOSED',
  LoanStatusResponseStatusEnum.WRITTEN_OFF: 'WRITTEN_OFF',
  LoanStatusResponseStatusEnum.RESCHEDULED: 'RESCHEDULED',
  LoanStatusResponseStatusEnum.OVERPAID: 'OVERPAID',
  LoanStatusResponseStatusEnum.UNKNOWN: 'UNKNOWN',
  LoanStatusResponseStatusEnum.unknownDefaultOpenApi:
      'unknown_default_open_api',
};
