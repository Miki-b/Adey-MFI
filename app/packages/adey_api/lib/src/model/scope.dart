//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'scope.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Scope {
  /// Returns a new [Scope] instance.
  Scope({

     this.officeId,

     this.officeName,

     this.officerName,

     this.role,

     this.staffId,
  });

  @JsonKey(
    
    name: r'officeId',
    required: false,
    includeIfNull: false,
  )


  final int? officeId;



  @JsonKey(
    
    name: r'officeName',
    required: false,
    includeIfNull: false,
  )


  final String? officeName;



  @JsonKey(
    
    name: r'officerName',
    required: false,
    includeIfNull: false,
  )


  final String? officerName;



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final String? role;



  @JsonKey(
    
    name: r'staffId',
    required: false,
    includeIfNull: false,
  )


  final int? staffId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Scope &&
      other.officeId == officeId &&
      other.officeName == officeName &&
      other.officerName == officerName &&
      other.role == role &&
      other.staffId == staffId;

    @override
    int get hashCode =>
        officeId.hashCode +
        officeName.hashCode +
        officerName.hashCode +
        role.hashCode +
        staffId.hashCode;

  factory Scope.fromJson(Map<String, dynamic> json) => _$ScopeFromJson(json);

  Map<String, dynamic> toJson() => _$ScopeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

