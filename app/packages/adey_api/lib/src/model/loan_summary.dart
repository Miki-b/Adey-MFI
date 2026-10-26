//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loan_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoanSummary {
  /// Returns a new [LoanSummary] instance.
  LoanSummary({

     this.accountNo,

     this.annualInterestRate,

     this.inArrears,

     this.installmentsPaid,

     this.installmentsTotal,

     this.loanId,

     this.nextDueAmount,

     this.nextDueDate,

     this.principal,

     this.principalOutstanding,

     this.productName,

     this.status,

     this.statusLabel,

     this.totalOutstanding,
  });

  @JsonKey(
    
    name: r'accountNo',
    required: false,
    includeIfNull: false,
  )


  final String? accountNo;



  @JsonKey(
    
    name: r'annualInterestRate',
    required: false,
    includeIfNull: false,
  )


  final num? annualInterestRate;



  @JsonKey(
    
    name: r'inArrears',
    required: false,
    includeIfNull: false,
  )


  final bool? inArrears;



  @JsonKey(
    
    name: r'installmentsPaid',
    required: false,
    includeIfNull: false,
  )


  final int? installmentsPaid;



  @JsonKey(
    
    name: r'installmentsTotal',
    required: false,
    includeIfNull: false,
  )


  final int? installmentsTotal;



  @JsonKey(
    
    name: r'loanId',
    required: false,
    includeIfNull: false,
  )


  final int? loanId;



  @JsonKey(
    
    name: r'nextDueAmount',
    required: false,
    includeIfNull: false,
  )


  final num? nextDueAmount;



  @JsonKey(
    
    name: r'nextDueDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextDueDate;



  @JsonKey(
    
    name: r'principal',
    required: false,
    includeIfNull: false,
  )


  final num? principal;



  @JsonKey(
    
    name: r'principalOutstanding',
    required: false,
    includeIfNull: false,
  )


  final num? principalOutstanding;



  @JsonKey(
    
    name: r'productName',
    required: false,
    includeIfNull: false,
  )


  final String? productName;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  unknownEnumValue: LoanSummaryStatusEnum.unknownDefaultOpenApi,
  )


  final LoanSummaryStatusEnum? status;



  @JsonKey(
    
    name: r'statusLabel',
    required: false,
    includeIfNull: false,
  )


  final String? statusLabel;



  @JsonKey(
    
    name: r'totalOutstanding',
    required: false,
    includeIfNull: false,
  )


  final num? totalOutstanding;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoanSummary &&
      other.accountNo == accountNo &&
      other.annualInterestRate == annualInterestRate &&
      other.inArrears == inArrears &&
      other.installmentsPaid == installmentsPaid &&
      other.installmentsTotal == installmentsTotal &&
      other.loanId == loanId &&
      other.nextDueAmount == nextDueAmount &&
      other.nextDueDate == nextDueDate &&
      other.principal == principal &&
      other.principalOutstanding == principalOutstanding &&
      other.productName == productName &&
      other.status == status &&
      other.statusLabel == statusLabel &&
      other.totalOutstanding == totalOutstanding;

    @override
    int get hashCode =>
        accountNo.hashCode +
        annualInterestRate.hashCode +
        inArrears.hashCode +
        installmentsPaid.hashCode +
        installmentsTotal.hashCode +
        loanId.hashCode +
        nextDueAmount.hashCode +
        nextDueDate.hashCode +
        principal.hashCode +
        principalOutstanding.hashCode +
        productName.hashCode +
        status.hashCode +
        statusLabel.hashCode +
        totalOutstanding.hashCode;

  factory LoanSummary.fromJson(Map<String, dynamic> json) => _$LoanSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$LoanSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LoanSummaryStatusEnum {
@JsonValue(r'SUBMITTED_AND_PENDING_APPROVAL')
SUBMITTED_AND_PENDING_APPROVAL(r'SUBMITTED_AND_PENDING_APPROVAL'),
@JsonValue(r'APPROVED')
APPROVED(r'APPROVED'),
@JsonValue(r'ACTIVE')
ACTIVE(r'ACTIVE'),
@JsonValue(r'WITHDRAWN_BY_CLIENT')
WITHDRAWN_BY_CLIENT(r'WITHDRAWN_BY_CLIENT'),
@JsonValue(r'REJECTED')
REJECTED(r'REJECTED'),
@JsonValue(r'CLOSED')
CLOSED(r'CLOSED'),
@JsonValue(r'WRITTEN_OFF')
WRITTEN_OFF(r'WRITTEN_OFF'),
@JsonValue(r'RESCHEDULED')
RESCHEDULED(r'RESCHEDULED'),
@JsonValue(r'OVERPAID')
OVERPAID(r'OVERPAID'),
@JsonValue(r'UNKNOWN')
UNKNOWN(r'UNKNOWN'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const LoanSummaryStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


