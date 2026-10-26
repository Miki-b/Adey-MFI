//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'amounts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Amounts {
  /// Returns a new [Amounts] instance.
  Amounts({

     this.currency,

     this.principalOutstanding,

     this.totalOverdue,
  });

  @JsonKey(
    
    name: r'currency',
    required: false,
    includeIfNull: false,
  )


  final String? currency;



  @JsonKey(
    
    name: r'principalOutstanding',
    required: false,
    includeIfNull: false,
  )


  final num? principalOutstanding;



  @JsonKey(
    
    name: r'totalOverdue',
    required: false,
    includeIfNull: false,
  )


  final num? totalOverdue;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Amounts &&
      other.currency == currency &&
      other.principalOutstanding == principalOutstanding &&
      other.totalOverdue == totalOverdue;

    @override
    int get hashCode =>
        currency.hashCode +
        principalOutstanding.hashCode +
        totalOverdue.hashCode;

  factory Amounts.fromJson(Map<String, dynamic> json) => _$AmountsFromJson(json);

  Map<String, dynamic> toJson() => _$AmountsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

