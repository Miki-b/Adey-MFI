//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dates.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Dates {
  /// Returns a new [Dates] instance.
  Dates({

     this.approvedOn,

     this.closedOn,

     this.disbursedOn,

     this.expectedDisbursement,

     this.expectedMaturity,

     this.rejectedOn,

    required  this.submittedOn,
  });

  @JsonKey(
    
    name: r'approvedOn',
    required: false,
    includeIfNull: false,
  )


  final DateTime? approvedOn;



  @JsonKey(
    
    name: r'closedOn',
    required: false,
    includeIfNull: false,
  )


  final DateTime? closedOn;



  @JsonKey(
    
    name: r'disbursedOn',
    required: false,
    includeIfNull: false,
  )


  final DateTime? disbursedOn;



  @JsonKey(
    
    name: r'expectedDisbursement',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expectedDisbursement;



  @JsonKey(
    
    name: r'expectedMaturity',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expectedMaturity;



  @JsonKey(
    
    name: r'rejectedOn',
    required: false,
    includeIfNull: false,
  )


  final DateTime? rejectedOn;



  @JsonKey(
    
    name: r'submittedOn',
    required: true,
    includeIfNull: false,
  )


  final DateTime submittedOn;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Dates &&
      other.approvedOn == approvedOn &&
      other.closedOn == closedOn &&
      other.disbursedOn == disbursedOn &&
      other.expectedDisbursement == expectedDisbursement &&
      other.expectedMaturity == expectedMaturity &&
      other.rejectedOn == rejectedOn &&
      other.submittedOn == submittedOn;

    @override
    int get hashCode =>
        approvedOn.hashCode +
        closedOn.hashCode +
        disbursedOn.hashCode +
        expectedDisbursement.hashCode +
        expectedMaturity.hashCode +
        rejectedOn.hashCode +
        submittedOn.hashCode;

  factory Dates.fromJson(Map<String, dynamic> json) => _$DatesFromJson(json);

  Map<String, dynamic> toJson() => _$DatesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

