//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'counts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Counts {
  /// Returns a new [Counts] instance.
  Counts({

    required  this.activeLoans,

    required  this.approvedNotDisbursed,

    required  this.clients,

    required  this.loansInArrears,

    required  this.pendingApplications,
  });

  @JsonKey(
    
    name: r'activeLoans',
    required: true,
    includeIfNull: false,
  )


  final int activeLoans;



  @JsonKey(
    
    name: r'approvedNotDisbursed',
    required: true,
    includeIfNull: false,
  )


  final int approvedNotDisbursed;



  @JsonKey(
    
    name: r'clients',
    required: true,
    includeIfNull: false,
  )


  final int clients;



  @JsonKey(
    
    name: r'loansInArrears',
    required: true,
    includeIfNull: false,
  )


  final int loansInArrears;



  @JsonKey(
    
    name: r'pendingApplications',
    required: true,
    includeIfNull: false,
  )


  final int pendingApplications;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Counts &&
      other.activeLoans == activeLoans &&
      other.approvedNotDisbursed == approvedNotDisbursed &&
      other.clients == clients &&
      other.loansInArrears == loansInArrears &&
      other.pendingApplications == pendingApplications;

    @override
    int get hashCode =>
        activeLoans.hashCode +
        approvedNotDisbursed.hashCode +
        clients.hashCode +
        loansInArrears.hashCode +
        pendingApplications.hashCode;

  factory Counts.fromJson(Map<String, dynamic> json) => _$CountsFromJson(json);

  Map<String, dynamic> toJson() => _$CountsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

