// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'problem.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ProblemCWProxy {
  Problem code(ProblemCodeEnum code);

  Problem detail(String? detail);

  Problem errors(List<ProblemErrorsInner>? errors);

  Problem instance(String? instance);

  Problem status(int status);

  Problem title(String? title);

  Problem type(String? type);

  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Problem(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Problem(...).copyWith(id: 12, name: "My name")
  /// ````
  Problem call({
    ProblemCodeEnum code,
    String? detail,
    List<ProblemErrorsInner>? errors,
    String? instance,
    int status,
    String? title,
    String? type,
  });
}

/// Proxy class for `copyWith` functionality. This is a callable class and can be used as follows: `instanceOfProblem.copyWith(...)`. Additionally contains functions for specific fields e.g. `instanceOfProblem.copyWith.fieldName(...)`
class _$ProblemCWProxyImpl implements _$ProblemCWProxy {
  const _$ProblemCWProxyImpl(this._value);

  final Problem _value;

  @override
  Problem code(ProblemCodeEnum code) => this(code: code);

  @override
  Problem detail(String? detail) => this(detail: detail);

  @override
  Problem errors(List<ProblemErrorsInner>? errors) => this(errors: errors);

  @override
  Problem instance(String? instance) => this(instance: instance);

  @override
  Problem status(int status) => this(status: status);

  @override
  Problem title(String? title) => this(title: title);

  @override
  Problem type(String? type) => this(type: type);

  @override
  /// This function **does support** nullification of nullable fields. All `null` values passed to `non-nullable` fields will be ignored. You can also use `Problem(...).copyWith.fieldName(...)` to override fields one at a time with nullification support.
  ///
  /// Usage
  /// ```dart
  /// Problem(...).copyWith(id: 12, name: "My name")
  /// ````
  Problem call({
    Object? code = const $CopyWithPlaceholder(),
    Object? detail = const $CopyWithPlaceholder(),
    Object? errors = const $CopyWithPlaceholder(),
    Object? instance = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
  }) {
    return Problem(
      code: code == const $CopyWithPlaceholder()
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as ProblemCodeEnum,
      detail: detail == const $CopyWithPlaceholder()
          ? _value.detail
          // ignore: cast_nullable_to_non_nullable
          : detail as String?,
      errors: errors == const $CopyWithPlaceholder()
          ? _value.errors
          // ignore: cast_nullable_to_non_nullable
          : errors as List<ProblemErrorsInner>?,
      instance: instance == const $CopyWithPlaceholder()
          ? _value.instance
          // ignore: cast_nullable_to_non_nullable
          : instance as String?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as int,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      type: type == const $CopyWithPlaceholder()
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as String?,
    );
  }
}

