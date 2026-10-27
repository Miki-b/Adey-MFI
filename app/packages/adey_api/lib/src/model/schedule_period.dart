//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'schedule_period.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SchedulePeriod {
  /// Returns a new [SchedulePeriod] instance.
  SchedulePeriod({

    required  this.balanceAfter,

    required  this.dueDate,

    required  this.interest,

    required  this.number,

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
    
    name: r'dueDate',
    required: true,
    includeIfNull: false,
  )


  final DateTime dueDate;



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
    bool operator ==(Object other) => identical(this, other) || other is SchedulePeriod &&
      other.balanceAfter == balanceAfter &&
      other.dueDate == dueDate &&
      other.interest == interest &&
      other.number == number &&
      other.principal == principal &&
      other.total == total;

    @override
    int get hashCode =>
        balanceAfter.hashCode +
        dueDate.hashCode +
        interest.hashCode +
        number.hashCode +
        principal.hashCode +
        total.hashCode;

  factory SchedulePeriod.fromJson(Map<String, dynamic> json) => _$SchedulePeriodFromJson(json);

  Map<String, dynamic> toJson() => _$SchedulePeriodToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

