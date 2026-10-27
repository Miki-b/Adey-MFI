//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loan_transaction_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoanTransactionResponse {
  /// Returns a new [LoanTransactionResponse] instance.
  LoanTransactionResponse({

    required  this.amount,

    required  this.date,

    required  this.feesPortion,

    required  this.id,

    required  this.interestPortion,

    required  this.outstandingBalanceAfter,

    required  this.overpaymentPortion,

     this.paymentType,

    required  this.penaltiesPortion,

    required  this.principalPortion,

    required  this.reversed,

    required  this.type,

     this.typeLabel,
  });

  @JsonKey(
    
    name: r'amount',
    required: true,
    includeIfNull: false,
  )


  final num amount;



  @JsonKey(
    
    name: r'date',
    required: true,
    includeIfNull: false,
  )


  final DateTime date;



  @JsonKey(
    
    name: r'feesPortion',
    required: true,
    includeIfNull: false,
  )


  final num feesPortion;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'interestPortion',
    required: true,
    includeIfNull: false,
  )


  final num interestPortion;



  @JsonKey(
    
    name: r'outstandingBalanceAfter',
    required: true,
    includeIfNull: false,
  )


  final num outstandingBalanceAfter;



  @JsonKey(
    
    name: r'overpaymentPortion',
    required: true,
    includeIfNull: false,
  )


  final num overpaymentPortion;



  @JsonKey(
    
    name: r'paymentType',
    required: false,
    includeIfNull: false,
  )


  final String? paymentType;



  @JsonKey(
    
    name: r'penaltiesPortion',
    required: true,
    includeIfNull: false,
  )


  final num penaltiesPortion;



  @JsonKey(
    
    name: r'principalPortion',
    required: true,
    includeIfNull: false,
  )


  final num principalPortion;



  @JsonKey(
    
    name: r'reversed',
    required: true,
    includeIfNull: false,
  )


  final bool reversed;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



  @JsonKey(
    
    name: r'typeLabel',
    required: false,
    includeIfNull: false,
  )


  final String? typeLabel;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoanTransactionResponse &&
      other.amount == amount &&
      other.date == date &&
      other.feesPortion == feesPortion &&
      other.id == id &&
      other.interestPortion == interestPortion &&
      other.outstandingBalanceAfter == outstandingBalanceAfter &&
      other.overpaymentPortion == overpaymentPortion &&
      other.paymentType == paymentType &&
      other.penaltiesPortion == penaltiesPortion &&
      other.principalPortion == principalPortion &&
      other.reversed == reversed &&
      other.type == type &&
      other.typeLabel == typeLabel;

    @override
    int get hashCode =>
        amount.hashCode +
        date.hashCode +
        feesPortion.hashCode +
        id.hashCode +
        interestPortion.hashCode +
        outstandingBalanceAfter.hashCode +
        overpaymentPortion.hashCode +
        paymentType.hashCode +
        penaltiesPortion.hashCode +
        principalPortion.hashCode +
        reversed.hashCode +
        type.hashCode +
        typeLabel.hashCode;

  factory LoanTransactionResponse.fromJson(Map<String, dynamic> json) => _$LoanTransactionResponseFromJson(json);

  Map<String, dynamic> toJson() => _$LoanTransactionResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

