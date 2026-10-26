// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'counts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CountsCWProxy {
  Counts activeLoans(int? activeLoans);

  Counts approvedNotDisbursed(int? approvedNotDisbursed);

  Counts clients(int? clients);

  Counts loansInArrears(int? loansInArrears);

  Counts pendingApplications(int? pendingApplications);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Counts(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Counts(...).copyWith(id: 12, name: "My name")
  /// ````
  Counts call({
    int? activeLoans,
    int? approvedNotDisbursed,
    int? clients,
    int? loansInArrears,
    int? pendingApplications,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfCounts.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfCounts.copyWith.fieldName(...)`
class _$CountsCWProxyImpl implements _$CountsCWProxy {
  const _$CountsCWProxyImpl(this._value);

  final Counts _value;

  @override
  Counts activeLoans(int? activeLoans) => this(activeLoans: activeLoans);

  @override
  Counts approvedNotDisbursed(int? approvedNotDisbursed) =>
      this(approvedNotDisbursed: approvedNotDisbursed);

  @override
  Counts clients(int? clients) => this(clients: clients);

  @override
  Counts loansInArrears(int? loansInArrears) =>
      this(loansInArrears: loansInArrears);

  @override
  Counts pendingApplications(int? pendingApplications) =>
      this(pendingApplications: pendingApplications);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Counts(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Counts(...).copyWith(id: 12, name: "My name")
  /// ````
  Counts call({
    Object? activeLoans = const $CopyWithPlaceholder(),
    Object? approvedNotDisbursed = const $CopyWithPlaceholder(),
    Object? clients = const $CopyWithPlaceholder(),
    Object? loansInArrears = const $CopyWithPlaceholder(),
    Object? pendingApplications = const $CopyWithPlaceholder(),
  }) {
    return Counts(
      activeLoans: activeLoans == const $CopyWithPlaceholder()
          ? _value.activeLoans
          // ignore: cast_nullable_to_non_nullable
          : activeLoans as int?,
      approvedNotDisbursed: approvedNotDisbursed == const $CopyWithPlaceholder()
          ? _value.approvedNotDisbursed
          // ignore: cast_nullable_to_non_nullable
          : approvedNotDisbursed as int?,
      clients: clients == const $CopyWithPlaceholder()
          ? _value.clients
          // ignore: cast_nullable_to_non_nullable
          : clients as int?,
      loansInArrears: loansInArrears == const $CopyWithPlaceholder()
          ? _value.loansInArrears
          // ignore: cast_nullable_to_non_nullable
          : loansInArrears as int?,
      pendingApplications: pendingApplications == const $CopyWithPlaceholder()
          ? _value.pendingApplications
          // ignore: cast_nullable_to_non_nullable
          : pendingApplications as int?,
    );
  }
}

extension $CountsCopyWith on Counts {
  /// Returns a callable class that can be used as follows: `instanceOfCounts.copyWith(...)` or like so:`instanceOfCounts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CountsCWProxy get copyWith => _$CountsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Counts _$CountsFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Counts',
  json,
  ($checkedConvert) {
    final val = Counts(
      activeLoans: $checkedConvert('activeLoans', (v) => (v as num?)?.toInt()),
      approvedNotDisbursed: $checkedConvert(
        'approvedNotDisbursed',
        (v) => (v as num?)?.toInt(),
      ),
      clients: $checkedConvert('clients', (v) => (v as num?)?.toInt()),
      loansInArrears: $checkedConvert(
        'loansInArrears',
        (v) => (v as num?)?.toInt(),
      ),
      pendingApplications: $checkedConvert(
        'pendingApplications',
        (v) => (v as num?)?.toInt(),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$CountsToJson(Counts instance) => <String, dynamic>{
  'activeLoans': ?instance.activeLoans,
  'approvedNotDisbursed': ?instance.approvedNotDisbursed,
  'clients': ?instance.clients,
  'loansInArrears': ?instance.loansInArrears,
  'pendingApplications': ?instance.pendingApplications,
};
