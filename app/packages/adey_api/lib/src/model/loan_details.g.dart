// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'loan_details.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LoanDetailsCWProxy {
  LoanDetails accountNo(String accountNo);

  LoanDetails annualInterestRate(num? annualInterestRate);

  LoanDetails approvedPrincipal(num? approvedPrincipal);

  LoanDetails clientId(int clientId);

  LoanDetails clientName(String clientName);

  LoanDetails currency(String? currency);

  LoanDetails dates(Dates? dates);

  LoanDetails loanId(int loanId);

  LoanDetails loanOfficerId(int? loanOfficerId);

  LoanDetails loanOfficerName(String? loanOfficerName);

  LoanDetails numberOfRepayments(int numberOfRepayments);

  LoanDetails principal(num principal);

  LoanDetails productId(int productId);

  LoanDetails productName(String productName);

  LoanDetails schedule(List<Installment> schedule);

  LoanDetails status(LoanDetailsStatusEnum status);

  LoanDetails statusLabel(String? statusLabel);

  LoanDetails summary(LoanTotals summary);

  LoanDetails transactions(List<LoanTransactionResponse> transactions);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `LoanDetails(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// LoanDetails(...).copyWith(id: 12, name: "My name")
  /// ````
  LoanDetails call({
    String accountNo,
    num? annualInterestRate,
    num? approvedPrincipal,
    int clientId,
    String clientName,
    String? currency,
    Dates? dates,
    int loanId,
    int? loanOfficerId,
    String? loanOfficerName,
    int numberOfRepayments,
    num principal,
    int productId,
    String productName,
    List<Installment> schedule,
    LoanDetailsStatusEnum status,
    String? statusLabel,
    LoanTotals summary,
    List<LoanTransactionResponse> transactions,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfLoanDetails.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfLoanDetails.copyWith.fieldName(...)`
class _$LoanDetailsCWProxyImpl implements _$LoanDetailsCWProxy {
  const _$LoanDetailsCWProxyImpl(this._value);

  final LoanDetails _value;

  @override
  LoanDetails accountNo(String accountNo) => this(accountNo: accountNo);

  @override
  LoanDetails annualInterestRate(num? annualInterestRate) =>
      this(annualInterestRate: annualInterestRate);

  @override
  LoanDetails approvedPrincipal(num? approvedPrincipal) =>
      this(approvedPrincipal: approvedPrincipal);

  @override
  LoanDetails clientId(int clientId) => this(clientId: clientId);

  @override
  LoanDetails clientName(String clientName) => this(clientName: clientName);

  @override
  LoanDetails currency(String? currency) => this(currency: currency);

  @override
  LoanDetails dates(Dates? dates) => this(dates: dates);

  @override
  LoanDetails loanId(int loanId) => this(loanId: loanId);

  @override
  LoanDetails loanOfficerId(int? loanOfficerId) =>
      this(loanOfficerId: loanOfficerId);

  @override
  LoanDetails loanOfficerName(String? loanOfficerName) =>
      this(loanOfficerName: loanOfficerName);

  @override
  LoanDetails numberOfRepayments(int numberOfRepayments) =>
      this(numberOfRepayments: numberOfRepayments);

  @override
  LoanDetails principal(num principal) => this(principal: principal);

  @override
  LoanDetails productId(int productId) => this(productId: productId);

  @override
  LoanDetails productName(String productName) => this(productName: productName);

  @override
  LoanDetails schedule(List<Installment> schedule) => this(schedule: schedule);

  @override
  LoanDetails status(LoanDetailsStatusEnum status) => this(status: status);

  @override
  LoanDetails statusLabel(String? statusLabel) =>
      this(statusLabel: statusLabel);

  @override
  LoanDetails summary(LoanTotals summary) => this(summary: summary);

