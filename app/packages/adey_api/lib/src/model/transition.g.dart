// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'transition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TransitionCWProxy {
  Transition at(DateTime at);

  Transition from(TransitionFromEnum? from);

  Transition note(String? note);

  Transition to(TransitionToEnum to);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Transition(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Transition(...).copyWith(id: 12, name: "My name")
  /// ````
  Transition call({
    DateTime at,
    TransitionFromEnum? from,
    String? note,
    TransitionToEnum to,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfTransition.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfTransition.copyWith.fieldName(...)`
class _$TransitionCWProxyImpl implements _$TransitionCWProxy {
  const _$TransitionCWProxyImpl(this._value);

  final Transition _value;

  @override
  Transition at(DateTime at) => this(at: at);

  @override
  Transition from(TransitionFromEnum? from) => this(from: from);

  @override
  Transition note(String? note) => this(note: note);

  @override
  Transition to(TransitionToEnum to) => this(to: to);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Transition(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Transition(...).copyWith(id: 12, name: "My name")
  /// ````
  Transition call({
    Object? at = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? note = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return Transition(
      at: at == const $CopyWithPlaceholder()
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as TransitionFromEnum?,
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as TransitionToEnum,
    );
  }
}

extension $TransitionCopyWith on Transition {
  /// Returns a callable class that can be used as follows: `instanceOfTransition.copyWith(...)` or like so:`instanceOfTransition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TransitionCWProxy get copyWith => _$TransitionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Transition _$TransitionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Transition', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['at', 'to']);
      final val = Transition(
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
        from: $checkedConvert(
          'from',
          (v) => $enumDecodeNullable(
            _$TransitionFromEnumEnumMap,
            v,
            unknownValue: TransitionFromEnum.unknownDefaultOpenApi,
          ),
        ),
        note: $checkedConvert('note', (v) => v as String?),
        to: $checkedConvert(
          'to',
          (v) => $enumDecode(
            _$TransitionToEnumEnumMap,
            v,
            unknownValue: TransitionToEnum.unknownDefaultOpenApi,
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TransitionToJson(Transition instance) =>
    <String, dynamic>{
      'at': instance.at.toIso8601String(),
      'from': ?_$TransitionFromEnumEnumMap[instance.from],
      'note': ?instance.note,
      'to': _$TransitionToEnumEnumMap[instance.to]!,
    };

const _$TransitionFromEnumEnumMap = {
  TransitionFromEnum.PENDING: 'PENDING',
  TransitionFromEnum.DECLINED: 'DECLINED',
  TransitionFromEnum.CHARGE_FAILED: 'CHARGE_FAILED',
  TransitionFromEnum.MISMATCH: 'MISMATCH',
  TransitionFromEnum.RECEIVED: 'RECEIVED',
  TransitionFromEnum.RETRYING: 'RETRYING',
  TransitionFromEnum.POSTED: 'POSTED',
  TransitionFromEnum.NEEDS_ATTENTION: 'NEEDS_ATTENTION',
  TransitionFromEnum.unknownDefaultOpenApi: 'unknown_default_open_api',
};

const _$TransitionToEnumEnumMap = {
  TransitionToEnum.PENDING: 'PENDING',
  TransitionToEnum.DECLINED: 'DECLINED',
  TransitionToEnum.CHARGE_FAILED: 'CHARGE_FAILED',
  TransitionToEnum.MISMATCH: 'MISMATCH',
  TransitionToEnum.RECEIVED: 'RECEIVED',
  TransitionToEnum.RETRYING: 'RETRYING',
  TransitionToEnum.POSTED: 'POSTED',
  TransitionToEnum.NEEDS_ATTENTION: 'NEEDS_ATTENTION',
  TransitionToEnum.unknownDefaultOpenApi: 'unknown_default_open_api',
};