extension $ProblemCopyWith on Problem {
  /// Returns a callable class that can be used as follows: `instanceOfProblem.copyWith(...)` or like so:`instanceOfProblem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ProblemCWProxy get copyWith => _$ProblemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Problem _$ProblemFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Problem',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['code', 'status']);
    final val = Problem(
      code: $checkedConvert(
        'code',
        (v) => $enumDecode(
          _$ProblemCodeEnumEnumMap,
          v,
          unknownValue: ProblemCodeEnum.unknownDefaultOpenApi,
        ),
      ),
      detail: $checkedConvert('detail', (v) => v as String?),
      errors: $checkedConvert(
        'errors',
        (v) => (v as List<dynamic>?)
            ?.map((e) => ProblemErrorsInner.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      instance: $checkedConvert('instance', (v) => v as String?),
      status: $checkedConvert('status', (v) => (v as num).toInt()),
      title: $checkedConvert('title', (v) => v as String?),
      type: $checkedConvert('type', (v) => v as String?),
    );
    return val;
  },
);

Map<String, dynamic> _$ProblemToJson(Problem instance) => <String, dynamic>{
  'code': _$ProblemCodeEnumEnumMap[instance.code]!,
  'detail': ?instance.detail,
  'errors': ?instance.errors?.map((e) => e.toJson()).toList(),
  'instance': ?instance.instance,
  'status': instance.status,
  'title': ?instance.title,
  'type': ?instance.type,
};

const _$ProblemCodeEnumEnumMap = {
  ProblemCodeEnum.VALIDATION_FAILED: 'VALIDATION_FAILED',
  ProblemCodeEnum.MALFORMED_REQUEST: 'MALFORMED_REQUEST',
  ProblemCodeEnum.INVALID_PARAMETER: 'INVALID_PARAMETER',
  ProblemCodeEnum.INVALID_REQUEST: 'INVALID_REQUEST',
  ProblemCodeEnum.FINERACT_VALIDATION: 'FINERACT_VALIDATION',
  ProblemCodeEnum.IDEMPOTENCY_KEY_INVALID: 'IDEMPOTENCY_KEY_INVALID',
  ProblemCodeEnum.RESOLUTION_NOTE_REQUIRED: 'RESOLUTION_NOTE_REQUIRED',
  ProblemCodeEnum.PHONE_METHOD_MISMATCH: 'PHONE_METHOD_MISMATCH',
  ProblemCodeEnum.UNAUTHENTICATED: 'UNAUTHENTICATED',
  ProblemCodeEnum.INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  ProblemCodeEnum.INVALID_REFRESH_TOKEN: 'INVALID_REFRESH_TOKEN',
  ProblemCodeEnum.WEBHOOK_SIGNATURE_INVALID: 'WEBHOOK_SIGNATURE_INVALID',
  ProblemCodeEnum.WEBHOOK_TIMESTAMP_INVALID: 'WEBHOOK_TIMESTAMP_INVALID',
  ProblemCodeEnum.FORBIDDEN: 'FORBIDDEN',
  ProblemCodeEnum.NOT_AN_OFFICER: 'NOT_AN_OFFICER',
  ProblemCodeEnum.NOT_FOUND: 'NOT_FOUND',
  ProblemCodeEnum.METHOD_NOT_ALLOWED: 'METHOD_NOT_ALLOWED',
  ProblemCodeEnum.NOT_ACCEPTABLE: 'NOT_ACCEPTABLE',
  ProblemCodeEnum.UNSUPPORTED_MEDIA_TYPE: 'UNSUPPORTED_MEDIA_TYPE',
  ProblemCodeEnum.OPEN_LOAN_EXISTS: 'OPEN_LOAN_EXISTS',
  ProblemCodeEnum.DUPLICATE_CLIENT: 'DUPLICATE_CLIENT',
  ProblemCodeEnum.IDEMPOTENCY_IN_PROGRESS: 'IDEMPOTENCY_IN_PROGRESS',
  ProblemCodeEnum.IDEMPOTENCY_OUTCOME_UNKNOWN: 'IDEMPOTENCY_OUTCOME_UNKNOWN',
  ProblemCodeEnum.CONFLICT: 'CONFLICT',
  ProblemCodeEnum.FINDING_ALREADY_RESOLVED: 'FINDING_ALREADY_RESOLVED',
  ProblemCodeEnum.LOAN_WRONG_STATE: 'LOAN_WRONG_STATE',
  ProblemCodeEnum.FINERACT_RULE: 'FINERACT_RULE',
  ProblemCodeEnum.IDEMPOTENCY_KEY_REUSED: 'IDEMPOTENCY_KEY_REUSED',
  ProblemCodeEnum.AMOUNT_EXCEEDS_OUTSTANDING: 'AMOUNT_EXCEEDS_OUTSTANDING',
  ProblemCodeEnum.LOAN_NOT_ACTIVE: 'LOAN_NOT_ACTIVE',
  ProblemCodeEnum.TOO_MANY_LOGIN_ATTEMPTS: 'TOO_MANY_LOGIN_ATTEMPTS',
  ProblemCodeEnum.INTERNAL_ERROR: 'INTERNAL_ERROR',
  ProblemCodeEnum.FINERACT_ERROR: 'FINERACT_ERROR',
  ProblemCodeEnum.FINERACT_UNAVAILABLE: 'FINERACT_UNAVAILABLE',
  ProblemCodeEnum.PROVIDER_UNAVAILABLE: 'PROVIDER_UNAVAILABLE',
  ProblemCodeEnum.POSTING_PENDING: 'POSTING_PENDING',
  ProblemCodeEnum.unknownDefaultOpenApi: 'unknown_default_open_api',
};