  @override
  LoanDetails transactions(List<LoanTransactionResponse> transactions) =>
      this(transactions: transactions);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `LoanDetails(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// LoanDetails(...).copyWith(id: 12, name: "My name")
  /// ````
  LoanDetails call({
    Object? accountNo = const $CopyWithPlaceholder(),
    Object? annualInterestRate = const $CopyWithPlaceholder(),
    Object? approvedPrincipal = const $CopyWithPlaceholder(),
    Object? clientId = const $CopyWithPlaceholder(),
    Object? clientName = const $CopyWithPlaceholder(),
    Object? currency = const $CopyWithPlaceholder(),
    Object? dates = const $CopyWithPlaceholder(),
    Object? loanId = const $CopyWithPlaceholder(),
    Object? loanOfficerId = const $CopyWithPlaceholder(),
    Object? loanOfficerName = const $CopyWithPlaceholder(),
    Object? numberOfRepayments = const $CopyWithPlaceholder(),
    Object? principal = const $CopyWithPlaceholder(),
    Object? productId = const $CopyWithPlaceholder(),
    Object? productName = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? statusLabel = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
    Object? transactions = const $CopyWithPlaceholder(),
  }) {
    return LoanDetails(
      accountNo: accountNo == const $CopyWithPlaceholder()
          ? _value.accountNo
          // ignore: cast_nullable_to_non_nullable
          : accountNo as String,
      annualInterestRate: annualInterestRate == const $CopyWithPlaceholder()
          ? _value.annualInterestRate
          // ignore: cast_nullable_to_non_nullable
          : annualInterestRate as num?,
      approvedPrincipal: approvedPrincipal == const $CopyWithPlaceholder()
          ? _value.approvedPrincipal
          // ignore: cast_nullable_to_non_nullable
          : approvedPrincipal as num?,
      clientId: clientId == const $CopyWithPlaceholder()
          ? _value.clientId
          // ignore: cast_nullable_to_non_nullable
          : clientId as int,
      clientName: clientName == const $CopyWithPlaceholder()
          ? _value.clientName
          // ignore: cast_nullable_to_non_nullable
          : clientName as String,
      currency: currency == const $CopyWithPlaceholder()
          ? _value.currency
          // ignore: cast_nullable_to_non_nullable
          : currency as String?,
      dates: dates == const $CopyWithPlaceholder()
          ? _value.dates
          // ignore: cast_nullable_to_non_nullable
          : dates as Dates?,
      loanId: loanId == const $CopyWithPlaceholder()
          ? _value.loanId
          // ignore: cast_nullable_to_non_nullable
          : loanId as int,
      loanOfficerId: loanOfficerId == const $CopyWithPlaceholder()
          ? _value.loanOfficerId
          // ignore: cast_nullable_to_non_nullable
          : loanOfficerId as int?,
      loanOfficerName: loanOfficerName == const $CopyWithPlaceholder()
          ? _value.loanOfficerName
          // ignore: cast_nullable_to_non_nullable
          : loanOfficerName as String?,
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
      productName: productName == const $CopyWithPlaceholder()
          ? _value.productName
          // ignore: cast_nullable_to_non_nullable
          : productName as String,
      schedule: schedule == const $CopyWithPlaceholder()
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as List<Installment>,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as LoanDetailsStatusEnum,
      statusLabel: statusLabel == const $CopyWithPlaceholder()
          ? _value.statusLabel
          // ignore: cast_nullable_to_non_nullable
          : statusLabel as String?,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as LoanTotals,
      transactions: transactions == const $CopyWithPlaceholder()
          ? _value.transactions
          // ignore: cast_nullable_to_non_nullable
          : transactions as List<LoanTransactionResponse>,
    );
  }
}

extension $LoanDetailsCopyWith on LoanDetails {
  /// Returns a callable class that can be used as follows: `instanceOfLoanDetails.copyWith(...)` or like so:`instanceOfLoanDetails.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LoanDetailsCWProxy get copyWith => _$LoanDetailsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LoanDetails _$LoanDetailsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LoanDetails', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'accountNo',
      'clientId',
      'clientName',
      'loanId',
      'numberOfRepayments',
      'principal',
      'productId',
      'productName',
      'schedule',
      'status',
      'summary',
      'transactions',
    ],
  );
  final val = LoanDetails(
    accountNo: $checkedConvert('accountNo', (v) => v as String),
    annualInterestRate: $checkedConvert('annualInterestRate', (v) => v as num?),
    approvedPrincipal: $checkedConvert('approvedPrincipal', (v) => v as num?),
    clientId: $checkedConvert('clientId', (v) => (v as num).toInt()),
    clientName: $checkedConvert('clientName', (v) => v as String),
    currency: $checkedConvert('currency', (v) => v as String?),
    dates: $checkedConvert(
      'dates',
      (v) => v == null ? null : Dates.fromJson(v as Map<String, dynamic>),
    ),
    loanId: $checkedConvert('loanId', (v) => (v as num).toInt()),
    loanOfficerId: $checkedConvert(
      'loanOfficerId',
      (v) => (v as num?)?.toInt(),
    ),
    loanOfficerName: $checkedConvert('loanOfficerName', (v) => v as String?),
    numberOfRepayments: $checkedConvert(
      'numberOfRepayments',
      (v) => (v as num).toInt(),
    ),
    principal: $checkedConvert('principal', (v) => v as num),
    productId: $checkedConvert('productId', (v) => (v as num).toInt()),
    productName: $checkedConvert('productName', (v) => v as String),
    schedule: $checkedConvert(
      'schedule',
      (v) => (v as List<dynamic>)
          .map((e) => Installment.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(
        _$LoanDetailsStatusEnumEnumMap,
        v,
        unknownValue: LoanDetailsStatusEnum.unknownDefaultOpenApi,
      ),
    ),
    statusLabel: $checkedConvert('statusLabel', (v) => v as String?),
    summary: $checkedConvert(
      'summary',
      (v) => LoanTotals.fromJson(v as Map<String, dynamic>),
    ),
    transactions: $checkedConvert(
      'transactions',
      (v) => (v as List<dynamic>)
          .map(
            (e) => LoanTransactionResponse.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$LoanDetailsToJson(LoanDetails instance) =>
    <String, dynamic>{
      'accountNo': instance.accountNo,
      'annualInterestRate': ?instance.annualInterestRate,
      'approvedPrincipal': ?instance.approvedPrincipal,
      'clientId': instance.clientId,
      'clientName': instance.clientName,
      'currency': ?instance.currency,
      'dates': ?instance.dates?.toJson(),
      'loanId': instance.loanId,
      'loanOfficerId': ?instance.loanOfficerId,
      'loanOfficerName': ?instance.loanOfficerName,
      'numberOfRepayments': instance.numberOfRepayments,
      'principal': instance.principal,
      'productId': instance.productId,
      'productName': instance.productName,
      'schedule': instance.schedule.map((e) => e.toJson()).toList(),
      'status': _$LoanDetailsStatusEnumEnumMap[instance.status]!,
      'statusLabel': ?instance.statusLabel,
      'summary': instance.summary.toJson(),
      'transactions': instance.transactions.map((e) => e.toJson()).toList(),
    };

const _$LoanDetailsStatusEnumEnumMap = {
  LoanDetailsStatusEnum.SUBMITTED_AND_PENDING_APPROVAL:
      'SUBMITTED_AND_PENDING_APPROVAL',
  LoanDetailsStatusEnum.APPROVED: 'APPROVED',
  LoanDetailsStatusEnum.ACTIVE: 'ACTIVE',
  LoanDetailsStatusEnum.WITHDRAWN_BY_CLIENT: 'WITHDRAWN_BY_CLIENT',
  LoanDetailsStatusEnum.REJECTED: 'REJECTED',
  LoanDetailsStatusEnum.CLOSED: 'CLOSED',
  LoanDetailsStatusEnum.WRITTEN_OFF: 'WRITTEN_OFF',
  LoanDetailsStatusEnum.RESCHEDULED: 'RESCHEDULED',
  LoanDetailsStatusEnum.OVERPAID: 'OVERPAID',
  LoanDetailsStatusEnum.UNKNOWN: 'UNKNOWN',
  LoanDetailsStatusEnum.unknownDefaultOpenApi: 'unknown_default_open_api',
};
