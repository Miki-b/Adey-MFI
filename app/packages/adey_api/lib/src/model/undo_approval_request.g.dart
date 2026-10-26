// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'undo_approval_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UndoApprovalRequestCWProxy {
  UndoApprovalRequest note(String? note);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `UndoApprovalRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// UndoApprovalRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  UndoApprovalRequest call({String? note});
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfUndoApprovalRequest.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfUndoApprovalRequest.copyWith.fieldName(...)`
class _$UndoApprovalRequestCWProxyImpl implements _$UndoApprovalRequestCWProxy {
  const _$UndoApprovalRequestCWProxyImpl(this._value);

  final UndoApprovalRequest _value;

  @override
  UndoApprovalRequest note(String? note) => this(note: note);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `UndoApprovalRequest(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// UndoApprovalRequest(...).copyWith(id: 12, name: "My name")
  /// ````
  UndoApprovalRequest call({Object? note = const $CopyWithPlaceholder()}) {
    return UndoApprovalRequest(
      note: note == const $CopyWithPlaceholder()
          ? _value.note
          // ignore: cast_nullable_to_non_nullable
          : note as String?,
    );
  }
}

extension $UndoApprovalRequestCopyWith on UndoApprovalRequest {
  /// Returns a callable class that can be used as follows: `instanceOfUndoApprovalRequest.copyWith(...)` or like so:`instanceOfUndoApprovalRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UndoApprovalRequestCWProxy get copyWith =>
      _$UndoApprovalRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UndoApprovalRequest _$UndoApprovalRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UndoApprovalRequest', json, ($checkedConvert) {
      final val = UndoApprovalRequest(
        note: $checkedConvert('note', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$UndoApprovalRequestToJson(
  UndoApprovalRequest instance,
) => <String, dynamic>{'note': ?instance.note};
