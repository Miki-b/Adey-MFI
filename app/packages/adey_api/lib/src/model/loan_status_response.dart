//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loan_status_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoanStatusResponse {
  /// Returns a new [LoanStatusResponse] instance.
  LoanStatusResponse({

     this.accountNo,

    required  this.loanId,

    required  this.status,

     this.statusLabel,
  });

  @JsonKey(
    
    name: r'accountNo',
    required: false,
    includeIfNull: false,
  )


  final String? accountNo;



  @JsonKey(
    
    name: r'loanId',
    required: true,
    includeIfNull: false,
  )


  final int loanId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  unknownEnumValue: LoanStatusResponseStatusEnum.unknownDefaultOpenApi,
  )


  final LoanStatusResponseStatusEnum status;



  @JsonKey(
    
    name: r'statusLabel',
    required: false,
    includeIfNull: false,
  )


  final String? statusLabel;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoanStatusResponse &&
      other.accountNo == accountNo &&
      other.loanId == loanId &&
      other.status == status &&
      other.statusLabel == statusLabel;

    @override
    int get hashCode =>
        accountNo.hashCode +
        loanId.hashCode +
        status.hashCode +
        statusLabel.hashCode;

  factory LoanStatusResponse.fromJson(Map<String, dynamic> json) => _$LoanStatusResponseFromJson(json);

  Map<String, dynamic> toJson() => _$LoanStatusResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LoanStatusResponseStatusEnum {
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

const LoanStatusResponseStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


