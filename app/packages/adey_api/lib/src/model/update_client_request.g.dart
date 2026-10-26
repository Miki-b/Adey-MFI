// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'update_client_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UpdateClientRequestCWProxy {
  UpdateClientRequest dateOfBirth(DateTime? dateOfBirth);

  UpdateClientRequest empty(bool? empty);

  UpdateClientRequest externalId(String? externalId);

  UpdateClientRequest firstname(String? firstname);

  UpdateClientRequest gender(UpdateClientRequestGenderEnum? gender);

  UpdateClientRequest lastname(String? lastname);

  UpdateClientRequest mobileNo(String? mobileNo);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `UpdateClientRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// UpdateClientRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  UpdateClientRequest call({
    DateTime? dateOfBirth,
    bool? empty,
    String? externalId,
    String? firstname,
    UpdateClientRequestGenderEnum? gender,
    String? lastname,
    String? mobileNo,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfUpdateClientRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfUpdateClientRequest.copyWith.fieldName(...)`
class _$UpdateClientRequestCWProxyImpl implements _$UpdateClientRequestCWProxy {
  const _$UpdateClientRequestCWProxyImpl(this._value);

  final UpdateClientRequest _value;

  @override
  UpdateClientRequest dateOfBirth(DateTime? dateOfBirth) =>
      this(dateOfBirth: dateOfBirth);

  @override
  UpdateClientRequest empty(bool? empty) => this(empty: empty);

  @override
  UpdateClientRequest externalId(String? externalId) =>
      this(externalId: externalId);

  @override
  UpdateClientRequest firstname(String? firstname) =>
      this(firstname: firstname);

  @override
  UpdateClientRequest gender(UpdateClientRequestGenderEnum? gender) =>
      this(gender: gender);

  @override
  UpdateClientRequest lastname(String? lastname) => this(lastname: lastname);

  @override
  UpdateClientRequest mobileNo(String? mobileNo) => this(mobileNo: mobileNo);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `UpdateClientRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// UpdateClientRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  UpdateClientRequest call({
    Object? dateOfBirth = const $CopyWithPlaceholder(),
    Object? empty = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? firstname = const $CopyWithPlaceholder(),
    Object? gender = const $CopyWithPlaceholder(),
    Object? lastname = const $CopyWithPlaceholder(),
    Object? mobileNo = const $CopyWithPlaceholder(),
  }) {
    return UpdateClientRequest(
      dateOfBirth: dateOfBirth == const $CopyWithPlaceholder()
          ? _value.dateOfBirth
          // ignore: cast_nullable_to_non_nullable
          : dateOfBirth as DateTime?,
      empty: empty == const $CopyWithPlaceholder()
          ? _value.empty
          // ignore: cast_nullable_to_non_nullable
          : empty as bool?,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      firstname: firstname == const $CopyWithPlaceholder()
          ? _value.firstname
          // ignore: cast_nullable_to_non_nullable
          : firstname as String?,
      gender: gender == const $CopyWithPlaceholder()
          ? _value.gender
          // ignore: cast_nullable_to_non_nullable
          : gender as UpdateClientRequestGenderEnum?,
      lastname: lastname == const $CopyWithPlaceholder()
          ? _value.lastname
          // ignore: cast_nullable_to_non_nullable
          : lastname as String?,
      mobileNo: mobileNo == const $CopyWithPlaceholder()
          ? _value.mobileNo
          // ignore: cast_nullable_to_non_nullable
          : mobileNo as String?,
    );
  }
}

extension $UpdateClientRequestCopyWith on UpdateClientRequest {
  /// Returns a callable class that can be used as follows: `instanceOfUpdateClientRequest.copyWith(...)` or like so:`instanceOfUpdateClientRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UpdateClientRequestCWProxy get copyWith =>
      _$UpdateClientRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UpdateClientRequest _$UpdateClientRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UpdateClientRequest', json, ($checkedConvert) {
      final val = UpdateClientRequest(
        dateOfBirth: $checkedConvert(
          'dateOfBirth',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        empty: $checkedConvert('empty', (v) => v as bool?),
        externalId: $checkedConvert('externalId', (v) => v as String?),
        firstname: $checkedConvert('firstname', (v) => v as String?),
        gender: $checkedConvert(
          'gender',
          (v) => $enumDecodeNullable(
            _$UpdateClientRequestGenderEnumEnumMap,
            v,
            unknownValue: UpdateClientRequestGenderEnum.unknownDefaultOpenApi,
          ),
        ),
        lastname: $checkedConvert('lastname', (v) => v as String?),
        mobileNo: $checkedConvert('mobileNo', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$UpdateClientRequestToJson(
  UpdateClientRequest instance,
) => <String, dynamic>{
  'dateOfBirth': ?instance.dateOfBirth?.toIso8601String(),
  'empty': ?instance.empty,
  'externalId': ?instance.externalId,
  'firstname': ?instance.firstname,
  'gender': ?_$UpdateClientRequestGenderEnumEnumMap[instance.gender],
  'lastname': ?instance.lastname,
  'mobileNo': ?instance.mobileNo,
};

const _$UpdateClientRequestGenderEnumEnumMap = {
  UpdateClientRequestGenderEnum.MALE: 'MALE',
  UpdateClientRequestGenderEnum.FEMALE: 'FEMALE',
  UpdateClientRequestGenderEnum.unknownDefaultOpenApi:
      'unknown_default_open_api',
};
