//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Summary {
  /// Returns a new [Summary] instance.
  Summary({

     this.amountMismatch,

     this.fineractTotal,

     this.matched,

     this.matchedTotal,

     this.paidNotPosted,

     this.postedNotPaid,

     this.providerTotal,

     this.unresolved,
  });

  @JsonKey(
    
    name: r'amountMismatch',
    required: false,
    includeIfNull: false,
  )


  final int? amountMismatch;



  @JsonKey(
    
    name: r'fineractTotal',
    required: false,
    includeIfNull: false,
  )


  final num? fineractTotal;



  @JsonKey(
    
    name: r'matched',
    required: false,
    includeIfNull: false,
  )


  final int? matched;



  @JsonKey(
    
    name: r'matchedTotal',
    required: false,
    includeIfNull: false,
  )


  final num? matchedTotal;



  @JsonKey(
    
    name: r'paidNotPosted',
    required: false,
    includeIfNull: false,
  )


  final int? paidNotPosted;



  @JsonKey(
    
    name: r'postedNotPaid',
    required: false,
    includeIfNull: false,
  )


  final int? postedNotPaid;



  @JsonKey(
    
    name: r'providerTotal',
    required: false,
    includeIfNull: false,
  )


  final num? providerTotal;



  @JsonKey(
    
    name: r'unresolved',
    required: false,
    includeIfNull: false,
  )


  final int? unresolved;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Summary &&
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

  factory Summary.fromJson(Map<String, dynamic> json) => _$SummaryFromJson(json);

  Map<String, dynamic> toJson() => _$SummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

