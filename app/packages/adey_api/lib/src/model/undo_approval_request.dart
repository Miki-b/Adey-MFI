//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'undo_approval_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UndoApprovalRequest {
  /// Returns a new [UndoApprovalRequest] instance.
  UndoApprovalRequest({

     this.note,
  });

  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UndoApprovalRequest &&
      other.note == note;

    @override
    int get hashCode =>
        note.hashCode;

  factory UndoApprovalRequest.fromJson(Map<String, dynamic> json) => _$UndoApprovalRequestFromJson(json);

  Map<String, dynamic> toJson() => _$UndoApprovalRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

