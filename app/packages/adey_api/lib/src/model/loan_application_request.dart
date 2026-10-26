//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loan_application_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoanApplicationRequest {
  /// Returns a new [LoanApplicationRequest] instance.
  LoanApplicationRequest({

    required  this.clientId,

     this.expectedDisbursementDate,

    required  this.numberOfRepayments,

    required  this.principal,

    required  this.productId,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'clientId',
    required: true,
    includeIfNull: false,
  )


  final int clientId;



      /// ISO date. Defaults to today.
  @JsonKey(
    
    name: r'expectedDisbursementDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expectedDisbursementDate;



          // minimum: 1
  @JsonKey(
    
    name: r'numberOfRepayments',
    required: true,
    includeIfNull: false,
  )


  final int numberOfRepayments;



          // minimum: 0
  @JsonKey(
    
    name: r'principal',
    required: true,
    includeIfNull: false,
  )


  final num principal;



          // minimum: 0
  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final int productId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoanApplicationRequest &&
      other.clientId == clientId &&
      other.expectedDisbursementDate == expectedDisbursementDate &&
      other.numberOfRepayments == numberOfRepayments &&
      other.principal == principal &&
      other.productId == productId;

    @override
    int get hashCode =>
        clientId.hashCode +
        expectedDisbursementDate.hashCode +
        numberOfRepayments.hashCode +
        principal.hashCode +
        productId.hashCode;

  factory LoanApplicationRequest.fromJson(Map<String, dynamic> json) => _$LoanApplicationRequestFromJson(json);

  Map<String, dynamic> toJson() => _$LoanApplicationRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

