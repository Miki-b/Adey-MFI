//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loan_list_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoanListItem {
  /// Returns a new [LoanListItem] instance.
  LoanListItem({

    required  this.accountNo,

    required  this.clientId,

     this.clientMobileNo,

    required  this.clientName,

    required  this.daysOverdue,

    required  this.inArrears,

    required  this.loanId,

     this.loanOfficerName,

     this.nextDueAmount,

     this.nextDueDate,

    required  this.principal,

    required  this.principalOutstanding,

    required  this.productName,

    required  this.status,

     this.statusLabel,

     this.submittedOn,

    required  this.totalOverdue,
  });

  @JsonKey(
    
    name: r'accountNo',
    required: true,
    includeIfNull: false,
  )


  final String accountNo;



  @JsonKey(
    
    name: r'clientId',
    required: true,
    includeIfNull: false,
  )


  final int clientId;



  @JsonKey(
    
    name: r'clientMobileNo',
    required: false,
    includeIfNull: false,
  )


  final String? clientMobileNo;



  @JsonKey(
    
    name: r'clientName',
    required: true,
    includeIfNull: false,
  )


  final String clientName;



  @JsonKey(
    
    name: r'daysOverdue',
    required: true,
    includeIfNull: false,
  )


  final int daysOverdue;



  @JsonKey(
    
    name: r'inArrears',
    required: true,
    includeIfNull: false,
  )


  final bool inArrears;



  @JsonKey(
    
    name: r'loanId',
    required: true,
    includeIfNull: false,
  )


  final int loanId;



  @JsonKey(
    
    name: r'loanOfficerName',
    required: false,
    includeIfNull: false,
  )


  final String? loanOfficerName;



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
    required: true,
    includeIfNull: false,
  )


  final num principal;



  @JsonKey(
    
    name: r'principalOutstanding',
    required: true,
    includeIfNull: false,
  )


  final num principalOutstanding;



  @JsonKey(
    
    name: r'productName',
    required: true,
    includeIfNull: false,
  )


  final String productName;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  unknownEnumValue: LoanListItemStatusEnum.unknownDefaultOpenApi,
  )


  final LoanListItemStatusEnum status;



  @JsonKey(
    
    name: r'statusLabel',
    required: false,
    includeIfNull: false,
  )


  final String? statusLabel;



  @JsonKey(
    
    name: r'submittedOn',
    required: false,
    includeIfNull: false,
  )


  final DateTime? submittedOn;



  @JsonKey(
    
    name: r'totalOverdue',
    required: true,
    includeIfNull: false,
  )


  final num totalOverdue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoanListItem &&
      other.accountNo == accountNo &&
      other.clientId == clientId &&
      other.clientMobileNo == clientMobileNo &&
      other.clientName == clientName &&
      other.daysOverdue == daysOverdue &&
      other.inArrears == inArrears &&
      other.loanId == loanId &&
      other.loanOfficerName == loanOfficerName &&
      other.nextDueAmount == nextDueAmount &&
      other.nextDueDate == nextDueDate &&
      other.principal == principal &&
      other.principalOutstanding == principalOutstanding &&
      other.productName == productName &&
      other.status == status &&
      other.statusLabel == statusLabel &&
      other.submittedOn == submittedOn &&
      other.totalOverdue == totalOverdue;

    @override
    int get hashCode =>
        accountNo.hashCode +
        clientId.hashCode +
        clientMobileNo.hashCode +
        clientName.hashCode +
        daysOverdue.hashCode +
        inArrears.hashCode +
        loanId.hashCode +
        loanOfficerName.hashCode +
        nextDueAmount.hashCode +
        nextDueDate.hashCode +
        principal.hashCode +
        principalOutstanding.hashCode +
        productName.hashCode +
        status.hashCode +
        statusLabel.hashCode +
        submittedOn.hashCode +
        totalOverdue.hashCode;

  factory LoanListItem.fromJson(Map<String, dynamic> json) => _$LoanListItemFromJson(json);

  Map<String, dynamic> toJson() => _$LoanListItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LoanListItemStatusEnum {
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

const LoanListItemStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


