// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'token_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TokenResponseCWProxy {
  TokenResponse accessToken(String? accessToken);

  TokenResponse accessTokenExpiresAt(DateTime? accessTokenExpiresAt);

  TokenResponse expiresIn(int? expiresIn);

  TokenResponse officer(OfficerResponse? officer);

  TokenResponse refreshToken(String? refreshToken);

  TokenResponse refreshTokenExpiresAt(DateTime? refreshTokenExpiresAt);

  TokenResponse tokenType(String? tokenType);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `TokenResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// TokenResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  TokenResponse call({
    String? accessToken,
    DateTime? accessTokenExpiresAt,
    int? expiresIn,
    OfficerResponse? officer,
    String? refreshToken,
    DateTime? refreshTokenExpiresAt,
    String? tokenType,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfTokenResponse.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfTokenResponse.copyWith.fieldName(...)`
class _$TokenResponseCWProxyImpl implements _$TokenResponseCWProxy {
  const _$TokenResponseCWProxyImpl(this._value);

  final TokenResponse _value;

  @override
  TokenResponse accessToken(String? accessToken) =>
      this(accessToken: accessToken);

  @override
  TokenResponse accessTokenExpiresAt(DateTime? accessTokenExpiresAt) =>
      this(accessTokenExpiresAt: accessTokenExpiresAt);

  @override
  TokenResponse expiresIn(int? expiresIn) => this(expiresIn: expiresIn);

  @override
  TokenResponse officer(OfficerResponse? officer) => this(officer: officer);

  @override
  TokenResponse refreshToken(String? refreshToken) =>
      this(refreshToken: refreshToken);

  @override
  TokenResponse refreshTokenExpiresAt(DateTime? refreshTokenExpiresAt) =>
      this(refreshTokenExpiresAt: refreshTokenExpiresAt);

  @override
  TokenResponse tokenType(String? tokenType) => this(tokenType: tokenType);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `TokenResponse(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// TokenResponse(...).copyWith(id: 12, name: "My name")
  /// ````
  TokenResponse call({
    Object? accessToken = const $CopyWithPlaceholder(),
    Object? accessTokenExpiresAt = const $CopyWithPlaceholder(),
    Object? expiresIn = const $CopyWithPlaceholder(),
    Object? officer = const $CopyWithPlaceholder(),
    Object? refreshToken = const $CopyWithPlaceholder(),
    Object? refreshTokenExpiresAt = const $CopyWithPlaceholder(),
    Object? tokenType = const $CopyWithPlaceholder(),
  }) {
    return TokenResponse(
      accessToken: accessToken == const $CopyWithPlaceholder()
          ? _value.accessToken
          // ignore: cast_nullable_to_non_nullable
          : accessToken as String?,
      accessTokenExpiresAt: accessTokenExpiresAt == const $CopyWithPlaceholder()
          ? _value.accessTokenExpiresAt
          // ignore: cast_nullable_to_non_nullable
          : accessTokenExpiresAt as DateTime?,
      expiresIn: expiresIn == const $CopyWithPlaceholder()
          ? _value.expiresIn
          // ignore: cast_nullable_to_non_nullable
          : expiresIn as int?,
      officer: officer == const $CopyWithPlaceholder()
          ? _value.officer
          // ignore: cast_nullable_to_non_nullable
          : officer as OfficerResponse?,
      refreshToken: refreshToken == const $CopyWithPlaceholder()
          ? _value.refreshToken
          // ignore: cast_nullable_to_non_nullable
          : refreshToken as String?,
      refreshTokenExpiresAt:
          refreshTokenExpiresAt == const $CopyWithPlaceholder()
          ? _value.refreshTokenExpiresAt
          // ignore: cast_nullable_to_non_nullable
          : refreshTokenExpiresAt as DateTime?,
      tokenType: tokenType == const $CopyWithPlaceholder()
          ? _value.tokenType
          // ignore: cast_nullable_to_non_nullable
          : tokenType as String?,
    );
  }
}

extension $TokenResponseCopyWith on TokenResponse {
  /// Returns a callable class that can be used as follows: `instanceOfTokenResponse.copyWith(...)` or like so:`instanceOfTokenResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TokenResponseCWProxy get copyWith => _$TokenResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TokenResponse _$TokenResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TokenResponse', json, ($checkedConvert) {
      final val = TokenResponse(
        accessToken: $checkedConvert('accessToken', (v) => v as String?),
        accessTokenExpiresAt: $checkedConvert(
          'accessTokenExpiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        expiresIn: $checkedConvert('expiresIn', (v) => (v as num?)?.toInt()),
        officer: $checkedConvert(
          'officer',
          (v) => v == null
              ? null
              : OfficerResponse.fromJson(v as Map<String, dynamic>),
        ),
        refreshToken: $checkedConvert('refreshToken', (v) => v as String?),
        refreshTokenExpiresAt: $checkedConvert(
          'refreshTokenExpiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        tokenType: $checkedConvert('tokenType', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TokenResponseToJson(
  TokenResponse instance,
) => <String, dynamic>{
  'accessToken': ?instance.accessToken,
  'accessTokenExpiresAt': ?instance.accessTokenExpiresAt?.toIso8601String(),
  'expiresIn': ?instance.expiresIn,
  'officer': ?instance.officer?.toJson(),
  'refreshToken': ?instance.refreshToken,
  'refreshTokenExpiresAt': ?instance.refreshTokenExpiresAt?.toIso8601String(),
  'tokenType': ?instance.tokenType,
};
