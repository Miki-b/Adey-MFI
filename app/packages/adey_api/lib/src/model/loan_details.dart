//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:adey_api/src/model/loan_transaction_response.dart';
import 'package:adey_api/src/model/summary.dart';
import 'package:adey_api/src/model/dates.dart';
import 'package:adey_api/src/model/installment.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loan_details.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoanDetails {
  /// Returns a new [LoanDetails] instance.
  LoanDetails({

     this.accountNo,

     this.annualInterestRate,

     this.approvedPrincipal,

     this.clientId,

     this.clientName,

     this.currency,

     this.dates,

     this.loanId,

     this.loanOfficerId,

     this.loanOfficerName,

     this.numberOfRepayments,

     this.principal,

     this.productId,

     this.productName,

     this.schedule,

     this.status,

     this.statusLabel,

     this.summary,

     this.transactions,
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
    
    name: r'approvedPrincipal',
    required: false,
    includeIfNull: false,
  )


  final num? approvedPrincipal;



  @JsonKey(
    
    name: r'clientId',
    required: false,
    includeIfNull: false,
  )


  final int? clientId;



  @JsonKey(
    
    name: r'clientName',
    required: false,
    includeIfNull: false,
  )


  final String? clientName;



  @JsonKey(
    
    name: r'currency',
    required: false,
    includeIfNull: false,
  )


  final String? currency;



  @JsonKey(
    
    name: r'dates',
    required: false,
    includeIfNull: false,
  )


  final Dates? dates;



  @JsonKey(
    
    name: r'loanId',
    required: false,
    includeIfNull: false,
  )


  final int? loanId;



  @JsonKey(
    
    name: r'loanOfficerId',
    required: false,
    includeIfNull: false,
  )


  final int? loanOfficerId;



  @JsonKey(
    
    name: r'loanOfficerName',
    required: false,
    includeIfNull: false,
  )


  final String? loanOfficerName;



  @JsonKey(
    
    name: r'numberOfRepayments',
    required: false,
    includeIfNull: false,
  )


  final int? numberOfRepayments;



  @JsonKey(
    
    name: r'principal',
    required: false,
    includeIfNull: false,
  )


  final num? principal;



  @JsonKey(
    
    name: r'productId',
    required: false,
    includeIfNull: false,
  )


  final int? productId;



  @JsonKey(
    
    name: r'productName',
    required: false,
    includeIfNull: false,
  )


  final String? productName;



  @JsonKey(
    
    name: r'schedule',
    required: false,
    includeIfNull: false,
  )


  final List<Installment>? schedule;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  unknownEnumValue: LoanDetailsStatusEnum.unknownDefaultOpenApi,
  )


  final LoanDetailsStatusEnum? status;



  @JsonKey(
    
    name: r'statusLabel',
    required: false,
    includeIfNull: false,
  )


  final String? statusLabel;



  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final Summary? summary;



  @JsonKey(
    
    name: r'transactions',
    required: false,
    includeIfNull: false,
  )


  final List<LoanTransactionResponse>? transactions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoanDetails &&
      other.accountNo == accountNo &&
      other.annualInterestRate == annualInterestRate &&
      other.approvedPrincipal == approvedPrincipal &&
      other.clientId == clientId &&
      other.clientName == clientName &&
      other.currency == currency &&
      other.dates == dates &&
      other.loanId == loanId &&
      other.loanOfficerId == loanOfficerId &&
      other.loanOfficerName == loanOfficerName &&
      other.numberOfRepayments == numberOfRepayments &&
      other.principal == principal &&
      other.productId == productId &&
      other.productName == productName &&
      other.schedule == schedule &&
      other.status == status &&
      other.statusLabel == statusLabel &&
      other.summary == summary &&
      other.transactions == transactions;

    @override
    int get hashCode =>
        accountNo.hashCode +
        annualInterestRate.hashCode +
        approvedPrincipal.hashCode +
        clientId.hashCode +
        clientName.hashCode +
        currency.hashCode +
        dates.hashCode +
        loanId.hashCode +
        loanOfficerId.hashCode +
        loanOfficerName.hashCode +
        numberOfRepayments.hashCode +
        principal.hashCode +
        productId.hashCode +
        productName.hashCode +
        schedule.hashCode +
        status.hashCode +
        statusLabel.hashCode +
        summary.hashCode +
        transactions.hashCode;

  factory LoanDetails.fromJson(Map<String, dynamic> json) => _$LoanDetailsFromJson(json);

  Map<String, dynamic> toJson() => _$LoanDetailsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LoanDetailsStatusEnum {
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

const LoanDetailsStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


