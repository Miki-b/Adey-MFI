//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'create_client_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CreateClientRequest {
  /// Returns a new [CreateClientRequest] instance.
  CreateClientRequest({

     this.activate,

    required  this.dateOfBirth,

     this.externalId,

    required  this.firstname,

    required  this.gender,

    required  this.lastname,

    required  this.mobileNo,

     this.staffId,
  });

      /// Activate immediately (default true). Inactive clients cannot get loans.
  @JsonKey(
    
    name: r'activate',
    required: false,
    includeIfNull: false,
  )


  final bool? activate;



      /// ISO date. The client must be at least 18 years old.
  @JsonKey(
    
    name: r'dateOfBirth',
    required: true,
    includeIfNull: false,
  )


  final DateTime dateOfBirth;



      /// National ID number (stored as Fineract externalId). Optional, must be unique.
  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'firstname',
    required: true,
    includeIfNull: false,
  )


  final String firstname;



  @JsonKey(
    
    name: r'gender',
    required: true,
    includeIfNull: false,
  unknownEnumValue: CreateClientRequestGenderEnum.unknownDefaultOpenApi,
  )


  final CreateClientRequestGenderEnum gender;



  @JsonKey(
    
    name: r'lastname',
    required: true,
    includeIfNull: false,
  )


  final String lastname;



      /// Ethiopian mobile number, 10 digits starting 09 or 07
  @JsonKey(
    
    name: r'mobileNo',
    required: true,
    includeIfNull: false,
  )


  final String mobileNo;



      /// Managers only: the loan officer (staff id) of your office to assign. Officers leave it empty; the client is assigned to them.
          // minimum: 0
  @JsonKey(
    
    name: r'staffId',
    required: false,
    includeIfNull: false,
  )


  final int? staffId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CreateClientRequest &&
      other.activate == activate &&
      other.dateOfBirth == dateOfBirth &&
      other.externalId == externalId &&
      other.firstname == firstname &&
      other.gender == gender &&
      other.lastname == lastname &&
      other.mobileNo == mobileNo &&
      other.staffId == staffId;

    @override
    int get hashCode =>
        activate.hashCode +
        dateOfBirth.hashCode +
        externalId.hashCode +
        firstname.hashCode +
        gender.hashCode +
        lastname.hashCode +
        mobileNo.hashCode +
        staffId.hashCode;

  factory CreateClientRequest.fromJson(Map<String, dynamic> json) => _$CreateClientRequestFromJson(json);

  Map<String, dynamic> toJson() => _$CreateClientRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum CreateClientRequestGenderEnum {
@JsonValue(r'MALE')
MALE(r'MALE'),
@JsonValue(r'FEMALE')
FEMALE(r'FEMALE'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const CreateClientRequestGenderEnum(this.value);

final String value;

@override
String toString() => value;
}


