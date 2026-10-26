//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'update_client_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UpdateClientRequest {
  /// Returns a new [UpdateClientRequest] instance.
  UpdateClientRequest({

     this.dateOfBirth,

     this.empty,

     this.externalId,

     this.firstname,

     this.gender,

     this.lastname,

     this.mobileNo,
  });

  @JsonKey(
    
    name: r'dateOfBirth',
    required: false,
    includeIfNull: false,
  )


  final DateTime? dateOfBirth;



  @JsonKey(
    
    name: r'empty',
    required: false,
    includeIfNull: false,
  )


  final bool? empty;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'firstname',
    required: false,
    includeIfNull: false,
  )


  final String? firstname;



  @JsonKey(
    
    name: r'gender',
    required: false,
    includeIfNull: false,
  unknownEnumValue: UpdateClientRequestGenderEnum.unknownDefaultOpenApi,
  )


  final UpdateClientRequestGenderEnum? gender;



  @JsonKey(
    
    name: r'lastname',
    required: false,
    includeIfNull: false,
  )


  final String? lastname;



  @JsonKey(
    
    name: r'mobileNo',
    required: false,
    includeIfNull: false,
  )


  final String? mobileNo;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UpdateClientRequest &&
      other.dateOfBirth == dateOfBirth &&
      other.empty == empty &&
      other.externalId == externalId &&
      other.firstname == firstname &&
      other.gender == gender &&
      other.lastname == lastname &&
      other.mobileNo == mobileNo;

    @override
    int get hashCode =>
        dateOfBirth.hashCode +
        empty.hashCode +
        externalId.hashCode +
        firstname.hashCode +
        gender.hashCode +
        lastname.hashCode +
        mobileNo.hashCode;

  factory UpdateClientRequest.fromJson(Map<String, dynamic> json) => _$UpdateClientRequestFromJson(json);

  Map<String, dynamic> toJson() => _$UpdateClientRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum UpdateClientRequestGenderEnum {
@JsonValue(r'MALE')
MALE(r'MALE'),
@JsonValue(r'FEMALE')
FEMALE(r'FEMALE'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const UpdateClientRequestGenderEnum(this.value);

final String value;

@override
String toString() => value;
}


