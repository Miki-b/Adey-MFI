// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'scope.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ScopeCWProxy {
  Scope officeId(int officeId);

  Scope officeName(String officeName);

  Scope officerName(String? officerName);

  Scope role(ScopeRoleEnum role);

  Scope staffId(int? staffId);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Scope(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Scope(...).copyWith(id: 12, name: "My name")
  /// ````
  Scope call({
    int officeId,
    String officeName,
    String? officerName,
    ScopeRoleEnum role,
    int? staffId,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfScope.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfScope.copyWith.fieldName(...)`
class _$ScopeCWProxyImpl implements _$ScopeCWProxy {
  const _$ScopeCWProxyImpl(this._value);

  final Scope _value;

  @override
  Scope officeId(int officeId) => this(officeId: officeId);

  @override
  Scope officeName(String officeName) => this(officeName: officeName);

  @override
  Scope officerName(String? officerName) => this(officerName: officerName);

  @override
  Scope role(ScopeRoleEnum role) => this(role: role);

  @override
  Scope staffId(int? staffId) => this(staffId: staffId);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Scope(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Scope(...).copyWith(id: 12, name: "My name")
  /// ````
  Scope call({
    Object? officeId = const $CopyWithPlaceholder(),
    Object? officeName = const $CopyWithPlaceholder(),
    Object? officerName = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
    Object? staffId = const $CopyWithPlaceholder(),
  }) {
    return Scope(
      officeId: officeId == const $CopyWithPlaceholder()
          ? _value.officeId
          // ignore: cast_nullable_to_non_nullable
          : officeId as int,
      officeName: officeName == const $CopyWithPlaceholder()
          ? _value.officeName
          // ignore: cast_nullable_to_non_nullable
          : officeName as String,
      officerName: officerName == const $CopyWithPlaceholder()
          ? _value.officerName
          // ignore: cast_nullable_to_non_nullable
          : officerName as String?,
      role: role == const $CopyWithPlaceholder()
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as ScopeRoleEnum,
      staffId: staffId == const $CopyWithPlaceholder()
          ? _value.staffId
          // ignore: cast_nullable_to_non_nullable
          : staffId as int?,
    );
  }
}

extension $ScopeCopyWith on Scope {
  /// Returns a callable class that can be used as follows: `instanceOfScope.copyWith(...)` or like so:`instanceOfScope.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ScopeCWProxy get copyWith => _$ScopeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Scope _$ScopeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Scope', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['officeId', 'officeName', 'role']);
      final val = Scope(
        officeId: $checkedConvert('officeId', (v) => (v as num).toInt()),
        officeName: $checkedConvert('officeName', (v) => v as String),
        officerName: $checkedConvert('officerName', (v) => v as String?),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(
            _$ScopeRoleEnumEnumMap,
            v,
            unknownValue: ScopeRoleEnum.unknownDefaultOpenApi,
          ),
        ),
        staffId: $checkedConvert('staffId', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$ScopeToJson(Scope instance) => <String, dynamic>{
  'officeId': instance.officeId,
  'officeName': instance.officeName,
  'officerName': ?instance.officerName,
  'role': _$ScopeRoleEnumEnumMap[instance.role]!,
  'staffId': ?instance.staffId,
};

const _$ScopeRoleEnumEnumMap = {
  ScopeRoleEnum.OFFICER: 'OFFICER',
  ScopeRoleEnum.MANAGER: 'MANAGER',
  ScopeRoleEnum.unknownDefaultOpenApi: 'unknown_default_open_api',
};
