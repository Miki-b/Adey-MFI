//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reconciliation_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReconciliationSummary {
  /// Returns a new [ReconciliationSummary] instance.
  ReconciliationSummary({

    required  this.amountMismatch,

    required  this.fineractTotal,

    required  this.matched,

    required  this.matchedTotal,

    required  this.paidNotPosted,

    required  this.postedNotPaid,

    required  this.providerTotal,

    required  this.unresolved,
  });

  @JsonKey(
    
    name: r'amountMismatch',
    required: true,
    includeIfNull: false,
  )


  final int amountMismatch;



  @JsonKey(
    
    name: r'fineractTotal',
    required: true,
    includeIfNull: false,
  )


  final num fineractTotal;



  @JsonKey(
    
    name: r'matched',
    required: true,
    includeIfNull: false,
  )


  final int matched;



  @JsonKey(
    
    name: r'matchedTotal',
    required: true,
    includeIfNull: false,
  )


  final num matchedTotal;



  @JsonKey(
    
    name: r'paidNotPosted',
    required: true,
    includeIfNull: false,
  )


  final int paidNotPosted;



  @JsonKey(
    
    name: r'postedNotPaid',
    required: true,
    includeIfNull: false,
  )


  final int postedNotPaid;



  @JsonKey(
    
    name: r'providerTotal',
    required: true,
    includeIfNull: false,
  )


  final num providerTotal;



  @JsonKey(
    
    name: r'unresolved',
    required: true,
    includeIfNull: false,
  )


  final int unresolved;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReconciliationSummary &&
      other.amountMismatch == amountMismatch &&
      other.fineractTotal == fineractTotal &&
      other.matched == matched &&
      other.matchedTotal == matchedTotal &&
      other.paidNotPosted == paidNotPosted &&
      other.postedNotPaid == postedNotPaid &&
      other.providerTotal == providerTotal &&
      other.unresolved == unresolved;

    @override
    int get hashCode =>
        amountMismatch.hashCode +
        fineractTotal.hashCode +
        matched.hashCode +
        matchedTotal.hashCode +
        paidNotPosted.hashCode +
        postedNotPaid.hashCode +
        providerTotal.hashCode +
        unresolved.hashCode;

  factory ReconciliationSummary.fromJson(Map<String, dynamic> json) => _$ReconciliationSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$ReconciliationSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

