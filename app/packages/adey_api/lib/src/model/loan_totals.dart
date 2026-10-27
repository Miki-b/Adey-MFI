//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loan_totals.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoanTotals {
  /// Returns a new [LoanTotals] instance.
  LoanTotals({

    required  this.inArrears,

    required  this.interestCharged,

    required  this.interestOutstanding,

    required  this.interestPaid,

     this.overdueSince,

    required  this.principalDisbursed,

    required  this.principalOutstanding,

    required  this.principalPaid,

    required  this.totalOutstanding,

    required  this.totalOverdue,
  });

  @JsonKey(
    
    name: r'inArrears',
    required: true,
    includeIfNull: false,
  )


  final bool inArrears;



  @JsonKey(
    
    name: r'interestCharged',
    required: true,
    includeIfNull: false,
  )


  final num interestCharged;



  @JsonKey(
    
    name: r'interestOutstanding',
    required: true,
    includeIfNull: false,
  )


  final num interestOutstanding;



  @JsonKey(
    
    name: r'interestPaid',
    required: true,
    includeIfNull: false,
  )


  final num interestPaid;



  @JsonKey(
    
    name: r'overdueSince',
    required: false,
    includeIfNull: false,
  )


  final DateTime? overdueSince;



  @JsonKey(
    
    name: r'principalDisbursed',
    required: true,
    includeIfNull: false,
  )


  final num principalDisbursed;



  @JsonKey(
    
    name: r'principalOutstanding',
    required: true,
    includeIfNull: false,
  )


  final num principalOutstanding;



  @JsonKey(
    
    name: r'principalPaid',
    required: true,
    includeIfNull: false,
  )


  final num principalPaid;



  @JsonKey(
    
    name: r'totalOutstanding',
    required: true,
    includeIfNull: false,
  )


  final num totalOutstanding;



  @JsonKey(
    
    name: r'totalOverdue',
    required: true,
    includeIfNull: false,
  )


  final num totalOverdue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoanTotals &&
      other.inArrears == inArrears &&
      other.interestCharged == interestCharged &&
      other.interestOutstanding == interestOutstanding &&
      other.interestPaid == interestPaid &&
      other.overdueSince == overdueSince &&
      other.principalDisbursed == principalDisbursed &&
      other.principalOutstanding == principalOutstanding &&
      other.principalPaid == principalPaid &&
      other.totalOutstanding == totalOutstanding &&
      other.totalOverdue == totalOverdue;

    @override
    int get hashCode =>
        inArrears.hashCode +
        interestCharged.hashCode +
        interestOutstanding.hashCode +
        interestPaid.hashCode +
        overdueSince.hashCode +
        principalDisbursed.hashCode +
        principalOutstanding.hashCode +
        principalPaid.hashCode +
        totalOutstanding.hashCode +
        totalOverdue.hashCode;

  factory LoanTotals.fromJson(Map<String, dynamic> json) => _$LoanTotalsFromJson(json);

  Map<String, dynamic> toJson() => _$LoanTotalsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

