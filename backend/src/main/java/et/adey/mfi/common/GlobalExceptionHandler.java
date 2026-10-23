package et.adey.mfi.common;

import et.adey.mfi.auth.AuthenticationFailedException;
import et.adey.mfi.auth.TooManyLoginAttemptsException;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractErrorCodes;
import et.adey.mfi.idempotency.IdempotencyException;
import et.adey.mfi.fineract.FineractUnavailableException;
import et.adey.mfi.fineract.model.FineractError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.validation.ConstraintViolationException;

import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Turns exceptions into RFC 7807 "problem detail" JSON:
 * {"type", "title", "status", "detail", "instance", "errors": [{"field", "message"}]}.
 * Extending ResponseEntityExceptionHandler gives Spring's own errors (bad JSON, validation,
 * wrong method, ...) the same format.
 *
 * EVERY error carries a stable "code" (e.g. OPEN_LOAN_EXISTS) that the app can switch on; the
 * "detail" text is for people and may change. All codes are listed in backend/README.md.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** One field-level problem, e.g. {"field": "principal", "message": "must be between ..."}. */
    public record FieldProblem(String field, String message) {
    }

    @ExceptionHandler(FineractApiException.class)
    public ProblemDetail handleFineract(FineractApiException ex) {
        FineractError error = ex.getError();

        // Wrong-state actions (disburse a pending loan, approve an active one, ...) come back from
        // Fineract as plain 400 validation errors. We recognise them by error code and answer 422.
        Optional<String> wrongState = FineractErrorCodes.wrongStateMessage(error);
        if (wrongState.isPresent()) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, wrongState.get());
            problem.setTitle("Not allowed in the loan's current state");
            problem.setType(URI.create("urn:adey:problem:loan-state"));
            problem.setProperty("code", FineractErrorCodes.WRONG_LOAN_STATE);
            return problem;
        }

        Optional<String> duplicate = FineractErrorCodes.duplicateMessage(error);
        if (duplicate.isPresent()) {
            ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, duplicate.get());
            problem.setTitle("Already exists");
            problem.setProperty("code", FineractErrorCodes.DUPLICATE_CLIENT);
            return problem;
        }

        HttpStatus status = mapFineractStatus(ex.getFineractStatus());

        // Fineract's top-level message is often generic ("Validation errors exist."),
        // so when there is exactly one field error we use its clearer message.
        List<FineractError.FieldError> fieldErrors = error.errorsOrEmpty();
        String detail = fieldErrors.size() == 1 && fieldErrors.get(0).defaultUserMessage() != null
                ? fieldErrors.get(0).defaultUserMessage()
                : error.defaultUserMessage();

        if (status.is5xxServerError()) {
            // Not the caller's fault: log the details, return a generic message.
            log.error("Fineract failure (HTTP {}): {}", ex.getFineractStatus(), error.developerMessage());
            detail = ex.getFineractStatus() == 401
                    ? "The backend could not authenticate with Fineract."
                    : "The core banking system returned an error.";
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(titleFor(status));
        problem.setType(URI.create("urn:adey:problem:fineract"));
        problem.setProperty("code", fineractCode(status));
        if (!status.is5xxServerError() && !fieldErrors.isEmpty()) {
            problem.setProperty("errors", fieldErrors.stream()
                    .map(fe -> new FieldProblem(fe.parameterName(), fe.defaultUserMessage()))
                    .toList());
        }
        return problem;
    }

    /** One of our own input checks failed (e.g. a payment type officers may not use). */
    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid request");
        problem.setProperty("code", "INVALID_REQUEST");
        problem.setProperty("errors", List.of(new FieldProblem(ex.getField(), ex.getMessage())));
        return problem;
    }

    /** Too many failed logins: 429 + Retry-After (seconds), as HTTP clients expect. */
    @ExceptionHandler(TooManyLoginAttemptsException.class)
    public ResponseEntity<ProblemDetail> handleTooManyLogins(TooManyLoginAttemptsException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        problem.setTitle("Too many login attempts");
        problem.setProperty("code", "TOO_MANY_LOGIN_ATTEMPTS");
        problem.setProperty("retryAfterSeconds", ex.getRetryAfterSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .body(problem);
    }

    /** Login or refresh refused. Deliberately vague detail (see AuthService). */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ProblemDetail> handleAuthFailed(AuthenticationFailedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        problem.setTitle("Unauthorized");
        problem.setProperty("code", ex.getCode());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header("WWW-Authenticate", "Bearer").body(problem);
    }

    /**
     * Thrown by AccessPolicy (someone else's client, manager-only action). Must be handled here:
     * otherwise the catch-all Exception handler below would turn it into a 500.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        problem.setTitle("Forbidden");
        problem.setProperty("code", "FORBIDDEN");
        return problem;
    }

    /** A retried request that cannot be answered safely (see IdempotencyService). */
    @ExceptionHandler(IdempotencyException.class)
    public ProblemDetail handleIdempotency(IdempotencyException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        problem.setTitle("Idempotency-Key problem");
        problem.setProperty("code", ex.getCode());
        return problem;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Business rule violated");
        problem.setType(URI.create("urn:adey:problem:business-rule"));
        problem.setProperty("code", ex.getCode());
        return problem;
    }

    @ExceptionHandler(FineractUnavailableException.class)
    public ProblemDetail handleUnavailable(FineractUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The core banking system (Fineract) is not reachable. Try again in a moment.");
        problem.setTitle("Fineract unavailable");
        problem.setType(URI.create("urn:adey:problem:fineract-unavailable"));
        problem.setProperty("code", "FINERACT_UNAVAILABLE");
        return problem;
    }

    /** Bean Validation failures on @Valid request bodies: list every bad field. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail problem = ex.getBody();
        problem.setDetail("Some fields are invalid.");
        problem.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldProblem(fe.getField(), fe.getDefaultMessage()))
                .toList());
        return handleExceptionInternal(ex, problem, headers, status, request);
    }

    /**
     * Invalid query/path parameters on a @Validated controller (e.g. size=1000 with @Max(100)).
     * Without this handler they fell through to the catch-all and became a 500.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Some parameters are invalid.");
        problem.setTitle("Bad Request");
        problem.setProperty("code", "VALIDATION_FAILED");
        problem.setProperty("errors", ex.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    // "search.size" (method.parameter) -> "size"
                    return new FieldProblem(path.substring(path.lastIndexOf('.') + 1), v.getMessage());
                })
                .toList());
        return problem;
    }

    /** Anything unexpected: 500 without leaking internals (the stack trace goes to the log). */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side.");
        problem.setTitle("Internal error");
        problem.setProperty("code", "INTERNAL_ERROR");
        return problem;
    }

    /**
     * Every Spring MVC built-in error (bad JSON, missing/invalid parameter, unknown URL, wrong
     * method, ...) passes through here: add our code if the handler did not set one.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        // Let Spring build the body first: for some errors (e.g. unknown URL) it only creates the
        // ProblemDetail inside super.handleExceptionInternal.
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem
                && (problem.getProperties() == null || !problem.getProperties().containsKey("code"))) {
            problem.setProperty("code", springCode(ex, statusCode));
        }
        return response;
    }

    static String springCode(Exception ex, HttpStatusCode status) {
        if (ex instanceof MethodArgumentNotValidException || ex instanceof HandlerMethodValidationException) {
            return "VALIDATION_FAILED";
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "MALFORMED_REQUEST";
        }
        if (ex instanceof TypeMismatchException || ex instanceof MissingServletRequestParameterException
                || ex instanceof MissingRequestHeaderException || ex instanceof ServletRequestBindingException) {
            return "INVALID_PARAMETER";
        }
        return switch (status.value()) {
            case 404 -> "NOT_FOUND";
            case 405 -> "METHOD_NOT_ALLOWED";
            case 406 -> "NOT_ACCEPTABLE";
            case 415 -> "UNSUPPORTED_MEDIA_TYPE";
            default -> status.is4xxClientError() ? "INVALID_REQUEST" : "INTERNAL_ERROR";
        };
    }

    /** Fineract errors without a more specific meaning, by the status we answer with. */
    static String fineractCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "FINERACT_VALIDATION";
            case NOT_FOUND -> "NOT_FOUND";
            case CONFLICT -> "CONFLICT";
            case UNPROCESSABLE_CONTENT -> "FINERACT_RULE";
            default -> "FINERACT_ERROR";
        };
    }

    static HttpStatus mapFineractStatus(int fineractStatus) {
        return switch (fineractStatus) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            // Fineract uses 403 for business-rule violations (e.g. "loan not in approved state").
            case 403 -> HttpStatus.UNPROCESSABLE_CONTENT;
            // 401 means OUR credentials for Fineract are wrong: a server-side problem.
            case 401 -> HttpStatus.BAD_GATEWAY;
            default -> fineractStatus >= 500 ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST;
        };
    }

    private static String titleFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Invalid request";
            case NOT_FOUND -> "Not found";
            case UNPROCESSABLE_CONTENT -> "Business rule violated";
            default -> status.getReasonPhrase();
        };
    }
}
