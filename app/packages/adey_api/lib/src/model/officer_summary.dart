//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'officer_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OfficerSummary {
  /// Returns a new [OfficerSummary] instance.
  OfficerSummary({

     this.activeLoans,

     this.clients,

     this.officerName,

     this.par30,

     this.principalOutstanding,

     this.staffId,

     this.totalOverdue,
  });

  @JsonKey(
    
    name: r'activeLoans',
    required: false,
    includeIfNull: false,
  )


  final int? activeLoans;



  @JsonKey(
    
    name: r'clients',
    required: false,
    includeIfNull: false,
  )


  final int? clients;



  @JsonKey(
    
    name: r'officerName',
    required: false,
    includeIfNull: false,
  )


  final String? officerName;



  @JsonKey(
    
    name: r'par30',
    required: false,
    includeIfNull: false,
  )


  final num? par30;



  @JsonKey(
    
    name: r'principalOutstanding',
    required: false,
    includeIfNull: false,
  )


  final num? principalOutstanding;



  @JsonKey(
    
    name: r'staffId',
    required: false,
    includeIfNull: false,
  )


  final int? staffId;



  @JsonKey(
    
    name: r'totalOverdue',
    required: false,
    includeIfNull: false,
  )


  final num? totalOverdue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OfficerSummary &&
      other.activeLoans == activeLoans &&
      other.clients == clients &&
      other.officerName == officerName &&
      other.par30 == par30 &&
      other.principalOutstanding == principalOutstanding &&
      other.staffId == staffId &&
      other.totalOverdue == totalOverdue;

    @override
    int get hashCode =>
        activeLoans.hashCode +
        clients.hashCode +
        officerName.hashCode +
        par30.hashCode +
        principalOutstanding.hashCode +
        staffId.hashCode +
        totalOverdue.hashCode;

  factory OfficerSummary.fromJson(Map<String, dynamic> json) => _$OfficerSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$OfficerSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

