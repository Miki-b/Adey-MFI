//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'repayment_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RepaymentRequest {
  /// Returns a new [RepaymentRequest] instance.
  RepaymentRequest({

    required  this.amount,

     this.note,

     this.paymentTypeId,

     this.transactionDate,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'amount',
    required: true,
    includeIfNull: false,
  )


  final num amount;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



      /// How the client paid (see GET /api/payment-types).
          // minimum: 0
  @JsonKey(
    
    name: r'paymentTypeId',
    required: false,
    includeIfNull: false,
  )


  final int? paymentTypeId;



      /// ISO date. Defaults to today.
  @JsonKey(
    
    name: r'transactionDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? transactionDate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RepaymentRequest &&
      other.amount == amount &&
      other.note == note &&
      other.paymentTypeId == paymentTypeId &&
      other.transactionDate == transactionDate;

    @override
    int get hashCode =>
        amount.hashCode +
        note.hashCode +
        paymentTypeId.hashCode +
        transactionDate.hashCode;

  factory RepaymentRequest.fromJson(Map<String, dynamic> json) => _$RepaymentRequestFromJson(json);

  Map<String, dynamic> toJson() => _$RepaymentRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

