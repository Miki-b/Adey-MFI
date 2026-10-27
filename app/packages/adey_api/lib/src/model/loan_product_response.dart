//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'loan_product_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LoanProductResponse {
  /// Returns a new [LoanProductResponse] instance.
  LoanProductResponse({

     this.amortization,

     this.annualInterestRate,

     this.currency,

    required  this.id,

     this.interestMethod,

    required  this.name,

     this.principalDefault,

     this.principalMax,

     this.principalMin,

     this.repaymentEvery,

     this.repaymentFrequency,

     this.repaymentsDefault,

     this.repaymentsMax,

     this.repaymentsMin,

     this.shortName,
  });

  @JsonKey(
    
    name: r'amortization',
    required: false,
    includeIfNull: false,
  unknownEnumValue: LoanProductResponseAmortizationEnum.unknownDefaultOpenApi,
  )


  final LoanProductResponseAmortizationEnum? amortization;



  @JsonKey(
    
    name: r'annualInterestRate',
    required: false,
    includeIfNull: false,
  )


  final num? annualInterestRate;



  @JsonKey(
    
    name: r'currency',
    required: false,
    includeIfNull: false,
  )


  final String? currency;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'interestMethod',
    required: false,
    includeIfNull: false,
  unknownEnumValue: LoanProductResponseInterestMethodEnum.unknownDefaultOpenApi,
  )


  final LoanProductResponseInterestMethodEnum? interestMethod;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'principalDefault',
    required: false,
    includeIfNull: false,
  )


  final num? principalDefault;



  @JsonKey(
    
    name: r'principalMax',
    required: false,
    includeIfNull: false,
  )


  final num? principalMax;



  @JsonKey(
    
    name: r'principalMin',
    required: false,
    includeIfNull: false,
  )


  final num? principalMin;



  @JsonKey(
    
    name: r'repaymentEvery',
    required: false,
    includeIfNull: false,
  )


  final int? repaymentEvery;



  @JsonKey(
    
    name: r'repaymentFrequency',
    required: false,
    includeIfNull: false,
  unknownEnumValue: LoanProductResponseRepaymentFrequencyEnum.unknownDefaultOpenApi,
  )


  final LoanProductResponseRepaymentFrequencyEnum? repaymentFrequency;



  @JsonKey(
    
    name: r'repaymentsDefault',
    required: false,
    includeIfNull: false,
  )


  final int? repaymentsDefault;



  @JsonKey(
    
    name: r'repaymentsMax',
    required: false,
    includeIfNull: false,
  )


  final int? repaymentsMax;



  @JsonKey(
    
    name: r'repaymentsMin',
    required: false,
    includeIfNull: false,
  )


  final int? repaymentsMin;



  @JsonKey(
    
    name: r'shortName',
    required: false,
    includeIfNull: false,
  )


  final String? shortName;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LoanProductResponse &&
      other.amortization == amortization &&
      other.annualInterestRate == annualInterestRate &&
      other.currency == currency &&
      other.id == id &&
      other.interestMethod == interestMethod &&
      other.name == name &&
      other.principalDefault == principalDefault &&
      other.principalMax == principalMax &&
      other.principalMin == principalMin &&
      other.repaymentEvery == repaymentEvery &&
      other.repaymentFrequency == repaymentFrequency &&
      other.repaymentsDefault == repaymentsDefault &&
      other.repaymentsMax == repaymentsMax &&
      other.repaymentsMin == repaymentsMin &&
      other.shortName == shortName;

    @override
    int get hashCode =>
        amortization.hashCode +
        annualInterestRate.hashCode +
        currency.hashCode +
        id.hashCode +
        interestMethod.hashCode +
        name.hashCode +
        principalDefault.hashCode +
        principalMax.hashCode +
        principalMin.hashCode +
        repaymentEvery.hashCode +
        repaymentFrequency.hashCode +
        repaymentsDefault.hashCode +
        repaymentsMax.hashCode +
        repaymentsMin.hashCode +
        shortName.hashCode;

  factory LoanProductResponse.fromJson(Map<String, dynamic> json) => _$LoanProductResponseFromJson(json);

  Map<String, dynamic> toJson() => _$LoanProductResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LoanProductResponseAmortizationEnum {
@JsonValue(r'EQUAL_INSTALLMENTS')
EQUAL_INSTALLMENTS(r'EQUAL_INSTALLMENTS'),
@JsonValue(r'EQUAL_PRINCIPAL')
EQUAL_PRINCIPAL(r'EQUAL_PRINCIPAL'),
@JsonValue(r'UNKNOWN')
UNKNOWN(r'UNKNOWN'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const LoanProductResponseAmortizationEnum(this.value);

final String value;

@override
String toString() => value;
}


enum LoanProductResponseInterestMethodEnum {
@JsonValue(r'DECLINING_BALANCE')
DECLINING_BALANCE(r'DECLINING_BALANCE'),
@JsonValue(r'FLAT')
FLAT(r'FLAT'),
@JsonValue(r'UNKNOWN')
UNKNOWN(r'UNKNOWN'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const LoanProductResponseInterestMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


enum LoanProductResponseRepaymentFrequencyEnum {
@JsonValue(r'DAYS')
DAYS(r'DAYS'),
@JsonValue(r'WEEKS')
WEEKS(r'WEEKS'),
@JsonValue(r'MONTHS')
MONTHS(r'MONTHS'),
@JsonValue(r'YEARS')
YEARS(r'YEARS'),
@JsonValue(r'UNKNOWN')
UNKNOWN(r'UNKNOWN'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const LoanProductResponseRepaymentFrequencyEnum(this.value);

final String value;

@override
String toString() => value;
}


