//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:adey_api/src/model/finding_response.dart';
import 'package:adey_api/src/model/reconciliation_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reconciliation_report.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReconciliationReport {
  /// Returns a new [ReconciliationReport] instance.
  ReconciliationReport({

    required  this.date,

    required  this.findings,

    required  this.runAt,

    required  this.runId,

    required  this.summary,

    required  this.triggeredBy,
  });

  @JsonKey(
    
    name: r'date',
    required: true,
    includeIfNull: false,
  )


  final DateTime date;



  @JsonKey(
    
    name: r'findings',
    required: true,
    includeIfNull: false,
  )


  final List<FindingResponse> findings;



  @JsonKey(
    
    name: r'runAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime runAt;



  @JsonKey(
    
    name: r'runId',
    required: true,
    includeIfNull: false,
  )


  final int runId;



  @JsonKey(
    
    name: r'summary',
    required: true,
    includeIfNull: false,
  )


  final ReconciliationSummary summary;



  @JsonKey(
    
    name: r'triggeredBy',
    required: true,
    includeIfNull: false,
  )


  final String triggeredBy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReconciliationReport &&
      other.date == date &&
      other.findings == findings &&
      other.runAt == runAt &&
      other.runId == runId &&
      other.summary == summary &&
      other.triggeredBy == triggeredBy;

    @override
    int get hashCode =>
        date.hashCode +
        findings.hashCode +
        runAt.hashCode +
        runId.hashCode +
        summary.hashCode +
        triggeredBy.hashCode;

  factory ReconciliationReport.fromJson(Map<String, dynamic> json) => _$ReconciliationReportFromJson(json);

  Map<String, dynamic> toJson() => _$ReconciliationReportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

