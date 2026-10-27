//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'client_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClientSummary {
  /// Returns a new [ClientSummary] instance.
  ClientSummary({

    required  this.accountNo,

     this.activationDate,

    required  this.active,

    required  this.displayName,

    required  this.id,

    required  this.officeName,

     this.staffName,
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
    
    name: r'displayName',
    required: true,
    includeIfNull: false,
  )


  final String displayName;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'officeName',
    required: true,
    includeIfNull: false,
  )


  final String officeName;



  @JsonKey(
    
    name: r'staffName',
    required: false,
    includeIfNull: false,
  )


  final String? staffName;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClientSummary &&
      other.accountNo == accountNo &&
      other.activationDate == activationDate &&
      other.active == active &&
      other.displayName == displayName &&
      other.id == id &&
      other.officeName == officeName &&
      other.staffName == staffName;

    @override
    int get hashCode =>
        accountNo.hashCode +
        activationDate.hashCode +
        active.hashCode +
        displayName.hashCode +
        id.hashCode +
        officeName.hashCode +
        staffName.hashCode;

  factory ClientSummary.fromJson(Map<String, dynamic> json) => _$ClientSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$ClientSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

