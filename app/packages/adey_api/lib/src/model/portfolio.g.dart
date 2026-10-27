// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'portfolio.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PortfolioCWProxy {
  Portfolio amounts(Amounts amounts);

  Portfolio asOf(DateTime asOf);

  Portfolio counts(Counts counts);

  Portfolio dueToday(List<CollectionItem> dueToday);

  Portfolio dueTodayCount(int dueTodayCount);

  Portfolio officers(List<OfficerSummary>? officers);

  Portfolio overdue(List<CollectionItem> overdue);

  Portfolio overdueCount(int overdueCount);

  Portfolio par(Par par);

  Portfolio scope(Scope scope);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Portfolio(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Portfolio(...).copyWith(id: 12, name: "My name")
  /// ````
  Portfolio call({
    Amounts amounts,
    DateTime asOf,
    Counts counts,
    List<CollectionItem> dueToday,
    int dueTodayCount,
    List<OfficerSummary>? officers,
    List<CollectionItem> overdue,
    int overdueCount,
    Par par,
    Scope scope,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfPortfolio.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfPortfolio.copyWith.fieldName(...)`
class _$PortfolioCWProxyImpl implements _$PortfolioCWProxy {
  const _$PortfolioCWProxyImpl(this._value);

  final Portfolio _value;

  @override
  Portfolio amounts(Amounts amounts) => this(amounts: amounts);

  @override
  Portfolio asOf(DateTime asOf) => this(asOf: asOf);

  @override
  Portfolio counts(Counts counts) => this(counts: counts);

  @override
  Portfolio dueToday(List<CollectionItem> dueToday) => this(dueToday: dueToday);

  @override
  Portfolio dueTodayCount(int dueTodayCount) =>
      this(dueTodayCount: dueTodayCount);

  @override
  Portfolio officers(List<OfficerSummary>? officers) =>
      this(officers: officers);

  @override
  Portfolio overdue(List<CollectionItem> overdue) => this(overdue: overdue);

  @override
  Portfolio overdueCount(int overdueCount) => this(overdueCount: overdueCount);

  @override
  Portfolio par(Par par) => this(par: par);

  @override
  Portfolio scope(Scope scope) => this(scope: scope);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Portfolio(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Portfolio(...).copyWith(id: 12, name: "My name")
  /// ````
  Portfolio call({
    Object? amounts = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? counts = const $CopyWithPlaceholder(),
    Object? dueToday = const $CopyWithPlaceholder(),
    Object? dueTodayCount = const $CopyWithPlaceholder(),
    Object? officers = const $CopyWithPlaceholder(),
    Object? overdue = const $CopyWithPlaceholder(),
    Object? overdueCount = const $CopyWithPlaceholder(),
    Object? par = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
  }) {
    return Portfolio(
      amounts: amounts == const $CopyWithPlaceholder()
          ? _value.amounts
          // ignore: cast_nullable_to_non_nullable
          : amounts as Amounts,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime,
      counts: counts == const $CopyWithPlaceholder()
          ? _value.counts
          // ignore: cast_nullable_to_non_nullable
          : counts as Counts,
      dueToday: dueToday == const $CopyWithPlaceholder()
          ? _value.dueToday
          // ignore: cast_nullable_to_non_nullable
          : dueToday as List<CollectionItem>,
      dueTodayCount: dueTodayCount == const $CopyWithPlaceholder()
          ? _value.dueTodayCount
          // ignore: cast_nullable_to_non_nullable
          : dueTodayCount as int,
      officers: officers == const $CopyWithPlaceholder()
          ? _value.officers
          // ignore: cast_nullable_to_non_nullable
          : officers as List<OfficerSummary>?,
      overdue: overdue == const $CopyWithPlaceholder()
          ? _value.overdue
          // ignore: cast_nullable_to_non_nullable
          : overdue as List<CollectionItem>,
      overdueCount: overdueCount == const $CopyWithPlaceholder()
          ? _value.overdueCount
          // ignore: cast_nullable_to_non_nullable
          : overdueCount as int,
      par: par == const $CopyWithPlaceholder()
          ? _value.par
          // ignore: cast_nullable_to_non_nullable
          : par as Par,
      scope: scope == const $CopyWithPlaceholder()
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as Scope,
    );
  }
}

extension $PortfolioCopyWith on Portfolio {
  /// Returns a callable class that can be used as follows: `instanceOfPortfolio.copyWith(...)` or like so:`instanceOfPortfolio.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PortfolioCWProxy get copyWith => _$PortfolioCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Portfolio _$PortfolioFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Portfolio', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'amounts',
      'asOf',
      'counts',
      'dueToday',
      'dueTodayCount',
      'overdue',
      'overdueCount',
      'par',
      'scope',
    ],
  );
  final val = Portfolio(
    amounts: $checkedConvert(
      'amounts',
      (v) => Amounts.fromJson(v as Map<String, dynamic>),
    ),
    asOf: $checkedConvert('asOf', (v) => DateTime.parse(v as String)),
    counts: $checkedConvert(
      'counts',
      (v) => Counts.fromJson(v as Map<String, dynamic>),
    ),
    dueToday: $checkedConvert(
      'dueToday',
      (v) => (v as List<dynamic>)
          .map((e) => CollectionItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    dueTodayCount: $checkedConvert('dueTodayCount', (v) => (v as num).toInt()),
    officers: $checkedConvert(
      'officers',
      (v) => (v as List<dynamic>?)
          ?.map((e) => OfficerSummary.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    overdue: $checkedConvert(
      'overdue',
      (v) => (v as List<dynamic>)
          .map((e) => CollectionItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    overdueCount: $checkedConvert('overdueCount', (v) => (v as num).toInt()),
    par: $checkedConvert('par', (v) => Par.fromJson(v as Map<String, dynamic>)),
    scope: $checkedConvert(
      'scope',
      (v) => Scope.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$PortfolioToJson(Portfolio instance) => <String, dynamic>{
  'amounts': instance.amounts.toJson(),
  'asOf': instance.asOf.toIso8601String(),
  'counts': instance.counts.toJson(),
  'dueToday': instance.dueToday.map((e) => e.toJson()).toList(),
  'dueTodayCount': instance.dueTodayCount,
  'officers': ?instance.officers?.map((e) => e.toJson()).toList(),
  'overdue': instance.overdue.map((e) => e.toJson()).toList(),
  'overdueCount': instance.overdueCount,
  'par': instance.par.toJson(),
  'scope': instance.scope.toJson(),
};
