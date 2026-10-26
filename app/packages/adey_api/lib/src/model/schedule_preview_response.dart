//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:adey_api/src/model/schedule_period.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'schedule_preview_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SchedulePreviewResponse {
  /// Returns a new [SchedulePreviewResponse] instance.
  SchedulePreviewResponse({

     this.currency,

     this.expectedDisbursementDate,

     this.installmentAmount,

     this.lastInstallmentAmount,

     this.numberOfRepayments,

     this.periods,

     this.principal,

     this.totalInterest,

     this.totalRepayable,
  });

  @JsonKey(
    
    name: r'currency',
    required: false,
    includeIfNull: false,
  )


  final String? currency;



  @JsonKey(
    
    name: r'expectedDisbursementDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expectedDisbursementDate;



  @JsonKey(
    
    name: r'installmentAmount',
    required: false,
    includeIfNull: false,
  )


  final num? installmentAmount;



  @JsonKey(
    
    name: r'lastInstallmentAmount',
    required: false,
    includeIfNull: false,
  )


  final num? lastInstallmentAmount;



  @JsonKey(
    
    name: r'numberOfRepayments',
    required: false,
    includeIfNull: false,
  )


  final int? numberOfRepayments;



  @JsonKey(
    
    name: r'periods',
    required: false,
    includeIfNull: false,
  )


  final List<SchedulePeriod>? periods;



  @JsonKey(
    
    name: r'principal',
    required: false,
    includeIfNull: false,
  )


  final num? principal;



  @JsonKey(
    
    name: r'totalInterest',
    required: false,
    includeIfNull: false,
  )


  final num? totalInterest;



  @JsonKey(
    
    name: r'totalRepayable',
    required: false,
    includeIfNull: false,
  )


  final num? totalRepayable;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SchedulePreviewResponse &&
      other.currency == currency &&
      other.expectedDisbursementDate == expectedDisbursementDate &&
      other.installmentAmount == installmentAmount &&
      other.lastInstallmentAmount == lastInstallmentAmount &&
      other.numberOfRepayments == numberOfRepayments &&
      other.periods == periods &&
      other.principal == principal &&
      other.totalInterest == totalInterest &&
      other.totalRepayable == totalRepayable;

    @override
    int get hashCode =>
        currency.hashCode +
        expectedDisbursementDate.hashCode +
        installmentAmount.hashCode +
        lastInstallmentAmount.hashCode +
        numberOfRepayments.hashCode +
        periods.hashCode +
        principal.hashCode +
        totalInterest.hashCode +
        totalRepayable.hashCode;

  factory SchedulePreviewResponse.fromJson(Map<String, dynamic> json) => _$SchedulePreviewResponseFromJson(json);

  Map<String, dynamic> toJson() => _$SchedulePreviewResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

