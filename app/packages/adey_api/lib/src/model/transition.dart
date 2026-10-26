//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'transition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Transition {
  /// Returns a new [Transition] instance.
  Transition({

     this.at,

     this.from,

     this.note,

     this.to,
  });

  @JsonKey(
    
    name: r'at',
    required: false,
    includeIfNull: false,
  )


  final DateTime? at;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  unknownEnumValue: TransitionFromEnum.unknownDefaultOpenApi,
  )


  final TransitionFromEnum? from;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  unknownEnumValue: TransitionToEnum.unknownDefaultOpenApi,
  )


  final TransitionToEnum? to;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Transition &&
      other.at == at &&
      other.from == from &&
      other.note == note &&
      other.to == to;

    @override
    int get hashCode =>
        at.hashCode +
        from.hashCode +
        note.hashCode +
        to.hashCode;

  factory Transition.fromJson(Map<String, dynamic> json) => _$TransitionFromJson(json);

  Map<String, dynamic> toJson() => _$TransitionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TransitionFromEnum {
@JsonValue(r'PENDING')
PENDING(r'PENDING'),
@JsonValue(r'DECLINED')
DECLINED(r'DECLINED'),
@JsonValue(r'CHARGE_FAILED')
CHARGE_FAILED(r'CHARGE_FAILED'),
@JsonValue(r'MISMATCH')
MISMATCH(r'MISMATCH'),
@JsonValue(r'RECEIVED')
RECEIVED(r'RECEIVED'),
@JsonValue(r'RETRYING')
RETRYING(r'RETRYING'),
@JsonValue(r'POSTED')
POSTED(r'POSTED'),
@JsonValue(r'NEEDS_ATTENTION')
NEEDS_ATTENTION(r'NEEDS_ATTENTION'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const TransitionFromEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TransitionToEnum {
@JsonValue(r'PENDING')
PENDING(r'PENDING'),
@JsonValue(r'DECLINED')
DECLINED(r'DECLINED'),
@JsonValue(r'CHARGE_FAILED')
CHARGE_FAILED(r'CHARGE_FAILED'),
@JsonValue(r'MISMATCH')
MISMATCH(r'MISMATCH'),
@JsonValue(r'RECEIVED')
RECEIVED(r'RECEIVED'),
@JsonValue(r'RETRYING')
RETRYING(r'RETRYING'),
@JsonValue(r'POSTED')
POSTED(r'POSTED'),
@JsonValue(r'NEEDS_ATTENTION')
NEEDS_ATTENTION(r'NEEDS_ATTENTION'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const TransitionToEnum(this.value);

final String value;

@override
String toString() => value;
}


