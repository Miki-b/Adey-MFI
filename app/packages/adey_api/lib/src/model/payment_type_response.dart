//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'payment_type_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PaymentTypeResponse {
  /// Returns a new [PaymentTypeResponse] instance.
  PaymentTypeResponse({

     this.cash,

     this.description,

     this.id,

     this.name,
  });

  @JsonKey(
    
    name: r'cash',
    required: false,
    includeIfNull: false,
  )


  final bool? cash;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final int? id;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PaymentTypeResponse &&
      other.cash == cash &&
      other.description == description &&
      other.id == id &&
      other.name == name;

    @override
    int get hashCode =>
        cash.hashCode +
        description.hashCode +
        id.hashCode +
        name.hashCode;

  factory PaymentTypeResponse.fromJson(Map<String, dynamic> json) => _$PaymentTypeResponseFromJson(json);

  Map<String, dynamic> toJson() => _$PaymentTypeResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

