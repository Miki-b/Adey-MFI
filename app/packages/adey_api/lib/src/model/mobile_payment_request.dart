//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mobile_payment_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MobilePaymentRequest {
  /// Returns a new [MobilePaymentRequest] instance.
  MobilePaymentRequest({

    required  this.amount,

     this.method,

    required  this.phone,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'amount',
    required: true,
    includeIfNull: false,
  )


  final num amount;



      /// Wallet: TELEBIRR (09... numbers) or MPESA (07... numbers). Default TELEBIRR.
  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  unknownEnumValue: MobilePaymentRequestMethodEnum.unknownDefaultOpenApi,
  )


  final MobilePaymentRequestMethodEnum? method;



      /// Wallet phone number, 10 digits starting 09 or 07
  @JsonKey(
    
    name: r'phone',
    required: true,
    includeIfNull: false,
  )


  final String phone;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MobilePaymentRequest &&
      other.amount == amount &&
      other.method == method &&
      other.phone == phone;

    @override
    int get hashCode =>
        amount.hashCode +
        method.hashCode +
        phone.hashCode;

  factory MobilePaymentRequest.fromJson(Map<String, dynamic> json) => _$MobilePaymentRequestFromJson(json);

  Map<String, dynamic> toJson() => _$MobilePaymentRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Wallet: TELEBIRR (09... numbers) or MPESA (07... numbers). Default TELEBIRR.
enum MobilePaymentRequestMethodEnum {
@JsonValue(r'TELEBIRR')
TELEBIRR(r'TELEBIRR'),
@JsonValue(r'MPESA')
MPESA(r'MPESA'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const MobilePaymentRequestMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


