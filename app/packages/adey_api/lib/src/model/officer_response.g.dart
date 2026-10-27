// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'officer_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OfficerResponseCWProxy {
  OfficerResponse displayName(String displayName);

  OfficerResponse officeId(int officeId);

  OfficerResponse officeName(String officeName);

  OfficerResponse roles(List<OfficerResponseRolesEnum> roles);

  OfficerResponse staffId(int staffId);

  OfficerResponse userId(int userId);

  OfficerResponse username(String username);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `OfficerResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// OfficerResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  OfficerResponse call({
    String displayName,
    int officeId,
    String officeName,
    List<OfficerResponseRolesEnum> roles,
    int staffId,
    int userId,
    String username,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfOfficerResponse.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfOfficerResponse.copyWith.fieldName(...)`
class _$OfficerResponseCWProxyImpl implements _$OfficerResponseCWProxy {
  const _$OfficerResponseCWProxyImpl(this._value);

  final OfficerResponse _value;

  @override
  OfficerResponse displayName(String displayName) =>
      this(displayName: displayName);

  @override
  OfficerResponse officeId(int officeId) => this(officeId: officeId);

  @override
  OfficerResponse officeName(String officeName) => this(officeName: officeName);

  @override
  OfficerResponse roles(List<OfficerResponseRolesEnum> roles) =>
      this(roles: roles);

  @override
  OfficerResponse staffId(int staffId) => this(staffId: staffId);

  @override
  OfficerResponse userId(int userId) => this(userId: userId);

  @override
  OfficerResponse username(String username) => this(username: username);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `OfficerResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// OfficerResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  OfficerResponse call({
    Object? displayName = const $CopyWithPlaceholder(),
    Object? officeId = const $CopyWithPlaceholder(),
    Object? officeName = const $CopyWithPlaceholder(),
    Object? roles = const $CopyWithPlaceholder(),
    Object? staffId = const $CopyWithPlaceholder(),
    Object? userId = const $CopyWithPlaceholder(),
    Object? username = const $CopyWithPlaceholder(),
  }) {
    return OfficerResponse(
      displayName: displayName == const $CopyWithPlaceholder()
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String,
      officeId: officeId == const $CopyWithPlaceholder()
          ? _value.officeId
          // ignore: cast_nullable_to_non_nullable
          : officeId as int,
      officeName: officeName == const $CopyWithPlaceholder()
          ? _value.officeName
          // ignore: cast_nullable_to_non_nullable
          : officeName as String,
      roles: roles == const $CopyWithPlaceholder()
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as List<OfficerResponseRolesEnum>,
      staffId: staffId == const $CopyWithPlaceholder()
          ? _value.staffId
          // ignore: cast_nullable_to_non_nullable
          : staffId as int,
      userId: userId == const $CopyWithPlaceholder()
          ? _value.userId
          // ignore: cast_nullable_to_non_nullable
          : userId as int,
      username: username == const $CopyWithPlaceholder()
          ? _value.username
          // ignore: cast_nullable_to_non_nullable
          : username as String,
    );
  }
}

extension $OfficerResponseCopyWith on OfficerResponse {
  /// Returns a callable class that can be used as follows: `instanceOfOfficerResponse.copyWith(...)` or like so:`instanceOfOfficerResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OfficerResponseCWProxy get copyWith => _$OfficerResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OfficerResponse _$OfficerResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OfficerResponse', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'displayName',
          'officeId',
          'officeName',
          'roles',
          'staffId',
          'userId',
          'username',
        ],
      );
      final val = OfficerResponse(
        displayName: $checkedConvert('displayName', (v) => v as String),
        officeId: $checkedConvert('officeId', (v) => (v as num).toInt()),
        officeName: $checkedConvert('officeName', (v) => v as String),
        roles: $checkedConvert(
          'roles',
          (v) => (v as List<dynamic>)
              .map(
                (e) => $enumDecode(
                  _$OfficerResponseRolesEnumEnumMap,
                  e,
                  unknownValue: OfficerResponseRolesEnum.unknownDefaultOpenApi,
                ),
              )
              .toList(),
        ),
        staffId: $checkedConvert('staffId', (v) => (v as num).toInt()),
        userId: $checkedConvert('userId', (v) => (v as num).toInt()),
        username: $checkedConvert('username', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$OfficerResponseToJson(OfficerResponse instance) =>
    <String, dynamic>{
      'displayName': instance.displayName,
      'officeId': instance.officeId,
      'officeName': instance.officeName,
      'roles': instance.roles
          .map((e) => _$OfficerResponseRolesEnumEnumMap[e]!)
          .toList(),
      'staffId': instance.staffId,
      'userId': instance.userId,
      'username': instance.username,
    };

const _$OfficerResponseRolesEnumEnumMap = {
  OfficerResponseRolesEnum.OFFICER: 'OFFICER',
  OfficerResponseRolesEnum.MANAGER: 'MANAGER',
  OfficerResponseRolesEnum.unknownDefaultOpenApi: 'unknown_default_open_api',
};
