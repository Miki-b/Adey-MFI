//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:adey_api/src/model/transition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mobile_payment_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MobilePaymentResponse {
  /// Returns a new [MobilePaymentResponse] instance.
  MobilePaymentResponse({

    required  this.amount,

    required  this.clientId,

    required  this.createdAt,

    required  this.currency,

    required  this.finalState,

     this.fineractTransactionId,

    required  this.history,

    required  this.id,

     this.lastError,

    required  this.loanId,

     this.method,

     this.nextAttemptAt,

     this.paidAt,

    required  this.phone,

    required  this.postingAttempts,

    required  this.provider,

     this.providerAmount,

     this.providerChargeId,

     this.providerTransactionId,

    required  this.reference,

    required  this.requestedBy,

    required  this.status,

    required  this.updatedAt,
  });

  @JsonKey(
    
    name: r'amount',
    required: true,
    includeIfNull: false,
  )


  final num amount;



  @JsonKey(
    
    name: r'clientId',
    required: true,
    includeIfNull: false,
  )


  final int clientId;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'currency',
    required: true,
    includeIfNull: false,
  )


  final String currency;



  @JsonKey(
    
    name: r'finalState',
    required: true,
    includeIfNull: false,
  )


  final bool finalState;



  @JsonKey(
    
    name: r'fineractTransactionId',
    required: false,
    includeIfNull: false,
  )


  final int? fineractTransactionId;



  @JsonKey(
    
    name: r'history',
    required: true,
    includeIfNull: false,
  )


  final List<Transition> history;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



  @JsonKey(
    
    name: r'loanId',
    required: true,
    includeIfNull: false,
  )


  final int loanId;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  unknownEnumValue: MobilePaymentResponseMethodEnum.unknownDefaultOpenApi,
  )


  final MobilePaymentResponseMethodEnum? method;



  @JsonKey(
    
    name: r'nextAttemptAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextAttemptAt;



  @JsonKey(
    
    name: r'paidAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? paidAt;



  @JsonKey(
    
    name: r'phone',
    required: true,
    includeIfNull: false,
  )


  final String phone;



  @JsonKey(
    
    name: r'postingAttempts',
    required: true,
    includeIfNull: false,
  )


  final int postingAttempts;



  @JsonKey(
    
    name: r'provider',
    required: true,
    includeIfNull: false,
  )


  final String provider;



  @JsonKey(
    
    name: r'providerAmount',
    required: false,
    includeIfNull: false,
  )


  final num? providerAmount;



  @JsonKey(
    
    name: r'providerChargeId',
    required: false,
    includeIfNull: false,
  )


  final String? providerChargeId;



  @JsonKey(
    
    name: r'providerTransactionId',
    required: false,
    includeIfNull: false,
  )


  final String? providerTransactionId;



  @JsonKey(
    
    name: r'reference',
    required: true,
    includeIfNull: false,
  )


  final String reference;



  @JsonKey(
    
    name: r'requestedBy',
    required: true,
    includeIfNull: false,
  )


  final String requestedBy;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  unknownEnumValue: MobilePaymentResponseStatusEnum.unknownDefaultOpenApi,
  )


  final MobilePaymentResponseStatusEnum status;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MobilePaymentResponse &&
      other.amount == amount &&
      other.clientId == clientId &&
      other.createdAt == createdAt &&
      other.currency == currency &&
      other.finalState == finalState &&
      other.fineractTransactionId == fineractTransactionId &&
      other.history == history &&
      other.id == id &&
      other.lastError == lastError &&
      other.loanId == loanId &&
      other.method == method &&
      other.nextAttemptAt == nextAttemptAt &&
      other.paidAt == paidAt &&
      other.phone == phone &&
      other.postingAttempts == postingAttempts &&
      other.provider == provider &&
      other.providerAmount == providerAmount &&
      other.providerChargeId == providerChargeId &&
      other.providerTransactionId == providerTransactionId &&
      other.reference == reference &&
      other.requestedBy == requestedBy &&
      other.status == status &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        amount.hashCode +
        clientId.hashCode +
        createdAt.hashCode +
        currency.hashCode +
        finalState.hashCode +
        fineractTransactionId.hashCode +
        history.hashCode +
        id.hashCode +
        lastError.hashCode +
        loanId.hashCode +
        method.hashCode +
        nextAttemptAt.hashCode +
        paidAt.hashCode +
        phone.hashCode +
        postingAttempts.hashCode +
        provider.hashCode +
        providerAmount.hashCode +
        providerChargeId.hashCode +
        providerTransactionId.hashCode +
        reference.hashCode +
        requestedBy.hashCode +
        status.hashCode +
        updatedAt.hashCode;

  factory MobilePaymentResponse.fromJson(Map<String, dynamic> json) => _$MobilePaymentResponseFromJson(json);

  Map<String, dynamic> toJson() => _$MobilePaymentResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MobilePaymentResponseMethodEnum {
@JsonValue(r'TELEBIRR')
TELEBIRR(r'TELEBIRR'),
@JsonValue(r'MPESA')
MPESA(r'MPESA'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const MobilePaymentResponseMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MobilePaymentResponseStatusEnum {
@JsonValue(r'PENDING')
PENDING(r'PENDING'),
@JsonValue(r'DECLINED')
DECLINED(r'DECLINED'),
@JsonValue(r'CHARGE_FAILED')
CHARGE_FAILED(r'CHARGE_FAILED'),
@JsonValue(r'MISMATCH')
MISMATCH(r'MISMATCH'),
@JsonValue(r'RECEIVED')
RECEIVED(r'RECEIVED'),
@JsonValue(r'RETRYING')
RETRYING(r'RETRYING'),
@JsonValue(r'POSTED')
POSTED(r'POSTED'),
@JsonValue(r'NEEDS_ATTENTION')
NEEDS_ATTENTION(r'NEEDS_ATTENTION'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const MobilePaymentResponseStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


