//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'finding_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FindingResponse {
  /// Returns a new [FindingResponse] instance.
  FindingResponse({

     this.detail,

     this.fineractAmount,

     this.fineractTransactionId,

    required  this.id,

     this.intentAmount,

     this.intentId,

     this.loanId,

     this.officeId,

     this.providerAmount,

     this.receiptNumber,

     this.reference,

     this.resolutionNote,

    required  this.resolved,

     this.resolvedAt,

     this.resolvedBy,

    required  this.type,
  });

  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final String? detail;



  @JsonKey(
    
    name: r'fineractAmount',
    required: false,
    includeIfNull: false,
  )


  final num? fineractAmount;



  @JsonKey(
    
    name: r'fineractTransactionId',
    required: false,
    includeIfNull: false,
  )


  final int? fineractTransactionId;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'intentAmount',
    required: false,
    includeIfNull: false,
  )


  final num? intentAmount;



  @JsonKey(
    
    name: r'intentId',
    required: false,
    includeIfNull: false,
  )


  final int? intentId;



  @JsonKey(
    
    name: r'loanId',
    required: false,
    includeIfNull: false,
  )


  final int? loanId;



  @JsonKey(
    
    name: r'officeId',
    required: false,
    includeIfNull: false,
  )


  final int? officeId;



  @JsonKey(
    
    name: r'providerAmount',
    required: false,
    includeIfNull: false,
  )


  final num? providerAmount;



  @JsonKey(
    
    name: r'receiptNumber',
    required: false,
    includeIfNull: false,
  )


  final String? receiptNumber;



  @JsonKey(
    
    name: r'reference',
    required: false,
    includeIfNull: false,
  )


  final String? reference;



  @JsonKey(
    
    name: r'resolutionNote',
    required: false,
    includeIfNull: false,
  )


  final String? resolutionNote;



  @JsonKey(
    
    name: r'resolved',
    required: true,
    includeIfNull: false,
  )


  final bool resolved;



  @JsonKey(
    
    name: r'resolvedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? resolvedAt;



  @JsonKey(
    
    name: r'resolvedBy',
    required: false,
    includeIfNull: false,
  )


  final String? resolvedBy;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  unknownEnumValue: FindingResponseTypeEnum.unknownDefaultOpenApi,
  )


  final FindingResponseTypeEnum type;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FindingResponse &&
      other.detail == detail &&
      other.fineractAmount == fineractAmount &&
      other.fineractTransactionId == fineractTransactionId &&
      other.id == id &&
      other.intentAmount == intentAmount &&
      other.intentId == intentId &&
      other.loanId == loanId &&
      other.officeId == officeId &&
      other.providerAmount == providerAmount &&
      other.receiptNumber == receiptNumber &&
      other.reference == reference &&
      other.resolutionNote == resolutionNote &&
      other.resolved == resolved &&
      other.resolvedAt == resolvedAt &&
      other.resolvedBy == resolvedBy &&
      other.type == type;

    @override
    int get hashCode =>
        detail.hashCode +
        fineractAmount.hashCode +
        fineractTransactionId.hashCode +
        id.hashCode +
        intentAmount.hashCode +
        intentId.hashCode +
        loanId.hashCode +
        officeId.hashCode +
        providerAmount.hashCode +
        receiptNumber.hashCode +
        reference.hashCode +
        resolutionNote.hashCode +
        resolved.hashCode +
        resolvedAt.hashCode +
        resolvedBy.hashCode +
        type.hashCode;

  factory FindingResponse.fromJson(Map<String, dynamic> json) => _$FindingResponseFromJson(json);

  Map<String, dynamic> toJson() => _$FindingResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FindingResponseTypeEnum {
@JsonValue(r'MATCHED')
MATCHED(r'MATCHED'),
@JsonValue(r'PAID_NOT_POSTED')
PAID_NOT_POSTED(r'PAID_NOT_POSTED'),
@JsonValue(r'POSTED_NOT_PAID')
POSTED_NOT_PAID(r'POSTED_NOT_PAID'),
@JsonValue(r'AMOUNT_MISMATCH')
AMOUNT_MISMATCH(r'AMOUNT_MISMATCH'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const FindingResponseTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


