//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'office_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OfficeResponse {
  /// Returns a new [OfficeResponse] instance.
  OfficeResponse({

     this.id,

     this.name,

     this.openingDate,

     this.parentId,
  });

  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final int? id;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'openingDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? openingDate;



  @JsonKey(
    
    name: r'parentId',
    required: false,
    includeIfNull: false,
  )


  final int? parentId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OfficeResponse &&
      other.id == id &&
      other.name == name &&
      other.openingDate == openingDate &&
      other.parentId == parentId;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        openingDate.hashCode +
        parentId.hashCode;

  factory OfficeResponse.fromJson(Map<String, dynamic> json) => _$OfficeResponseFromJson(json);

  Map<String, dynamic> toJson() => _$OfficeResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

