// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'client_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ClientSummaryCWProxy {
  ClientSummary accountNo(String accountNo);

  ClientSummary activationDate(DateTime? activationDate);

  ClientSummary active(bool active);

  ClientSummary displayName(String displayName);

  ClientSummary id(int id);

  ClientSummary officeName(String officeName);

  ClientSummary staffName(String? staffName);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ClientSummary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ClientSummary(...).copyWith(id: 12, name: "My name")
  /// ````
  ClientSummary call({
    String accountNo,
    DateTime? activationDate,
    bool active,
    String displayName,
    int id,
    String officeName,
    String? staffName,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfClientSummary.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfClientSummary.copyWith.fieldName(...)`
class _$ClientSummaryCWProxyImpl implements _$ClientSummaryCWProxy {
  const _$ClientSummaryCWProxyImpl(this._value);

  final ClientSummary _value;

  @override
  ClientSummary accountNo(String accountNo) => this(accountNo: accountNo);

  @override
  ClientSummary activationDate(DateTime? activationDate) =>
      this(activationDate: activationDate);

  @override
  ClientSummary active(bool active) => this(active: active);

  @override
  ClientSummary displayName(String displayName) =>
      this(displayName: displayName);

  @override
  ClientSummary id(int id) => this(id: id);

  @override
  ClientSummary officeName(String officeName) => this(officeName: officeName);

  @override
  ClientSummary staffName(String? staffName) => this(staffName: staffName);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ClientSummary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ClientSummary(...).copyWith(id: 12, name: "My name")
  /// ````
  ClientSummary call({
    Object? accountNo = const $CopyWithPlaceholder(),
    Object? activationDate = const $CopyWithPlaceholder(),
    Object? active = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? officeName = const $CopyWithPlaceholder(),
    Object? staffName = const $CopyWithPlaceholder(),
  }) {
    return ClientSummary(
      accountNo: accountNo == const $CopyWithPlaceholder()
          ? _value.accountNo
          // ignore: cast_nullable_to_non_nullable
          : accountNo as String,
      activationDate: activationDate == const $CopyWithPlaceholder()
          ? _value.activationDate
          // ignore: cast_nullable_to_non_nullable
          : activationDate as DateTime?,
      active: active == const $CopyWithPlaceholder()
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as bool,
      displayName: displayName == const $CopyWithPlaceholder()
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      officeName: officeName == const $CopyWithPlaceholder()
          ? _value.officeName
          // ignore: cast_nullable_to_non_nullable
          : officeName as String,
      staffName: staffName == const $CopyWithPlaceholder()
          ? _value.staffName
          // ignore: cast_nullable_to_non_nullable
          : staffName as String?,
    );
  }
}

extension $ClientSummaryCopyWith on ClientSummary {
  /// Returns a callable class that can be used as follows: `instanceOfClientSummary.copyWith(...)` or like so:`instanceOfClientSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ClientSummaryCWProxy get copyWith => _$ClientSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ClientSummary _$ClientSummaryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ClientSummary', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'accountNo',
          'active',
          'displayName',
          'id',
          'officeName',
        ],
      );
      final val = ClientSummary(
        accountNo: $checkedConvert('accountNo', (v) => v as String),
        activationDate: $checkedConvert(
          'activationDate',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        active: $checkedConvert('active', (v) => v as bool),
        displayName: $checkedConvert('displayName', (v) => v as String),
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        officeName: $checkedConvert('officeName', (v) => v as String),
        staffName: $checkedConvert('staffName', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ClientSummaryToJson(ClientSummary instance) =>
    <String, dynamic>{
      'accountNo': instance.accountNo,
      'activationDate': ?instance.activationDate?.toIso8601String(),
      'active': instance.active,
      'displayName': instance.displayName,
      'id': instance.id,
      'officeName': instance.officeName,
      'staffName': ?instance.staffName,
    };
