//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resolve_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResolveRequest {
  /// Returns a new [ResolveRequest] instance.
  ResolveRequest({

     this.note,
  });

  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResolveRequest &&
      other.note == note;

    @override
    int get hashCode =>
        note.hashCode;

  factory ResolveRequest.fromJson(Map<String, dynamic> json) => _$ResolveRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ResolveRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

