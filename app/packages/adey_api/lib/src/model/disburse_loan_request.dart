//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'disburse_loan_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DisburseLoanRequest {
  /// Returns a new [DisburseLoanRequest] instance.
  DisburseLoanRequest({

     this.actualDisbursementDate,

     this.note,

     this.paymentTypeId,
  });

      /// ISO date. Defaults to today.
  @JsonKey(
    
    name: r'actualDisbursementDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? actualDisbursementDate;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



      /// How the money was handed over (see GET /api/payment-types).
          // minimum: 0
  @JsonKey(
    
    name: r'paymentTypeId',
    required: false,
    includeIfNull: false,
  )


  final int? paymentTypeId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DisburseLoanRequest &&
      other.actualDisbursementDate == actualDisbursementDate &&
      other.note == note &&
      other.paymentTypeId == paymentTypeId;

    @override
    int get hashCode =>
        actualDisbursementDate.hashCode +
        note.hashCode +
        paymentTypeId.hashCode;

  factory DisburseLoanRequest.fromJson(Map<String, dynamic> json) => _$DisburseLoanRequestFromJson(json);

  Map<String, dynamic> toJson() => _$DisburseLoanRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

