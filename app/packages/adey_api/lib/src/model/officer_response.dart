//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'officer_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OfficerResponse {
  /// Returns a new [OfficerResponse] instance.
  OfficerResponse({

     this.displayName,

     this.officeId,

     this.officeName,

     this.roles,

     this.staffId,

     this.userId,

     this.username,
  });

  @JsonKey(
    
    name: r'displayName',
    required: false,
    includeIfNull: false,
  )


  final String? displayName;



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
    
    name: r'roles',
    required: false,
    includeIfNull: false,
  unknownEnumValue: OfficerResponseRolesEnum.unknownDefaultOpenApi,
  )


  final List<OfficerResponseRolesEnum>? roles;



  @JsonKey(
    
    name: r'staffId',
    required: false,
    includeIfNull: false,
  )


  final int? staffId;



  @JsonKey(
    
    name: r'userId',
    required: false,
    includeIfNull: false,
  )


  final int? userId;



  @JsonKey(
    
    name: r'username',
    required: false,
    includeIfNull: false,
  )


  final String? username;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OfficerResponse &&
      other.displayName == displayName &&
      other.officeId == officeId &&
      other.officeName == officeName &&
      other.roles == roles &&
      other.staffId == staffId &&
      other.userId == userId &&
      other.username == username;

    @override
    int get hashCode =>
        displayName.hashCode +
        officeId.hashCode +
        officeName.hashCode +
        roles.hashCode +
        staffId.hashCode +
        userId.hashCode +
        username.hashCode;

  factory OfficerResponse.fromJson(Map<String, dynamic> json) => _$OfficerResponseFromJson(json);

  Map<String, dynamic> toJson() => _$OfficerResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OfficerResponseRolesEnum {
@JsonValue(r'OFFICER')
OFFICER(r'OFFICER'),
@JsonValue(r'MANAGER')
MANAGER(r'MANAGER'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const OfficerResponseRolesEnum(this.value);

final String value;

@override
String toString() => value;
}


