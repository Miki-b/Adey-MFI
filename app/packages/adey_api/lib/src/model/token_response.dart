//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:adey_api/src/model/officer_response.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'token_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TokenResponse {
  /// Returns a new [TokenResponse] instance.
  TokenResponse({

     this.accessToken,

     this.accessTokenExpiresAt,

     this.expiresIn,

     this.officer,

     this.refreshToken,

     this.refreshTokenExpiresAt,

     this.tokenType,
  });

  @JsonKey(
    
    name: r'accessToken',
    required: false,
    includeIfNull: false,
  )


  final String? accessToken;



  @JsonKey(
    
    name: r'accessTokenExpiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? accessTokenExpiresAt;



  @JsonKey(
    
    name: r'expiresIn',
    required: false,
    includeIfNull: false,
  )


  final int? expiresIn;



  @JsonKey(
    
    name: r'officer',
    required: false,
    includeIfNull: false,
  )


  final OfficerResponse? officer;



  @JsonKey(
    
    name: r'refreshToken',
    required: false,
    includeIfNull: false,
  )


  final String? refreshToken;



  @JsonKey(
    
    name: r'refreshTokenExpiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? refreshTokenExpiresAt;



  @JsonKey(
    
    name: r'tokenType',
    required: false,
    includeIfNull: false,
  )


  final String? tokenType;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TokenResponse &&
      other.accessToken == accessToken &&
      other.accessTokenExpiresAt == accessTokenExpiresAt &&
      other.expiresIn == expiresIn &&
      other.officer == officer &&
      other.refreshToken == refreshToken &&
      other.refreshTokenExpiresAt == refreshTokenExpiresAt &&
      other.tokenType == tokenType;

    @override
    int get hashCode =>
        accessToken.hashCode +
        accessTokenExpiresAt.hashCode +
        expiresIn.hashCode +
        officer.hashCode +
        refreshToken.hashCode +
        refreshTokenExpiresAt.hashCode +
        tokenType.hashCode;

  factory TokenResponse.fromJson(Map<String, dynamic> json) => _$TokenResponseFromJson(json);

  Map<String, dynamic> toJson() => _$TokenResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

