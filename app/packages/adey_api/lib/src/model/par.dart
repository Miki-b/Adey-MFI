//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'par.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Par {
  /// Returns a new [Par] instance.
  Par({

     this.par1,

     this.par1Principal,

     this.par30,

     this.par30Principal,
  });

  @JsonKey(
    
    name: r'par1',
    required: false,
    includeIfNull: false,
  )


  final num? par1;



  @JsonKey(
    
    name: r'par1Principal',
    required: false,
    includeIfNull: false,
  )


  final num? par1Principal;



  @JsonKey(
    
    name: r'par30',
    required: false,
    includeIfNull: false,
  )


  final num? par30;



  @JsonKey(
    
    name: r'par30Principal',
    required: false,
    includeIfNull: false,
  )


  final num? par30Principal;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Par &&
      other.par1 == par1 &&
      other.par1Principal == par1Principal &&
      other.par30 == par30 &&
      other.par30Principal == par30Principal;

    @override
    int get hashCode =>
        par1.hashCode +
        par1Principal.hashCode +
        par30.hashCode +
        par30Principal.hashCode;

  factory Par.fromJson(Map<String, dynamic> json) => _$ParFromJson(json);

  Map<String, dynamic> toJson() => _$ParToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

