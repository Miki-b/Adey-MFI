//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'client_details.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClientDetails {
  /// Returns a new [ClientDetails] instance.
  ClientDetails({

    required  this.accountNo,

     this.activationDate,

    required  this.active,

     this.dateOfBirth,

    required  this.displayName,

     this.externalId,

     this.firstName,

     this.gender,

    required  this.id,

     this.lastName,

     this.middleName,

     this.mobileNo,

    required  this.officeId,

    required  this.officeName,

     this.staffId,

     this.staffName,

     this.status,

     this.submittedOnDate,
  });

  @JsonKey(
    
    name: r'accountNo',
    required: true,
    includeIfNull: false,
  )


  final String accountNo;



  @JsonKey(
    
    name: r'activationDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? activationDate;



  @JsonKey(
    
    name: r'active',
    required: true,
    includeIfNull: false,
  )


  final bool active;



  @JsonKey(
    
    name: r'dateOfBirth',
    required: false,
    includeIfNull: false,
  )


  final DateTime? dateOfBirth;



  @JsonKey(
    
    name: r'displayName',
    required: true,
    includeIfNull: false,
  )


  final String displayName;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;



  @JsonKey(
    
    name: r'firstName',
    required: false,
    includeIfNull: false,
  )


  final String? firstName;



  @JsonKey(
    
    name: r'gender',
    required: false,
    includeIfNull: false,
  )


  final String? gender;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'lastName',
    required: false,
    includeIfNull: false,
  )


  final String? lastName;



  @JsonKey(
    
    name: r'middleName',
    required: false,
    includeIfNull: false,
  )


  final String? middleName;



  @JsonKey(
    
    name: r'mobileNo',
    required: false,
    includeIfNull: false,
  )


  final String? mobileNo;



  @JsonKey(
    
    name: r'officeId',
    required: true,
    includeIfNull: false,
  )


  final int officeId;



  @JsonKey(
    
    name: r'officeName',
    required: true,
    includeIfNull: false,
  )


  final String officeName;



  @JsonKey(
    
    name: r'staffId',
    required: false,
    includeIfNull: false,
  )


  final int? staffId;



  @JsonKey(
    
    name: r'staffName',
    required: false,
    includeIfNull: false,
  )


  final String? staffName;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final String? status;



  @JsonKey(
    
    name: r'submittedOnDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? submittedOnDate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClientDetails &&
      other.accountNo == accountNo &&
      other.activationDate == activationDate &&
      other.active == active &&
      other.dateOfBirth == dateOfBirth &&
      other.displayName == displayName &&
      other.externalId == externalId &&
      other.firstName == firstName &&
      other.gender == gender &&
      other.id == id &&
      other.lastName == lastName &&
      other.middleName == middleName &&
      other.mobileNo == mobileNo &&
      other.officeId == officeId &&
      other.officeName == officeName &&
      other.staffId == staffId &&
      other.staffName == staffName &&
      other.status == status &&
      other.submittedOnDate == submittedOnDate;

    @override
    int get hashCode =>
        accountNo.hashCode +
        activationDate.hashCode +
        active.hashCode +
        dateOfBirth.hashCode +
        displayName.hashCode +
        externalId.hashCode +
        firstName.hashCode +
        gender.hashCode +
        id.hashCode +
        lastName.hashCode +
        middleName.hashCode +
        mobileNo.hashCode +
        officeId.hashCode +
        officeName.hashCode +
        staffId.hashCode +
        staffName.hashCode +
        status.hashCode +
        submittedOnDate.hashCode;

  factory ClientDetails.fromJson(Map<String, dynamic> json) => _$ClientDetailsFromJson(json);

  Map<String, dynamic> toJson() => _$ClientDetailsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

