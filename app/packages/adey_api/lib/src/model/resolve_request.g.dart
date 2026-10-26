// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'resolve_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ResolveRequestCWProxy {
  ResolveRequest note(String? note);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ResolveRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ResolveRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  ResolveRequest call({String? note});
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfResolveRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfResolveRequest.copyWith.fieldName(...)`
class _$ResolveRequestCWProxyImpl implements _$ResolveRequestCWProxy {
  const _$ResolveRequestCWProxyImpl(this._value);

  final ResolveRequest _value;

  @override
  ResolveRequest note(String? note) => this(note: note);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `ResolveRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// ResolveRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  ResolveRequest call({Object? note = const $CopyWithPlaceholder()}) {
    return ResolveRequest(
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $ResolveRequestCopyWith on ResolveRequest {
  /// Returns a callable class that can be used as follows: `instanceOfResolveRequest.copyWith(...)` or like so:`instanceOfResolveRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ResolveRequestCWProxy get copyWith => _$ResolveRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ResolveRequest _$ResolveRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ResolveRequest', json, ($checkedConvert) {
      final val = ResolveRequest(
        note: $checkedConvert('note', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ResolveRequestToJson(ResolveRequest instance) =>
    <String, dynamic>{'note': ?instance.note};
