// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'par.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ParCWProxy {
  Par par1(num par1);

  Par par1Principal(num par1Principal);

  Par par30(num par30);

  Par par30Principal(num par30Principal);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Par(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Par(...).copyWith(id: 12, name: "My name")
  /// ````
  Par call({num par1, num par1Principal, num par30, num par30Principal});
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfPar.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfPar.copyWith.fieldName(...)`
class _$ParCWProxyImpl implements _$ParCWProxy {
  const _$ParCWProxyImpl(this._value);

  final Par _value;

  @override
  Par par1(num par1) => this(par1: par1);

  @override
  Par par1Principal(num par1Principal) => this(par1Principal: par1Principal);

  @override
  Par par30(num par30) => this(par30: par30);

  @override
  Par par30Principal(num par30Principal) =>
      this(par30Principal: par30Principal);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Par(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Par(...).copyWith(id: 12, name: "My name")
  /// ````
  Par call({
    Object? par1 = const $CopyWithPlaceholder(),
    Object? par1Principal = const $CopyWithPlaceholder(),
    Object? par30 = const $CopyWithPlaceholder(),
    Object? par30Principal = const $CopyWithPlaceholder(),
  }) {
    return Par(
      par1: par1 == const $CopyWithPlaceholder()
          ? _value.par1
          // ignore: cast_nullable_to_non_nullable
          : par1 as num,
      par1Principal: par1Principal == const $CopyWithPlaceholder()
          ? _value.par1Principal
          // ignore: cast_nullable_to_non_nullable
          : par1Principal as num,
      par30: par30 == const $CopyWithPlaceholder()
          ? _value.par30
          // ignore: cast_nullable_to_non_nullable
          : par30 as num,
      par30Principal: par30Principal == const $CopyWithPlaceholder()
          ? _value.par30Principal
          // ignore: cast_nullable_to_non_nullable
          : par30Principal as num,
    );
  }
}

extension $ParCopyWith on Par {
  /// Returns a callable class that can be used as follows: `instanceOfPar.copyWith(...)` or like so:`instanceOfPar.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ParCWProxy get copyWith => _$ParCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Par _$ParFromJson(Map<String, dynamic> json) => $checkedCreate('Par', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const ['par1', 'par1Principal', 'par30', 'par30Principal'],
  );
  final val = Par(
    par1: $checkedConvert('par1', (v) => v as num),
    par1Principal: $checkedConvert('par1Principal', (v) => v as num),
    par30: $checkedConvert('par30', (v) => v as num),
    par30Principal: $checkedConvert('par30Principal', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$ParToJson(Par instance) => <String, dynamic>{
  'par1': instance.par1,
  'par1Principal': instance.par1Principal,
  'par30': instance.par30,
  'par30Principal': instance.par30Principal,
};
