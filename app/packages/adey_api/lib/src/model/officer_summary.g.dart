// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'officer_summary.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OfficerSummaryCWProxy {
  OfficerSummary activeLoans(int activeLoans);

  OfficerSummary clients(int clients);

  OfficerSummary officerName(String? officerName);

  OfficerSummary par30(num par30);

  OfficerSummary principalOutstanding(num principalOutstanding);

  OfficerSummary staffId(int? staffId);

  OfficerSummary totalOverdue(num totalOverdue);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `OfficerSummary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// OfficerSummary(...).copyWith(id: 12, name: "My name")
  /// ````
  OfficerSummary call({
    int activeLoans,
    int clients,
    String? officerName,
    num par30,
    num principalOutstanding,
    int? staffId,
    num totalOverdue,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfOfficerSummary.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfOfficerSummary.copyWith.fieldName(...)`
class _$OfficerSummaryCWProxyImpl implements _$OfficerSummaryCWProxy {
  const _$OfficerSummaryCWProxyImpl(this._value);

  final OfficerSummary _value;

  @override
  OfficerSummary activeLoans(int activeLoans) => this(activeLoans: activeLoans);

  @override
  OfficerSummary clients(int clients) => this(clients: clients);

  @override
  OfficerSummary officerName(String? officerName) =>
      this(officerName: officerName);

  @override
  OfficerSummary par30(num par30) => this(par30: par30);

  @override
  OfficerSummary principalOutstanding(num principalOutstanding) =>
      this(principalOutstanding: principalOutstanding);

  @override
  OfficerSummary staffId(int? staffId) => this(staffId: staffId);

  @override
  OfficerSummary totalOverdue(num totalOverdue) =>
      this(totalOverdue: totalOverdue);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `OfficerSummary(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// OfficerSummary(...).copyWith(id: 12, name: "My name")
  /// ````
  OfficerSummary call({
    Object? activeLoans = const $CopyWithPlaceholder(),
    Object? clients = const $CopyWithPlaceholder(),
    Object? officerName = const $CopyWithPlaceholder(),
    Object? par30 = const $CopyWithPlaceholder(),
    Object? principalOutstanding = const $CopyWithPlaceholder(),
    Object? staffId = const $CopyWithPlaceholder(),
    Object? totalOverdue = const $CopyWithPlaceholder(),
  }) {
    return OfficerSummary(
      activeLoans: activeLoans == const $CopyWithPlaceholder()
          ? _value.activeLoans
          // ignore: cast_nullable_to_non_nullable
          : activeLoans as int,
      clients: clients == const $CopyWithPlaceholder()
          ? _value.clients
          // ignore: cast_nullable_to_non_nullable
          : clients as int,
      officerName: officerName == const $CopyWithPlaceholder()
          ? _value.officerName
          // ignore: cast_nullable_to_non_nullable
          : officerName as String?,
      par30: par30 == const $CopyWithPlaceholder()
          ? _value.par30
          // ignore: cast_nullable_to_non_nullable
          : par30 as num,
      principalOutstanding: principalOutstanding == const $CopyWithPlaceholder()
          ? _value.principalOutstanding
          // ignore: cast_nullable_to_non_nullable
          : principalOutstanding as num,
      staffId: staffId == const $CopyWithPlaceholder()
          ? _value.staffId
          // ignore: cast_nullable_to_non_nullable
          : staffId as int?,
      totalOverdue: totalOverdue == const $CopyWithPlaceholder()
          ? _value.totalOverdue
          // ignore: cast_nullable_to_non_nullable
          : totalOverdue as num,
    );
  }
}

extension $OfficerSummaryCopyWith on OfficerSummary {
  /// Returns a callable class that can be used as follows: `instanceOfOfficerSummary.copyWith(...)` or like so:`instanceOfOfficerSummary.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OfficerSummaryCWProxy get copyWith => _$OfficerSummaryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OfficerSummary _$OfficerSummaryFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OfficerSummary', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'activeLoans',
          'clients',
          'par30',
          'principalOutstanding',
          'totalOverdue',
        ],
      );
      final val = OfficerSummary(
        activeLoans: $checkedConvert('activeLoans', (v) => (v as num).toInt()),
        clients: $checkedConvert('clients', (v) => (v as num).toInt()),
        officerName: $checkedConvert('officerName', (v) => v as String?),
        par30: $checkedConvert('par30', (v) => v as num),
        principalOutstanding: $checkedConvert(
          'principalOutstanding',
          (v) => v as num,
        ),
        staffId: $checkedConvert('staffId', (v) => (v as num?)?.toInt()),
        totalOverdue: $checkedConvert('totalOverdue', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$OfficerSummaryToJson(OfficerSummary instance) =>
    <String, dynamic>{
      'activeLoans': instance.activeLoans,
      'clients': instance.clients,
      'officerName': ?instance.officerName,
      'par30': instance.par30,
      'principalOutstanding': instance.principalOutstanding,
      'staffId': ?instance.staffId,
      'totalOverdue': instance.totalOverdue,
    };
