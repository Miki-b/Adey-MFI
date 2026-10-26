// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'collection_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CollectionItemCWProxy {
  CollectionItem accountNo(String? accountNo);

  CollectionItem amount(num? amount);

  CollectionItem clientId(int? clientId);

  CollectionItem clientMobileNo(String? clientMobileNo);

  CollectionItem clientName(String? clientName);

  CollectionItem daysOverdue(int? daysOverdue);

  CollectionItem dueDate(DateTime? dueDate);

  CollectionItem installment(int? installment);

  CollectionItem loanId(int? loanId);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `CollectionItem(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// CollectionItem(...).copyWith(id: 12, name: "My name")
  /// ````
  CollectionItem call({
    String? accountNo,
    num? amount,
    int? clientId,
    String? clientMobileNo,
    String? clientName,
    int? daysOverdue,
    DateTime? dueDate,
    int? installment,
    int? loanId,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfCollectionItem.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfCollectionItem.copyWith.fieldName(...)`
class _$CollectionItemCWProxyImpl implements _$CollectionItemCWProxy {
  const _$CollectionItemCWProxyImpl(this._value);

  final CollectionItem _value;

  @override
  CollectionItem accountNo(String? accountNo) => this(accountNo: accountNo);

  @override
  CollectionItem amount(num? amount) => this(amount: amount);

  @override
  CollectionItem clientId(int? clientId) => this(clientId: clientId);

  @override
  CollectionItem clientMobileNo(String? clientMobileNo) =>
      this(clientMobileNo: clientMobileNo);

  @override
  CollectionItem clientName(String? clientName) => this(clientName: clientName);

  @override
  CollectionItem daysOverdue(int? daysOverdue) =>
      this(daysOverdue: daysOverdue);

  @override
  CollectionItem dueDate(DateTime? dueDate) => this(dueDate: dueDate);

  @override
  CollectionItem installment(int? installment) =>
      this(installment: installment);

  @override
  CollectionItem loanId(int? loanId) => this(loanId: loanId);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `CollectionItem(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// CollectionItem(...).copyWith(id: 12, name: "My name")
  /// ````
  CollectionItem call({
    Object? accountNo = const $CopyWithPlaceholder(),
    Object? amount = const $CopyWithPlaceholder(),
    Object? clientId = const $CopyWithPlaceholder(),
    Object? clientMobileNo = const $CopyWithPlaceholder(),
    Object? clientName = const $CopyWithPlaceholder(),
    Object? daysOverdue = const $CopyWithPlaceholder(),
    Object? dueDate = const $CopyWithPlaceholder(),
    Object? installment = const $CopyWithPlaceholder(),
    Object? loanId = const $CopyWithPlaceholder(),
  }) {
    return CollectionItem(
      accountNo: accountNo == const $CopyWithPlaceholder()
          ? _value.accountNo
          // ignore: cast_nullable_to_non_nullable
          : accountNo as String?,
      amount: amount == const $CopyWithPlaceholder()
          ? _value.amount
          // ignore: cast_nullable_to_non_nullable
          : amount as num?,
      clientId: clientId == const $CopyWithPlaceholder()
          ? _value.clientId
          // ignore: cast_nullable_to_non_nullable
          : clientId as int?,
      clientMobileNo: clientMobileNo == const $CopyWithPlaceholder()
          ? _value.clientMobileNo
          // ignore: cast_nullable_to_non_nullable
          : clientMobileNo as String?,
      clientName: clientName == const $CopyWithPlaceholder()
          ? _value.clientName
          // ignore: cast_nullable_to_non_nullable
          : clientName as String?,
      daysOverdue: daysOverdue == const $CopyWithPlaceholder()
          ? _value.daysOverdue
          // ignore: cast_nullable_to_non_nullable
          : daysOverdue as int?,
      dueDate: dueDate == const $CopyWithPlaceholder()
          ? _value.dueDate
          // ignore: cast_nullable_to_non_nullable
          : dueDate as DateTime?,
      installment: installment == const $CopyWithPlaceholder()
          ? _value.installment
          // ignore: cast_nullable_to_non_nullable
          : installment as int?,
      loanId: loanId == const $CopyWithPlaceholder()
          ? _value.loanId
          // ignore: cast_nullable_to_non_nullable
          : loanId as int?,
    );
  }
}

extension $CollectionItemCopyWith on CollectionItem {
  /// Returns a callable class that can be used as follows: `instanceOfCollectionItem.copyWith(...)` or like so:`instanceOfCollectionItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CollectionItemCWProxy get copyWith => _$CollectionItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CollectionItem _$CollectionItemFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CollectionItem', json, ($checkedConvert) {
  final val = CollectionItem(
    accountNo: $checkedConvert('accountNo', (v) => v as String?),
    amount: $checkedConvert('amount', (v) => v as num?),
    clientId: $checkedConvert('clientId', (v) => (v as num?)?.toInt()),
    clientMobileNo: $checkedConvert('clientMobileNo', (v) => v as String?),
    clientName: $checkedConvert('clientName', (v) => v as String?),
    daysOverdue: $checkedConvert('daysOverdue', (v) => (v as num?)?.toInt()),
    dueDate: $checkedConvert(
      'dueDate',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    installment: $checkedConvert('installment', (v) => (v as num?)?.toInt()),
    loanId: $checkedConvert('loanId', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$CollectionItemToJson(CollectionItem instance) =>
    <String, dynamic>{
      'accountNo': ?instance.accountNo,
      'amount': ?instance.amount,
      'clientId': ?instance.clientId,
      'clientMobileNo': ?instance.clientMobileNo,
      'clientName': ?instance.clientName,
      'daysOverdue': ?instance.daysOverdue,
      'dueDate': ?instance.dueDate?.toIso8601String(),
      'installment': ?instance.installment,
      'loanId': ?instance.loanId,
    };
