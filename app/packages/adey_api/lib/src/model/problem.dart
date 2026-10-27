//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:adey_api/src/model/problem_errors_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'problem.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Problem {
  /// Returns a new [Problem] instance.
  Problem({

    required  this.code,

     this.detail,

     this.errors,

     this.instance,

    required  this.status,

     this.title,

     this.type,
  });

      /// Stable error code: switch on this, not on detail
  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  unknownEnumValue: ProblemCodeEnum.unknownDefaultOpenApi,
  )


  final ProblemCodeEnum code;



      /// Human-readable explanation (may change; for people, not code)
  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final String? detail;



      /// Field-level problems (validation errors only)
  @JsonKey(
    
    name: r'errors',
    required: false,
    includeIfNull: false,
  )


  final List<ProblemErrorsInner>? errors;



      /// The request path
  @JsonKey(
    
    name: r'instance',
    required: false,
    includeIfNull: false,
  )


  final String? instance;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final int status;



      /// Short summary of the HTTP status
  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



      /// Problem type URI
  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final String? type;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Problem &&
      other.code == code &&
      other.detail == detail &&
      other.errors == errors &&
      other.instance == instance &&
      other.status == status &&
      other.title == title &&
      other.type == type;

    @override
    int get hashCode =>
        code.hashCode +
        detail.hashCode +
        errors.hashCode +
        instance.hashCode +
        status.hashCode +
        title.hashCode +
        type.hashCode;

  factory Problem.fromJson(Map<String, dynamic> json) => _$ProblemFromJson(json);

  Map<String, dynamic> toJson() => _$ProblemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Stable error code: switch on this, not on detail
enum ProblemCodeEnum {
@JsonValue(r'VALIDATION_FAILED')
VALIDATION_FAILED(r'VALIDATION_FAILED'),
@JsonValue(r'MALFORMED_REQUEST')
MALFORMED_REQUEST(r'MALFORMED_REQUEST'),
@JsonValue(r'INVALID_PARAMETER')
INVALID_PARAMETER(r'INVALID_PARAMETER'),
@JsonValue(r'INVALID_REQUEST')
INVALID_REQUEST(r'INVALID_REQUEST'),
@JsonValue(r'FINERACT_VALIDATION')
FINERACT_VALIDATION(r'FINERACT_VALIDATION'),
@JsonValue(r'IDEMPOTENCY_KEY_INVALID')
IDEMPOTENCY_KEY_INVALID(r'IDEMPOTENCY_KEY_INVALID'),
@JsonValue(r'RESOLUTION_NOTE_REQUIRED')
RESOLUTION_NOTE_REQUIRED(r'RESOLUTION_NOTE_REQUIRED'),
@JsonValue(r'PHONE_METHOD_MISMATCH')
PHONE_METHOD_MISMATCH(r'PHONE_METHOD_MISMATCH'),
@JsonValue(r'UNAUTHENTICATED')
UNAUTHENTICATED(r'UNAUTHENTICATED'),
@JsonValue(r'INVALID_CREDENTIALS')
INVALID_CREDENTIALS(r'INVALID_CREDENTIALS'),
@JsonValue(r'INVALID_REFRESH_TOKEN')
INVALID_REFRESH_TOKEN(r'INVALID_REFRESH_TOKEN'),
@JsonValue(r'WEBHOOK_SIGNATURE_INVALID')
WEBHOOK_SIGNATURE_INVALID(r'WEBHOOK_SIGNATURE_INVALID'),
@JsonValue(r'WEBHOOK_TIMESTAMP_INVALID')
WEBHOOK_TIMESTAMP_INVALID(r'WEBHOOK_TIMESTAMP_INVALID'),
@JsonValue(r'FORBIDDEN')
FORBIDDEN(r'FORBIDDEN'),
@JsonValue(r'NOT_AN_OFFICER')
NOT_AN_OFFICER(r'NOT_AN_OFFICER'),
@JsonValue(r'NOT_FOUND')
NOT_FOUND(r'NOT_FOUND'),
@JsonValue(r'METHOD_NOT_ALLOWED')
METHOD_NOT_ALLOWED(r'METHOD_NOT_ALLOWED'),
@JsonValue(r'NOT_ACCEPTABLE')
NOT_ACCEPTABLE(r'NOT_ACCEPTABLE'),
@JsonValue(r'UNSUPPORTED_MEDIA_TYPE')
UNSUPPORTED_MEDIA_TYPE(r'UNSUPPORTED_MEDIA_TYPE'),
@JsonValue(r'OPEN_LOAN_EXISTS')
OPEN_LOAN_EXISTS(r'OPEN_LOAN_EXISTS'),
@JsonValue(r'DUPLICATE_CLIENT')
DUPLICATE_CLIENT(r'DUPLICATE_CLIENT'),
@JsonValue(r'IDEMPOTENCY_IN_PROGRESS')
IDEMPOTENCY_IN_PROGRESS(r'IDEMPOTENCY_IN_PROGRESS'),
@JsonValue(r'IDEMPOTENCY_OUTCOME_UNKNOWN')
IDEMPOTENCY_OUTCOME_UNKNOWN(r'IDEMPOTENCY_OUTCOME_UNKNOWN'),
@JsonValue(r'CONFLICT')
CONFLICT(r'CONFLICT'),
@JsonValue(r'FINDING_ALREADY_RESOLVED')
FINDING_ALREADY_RESOLVED(r'FINDING_ALREADY_RESOLVED'),
@JsonValue(r'LOAN_WRONG_STATE')
LOAN_WRONG_STATE(r'LOAN_WRONG_STATE'),
@JsonValue(r'FINERACT_RULE')
FINERACT_RULE(r'FINERACT_RULE'),
@JsonValue(r'IDEMPOTENCY_KEY_REUSED')
IDEMPOTENCY_KEY_REUSED(r'IDEMPOTENCY_KEY_REUSED'),
@JsonValue(r'AMOUNT_EXCEEDS_OUTSTANDING')
AMOUNT_EXCEEDS_OUTSTANDING(r'AMOUNT_EXCEEDS_OUTSTANDING'),
@JsonValue(r'LOAN_NOT_ACTIVE')
LOAN_NOT_ACTIVE(r'LOAN_NOT_ACTIVE'),
@JsonValue(r'TOO_MANY_LOGIN_ATTEMPTS')
TOO_MANY_LOGIN_ATTEMPTS(r'TOO_MANY_LOGIN_ATTEMPTS'),
@JsonValue(r'INTERNAL_ERROR')
INTERNAL_ERROR(r'INTERNAL_ERROR'),
@JsonValue(r'FINERACT_ERROR')
FINERACT_ERROR(r'FINERACT_ERROR'),
@JsonValue(r'FINERACT_UNAVAILABLE')
FINERACT_UNAVAILABLE(r'FINERACT_UNAVAILABLE'),
@JsonValue(r'PROVIDER_UNAVAILABLE')
PROVIDER_UNAVAILABLE(r'PROVIDER_UNAVAILABLE'),
@JsonValue(r'POSTING_PENDING')
POSTING_PENDING(r'POSTING_PENDING'),
@JsonValue(r'unknown_default_open_api')
unknownDefaultOpenApi(r'unknown_default_open_api');

const ProblemCodeEnum(this.value);

final String value;

@override
String toString() => value;
}


