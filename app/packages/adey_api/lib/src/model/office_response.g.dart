// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'office_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OfficeResponseCWProxy {
  OfficeResponse id(int id);

  OfficeResponse name(String name);

  OfficeResponse openingDate(DateTime? openingDate);

  OfficeResponse parentId(int? parentId);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `OfficeResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// OfficeResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  OfficeResponse call({
    int id,
    String name,
    DateTime? openingDate,
    int? parentId,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfOfficeResponse.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfOfficeResponse.copyWith.fieldName(...)`
class _$OfficeResponseCWProxyImpl implements _$OfficeResponseCWProxy {
  const _$OfficeResponseCWProxyImpl(this._value);

  final OfficeResponse _value;

  @override
  OfficeResponse id(int id) => this(id: id);

  @override
  OfficeResponse name(String name) => this(name: name);

  @override
  OfficeResponse openingDate(DateTime? openingDate) =>
      this(openingDate: openingDate);

  @override
  OfficeResponse parentId(int? parentId) => this(parentId: parentId);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `OfficeResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// OfficeResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  OfficeResponse call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? openingDate = const $CopyWithPlaceholder(),
    Object? parentId = const $CopyWithPlaceholder(),
  }) {
    return OfficeResponse(
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      openingDate: openingDate == const $CopyWithPlaceholder()
          ? _value.openingDate
          // ignore: cast_nullable_to_non_nullable
          : openingDate as DateTime?,
      parentId: parentId == const $CopyWithPlaceholder()
          ? _value.parentId
          // ignore: cast_nullable_to_non_nullable
          : parentId as int?,
    );
  }
}

extension $OfficeResponseCopyWith on OfficeResponse {
  /// Returns a callable class that can be used as follows: `instanceOfOfficeResponse.copyWith(...)` or like so:`instanceOfOfficeResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OfficeResponseCWProxy get copyWith => _$OfficeResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OfficeResponse _$OfficeResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('OfficeResponse', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'name']);
      final val = OfficeResponse(
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        name: $checkedConvert('name', (v) => v as String),
        openingDate: $checkedConvert(
          'openingDate',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        parentId: $checkedConvert('parentId', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$OfficeResponseToJson(OfficeResponse instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'openingDate': ?instance.openingDate?.toIso8601String(),
      'parentId': ?instance.parentId,
    };
