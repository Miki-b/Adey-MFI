//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'installment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Installment {
  /// Returns a new [Installment] instance.
  Installment({

    required  this.balanceAfter,

    required  this.complete,

    required  this.dueDate,

    required  this.fees,

    required  this.interest,

    required  this.number,

    required  this.outstanding,

    required  this.paid,

    required  this.penalties,

    required  this.principal,

    required  this.total,
  });

  @JsonKey(
    
    name: r'balanceAfter',
    required: true,
    includeIfNull: false,
  )


  final num balanceAfter;



  @JsonKey(
    
    name: r'complete',
    required: true,
    includeIfNull: false,
  )


  final bool complete;



  @JsonKey(
    
    name: r'dueDate',
    required: true,
    includeIfNull: false,
  )


  final DateTime dueDate;



  @JsonKey(
    
    name: r'fees',
    required: true,
    includeIfNull: false,
  )


  final num fees;



  @JsonKey(
    
    name: r'interest',
    required: true,
    includeIfNull: false,
  )


  final num interest;



  @JsonKey(
    
    name: r'number',
    required: true,
    includeIfNull: false,
  )


  final int number;



  @JsonKey(
    
    name: r'outstanding',
    required: true,
    includeIfNull: false,
  )


  final num outstanding;



  @JsonKey(
    
    name: r'paid',
    required: true,
    includeIfNull: false,
  )


  final num paid;



  @JsonKey(
    
    name: r'penalties',
    required: true,
    includeIfNull: false,
  )


  final num penalties;



  @JsonKey(
    
    name: r'principal',
    required: true,
    includeIfNull: false,
  )


  final num principal;



  @JsonKey(
    
    name: r'total',
    required: true,
    includeIfNull: false,
  )


  final num total;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Installment &&
      other.balanceAfter == balanceAfter &&
      other.complete == complete &&
      other.dueDate == dueDate &&
      other.fees == fees &&
      other.interest == interest &&
      other.number == number &&
      other.outstanding == outstanding &&
      other.paid == paid &&
      other.penalties == penalties &&
      other.principal == principal &&
      other.total == total;

    @override
    int get hashCode =>
        balanceAfter.hashCode +
        complete.hashCode +
        dueDate.hashCode +
        fees.hashCode +
        interest.hashCode +
        number.hashCode +
        outstanding.hashCode +
        paid.hashCode +
        penalties.hashCode +
        principal.hashCode +
        total.hashCode;

  factory Installment.fromJson(Map<String, dynamic> json) => _$InstallmentFromJson(json);

  Map<String, dynamic> toJson() => _$InstallmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

