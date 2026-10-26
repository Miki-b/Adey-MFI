// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'create_client_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CreateClientRequestCWProxy {
  CreateClientRequest activate(bool? activate);

  CreateClientRequest dateOfBirth(DateTime dateOfBirth);

  CreateClientRequest externalId(String? externalId);

  CreateClientRequest firstname(String firstname);

  CreateClientRequest gender(CreateClientRequestGenderEnum gender);

  CreateClientRequest lastname(String lastname);

  CreateClientRequest mobileNo(String mobileNo);

  CreateClientRequest staffId(int? staffId);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `CreateClientRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// CreateClientRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  CreateClientRequest call({
    bool? activate,
    DateTime dateOfBirth,
    String? externalId,
    String firstname,
    CreateClientRequestGenderEnum gender,
    String lastname,
    String mobileNo,
    int? staffId,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfCreateClientRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfCreateClientRequest.copyWith.fieldName(...)`
class _$CreateClientRequestCWProxyImpl implements _$CreateClientRequestCWProxy {
  const _$CreateClientRequestCWProxyImpl(this._value);

  final CreateClientRequest _value;

  @override
  CreateClientRequest activate(bool? activate) => this(activate: activate);

  @override
  CreateClientRequest dateOfBirth(DateTime dateOfBirth) =>
      this(dateOfBirth: dateOfBirth);

  @override
  CreateClientRequest externalId(String? externalId) =>
      this(externalId: externalId);

  @override
  CreateClientRequest firstname(String firstname) => this(firstname: firstname);

  @override
  CreateClientRequest gender(CreateClientRequestGenderEnum gender) =>
      this(gender: gender);

  @override
  CreateClientRequest lastname(String lastname) => this(lastname: lastname);

  @override
  CreateClientRequest mobileNo(String mobileNo) => this(mobileNo: mobileNo);

  @override
  CreateClientRequest staffId(int? staffId) => this(staffId: staffId);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `CreateClientRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// CreateClientRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  CreateClientRequest call({
    Object? activate = const $CopyWithPlaceholder(),
    Object? dateOfBirth = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? firstname = const $CopyWithPlaceholder(),
    Object? gender = const $CopyWithPlaceholder(),
    Object? lastname = const $CopyWithPlaceholder(),
    Object? mobileNo = const $CopyWithPlaceholder(),
    Object? staffId = const $CopyWithPlaceholder(),
  }) {
    return CreateClientRequest(
      activate: activate == const $CopyWithPlaceholder()
          ? _value.activate
          // ignore: cast_nullable_to_non_nullable
          : activate as bool?,
      dateOfBirth: dateOfBirth == const $CopyWithPlaceholder()
          ? _value.dateOfBirth
          // ignore: cast_nullable_to_non_nullable
          : dateOfBirth as DateTime,
      externalId: externalId == const $CopyWithPlaceholder()
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String?,
      firstname: firstname == const $CopyWithPlaceholder()
          ? _value.firstname
          // ignore: cast_nullable_to_non_nullable
          : firstname as String,
      gender: gender == const $CopyWithPlaceholder()
          ? _value.gender
          // ignore: cast_nullable_to_non_nullable
          : gender as CreateClientRequestGenderEnum,
      lastname: lastname == const $CopyWithPlaceholder()
          ? _value.lastname
          // ignore: cast_nullable_to_non_nullable
          : lastname as String,
      mobileNo: mobileNo == const $CopyWithPlaceholder()
          ? _value.mobileNo
          // ignore: cast_nullable_to_non_nullable
          : mobileNo as String,
      staffId: staffId == const $CopyWithPlaceholder()
          ? _value.staffId
          // ignore: cast_nullable_to_non_nullable
          : staffId as int?,
    );
  }
}

extension $CreateClientRequestCopyWith on CreateClientRequest {
  /// Returns a callable class that can be used as follows: `instanceOfCreateClientRequest.copyWith(...)` or like so:`instanceOfCreateClientRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CreateClientRequestCWProxy get copyWith =>
      _$CreateClientRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CreateClientRequest _$CreateClientRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('CreateClientRequest', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'dateOfBirth',
          'firstname',
          'gender',
          'lastname',
          'mobileNo',
        ],
      );
      final val = CreateClientRequest(
        activate: $checkedConvert('activate', (v) => v as bool?),
        dateOfBirth: $checkedConvert(
          'dateOfBirth',
          (v) => DateTime.parse(v as String),
        ),
        externalId: $checkedConvert('externalId', (v) => v as String?),
        firstname: $checkedConvert('firstname', (v) => v as String),
        gender: $checkedConvert(
          'gender',
          (v) => $enumDecode(
            _$CreateClientRequestGenderEnumEnumMap,
            v,
            unknownValue: CreateClientRequestGenderEnum.unknownDefaultOpenApi,
          ),
        ),
        lastname: $checkedConvert('lastname', (v) => v as String),
        mobileNo: $checkedConvert('mobileNo', (v) => v as String),
        staffId: $checkedConvert('staffId', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$CreateClientRequestToJson(
  CreateClientRequest instance,
) => <String, dynamic>{
  'activate': ?instance.activate,
  'dateOfBirth': instance.dateOfBirth.toIso8601String(),
  'externalId': ?instance.externalId,
  'firstname': instance.firstname,
  'gender': _$CreateClientRequestGenderEnumEnumMap[instance.gender]!,
  'lastname': instance.lastname,
  'mobileNo': instance.mobileNo,
  'staffId': ?instance.staffId,
};

const _$CreateClientRequestGenderEnumEnumMap = {
  CreateClientRequestGenderEnum.MALE: 'MALE',
  CreateClientRequestGenderEnum.FEMALE: 'FEMALE',
  CreateClientRequestGenderEnum.unknownDefaultOpenApi:
      'unknown_default_open_api',
};
