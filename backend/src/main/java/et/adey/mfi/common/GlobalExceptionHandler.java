package et.adey.mfi.common;

import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractUnavailableException;
import et.adey.mfi.fineract.model.FineractError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;

/**
 * Turns exceptions into RFC 7807 "problem detail" JSON:
 * {"type", "title", "status", "detail", "instance", "errors": [{"field", "message"}]}.
 * Extending ResponseEntityExceptionHandler gives Spring's own errors (bad JSON, validation,
 * wrong method, ...) the same format.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** One field-level problem, e.g. {"field": "principal", "message": "must be between ..."}. */
    public record FieldProblem(String field, String message) {
    }

    @ExceptionHandler(FineractApiException.class)
    public ProblemDetail handleFineract(FineractApiException ex) {
        HttpStatus status = mapFineractStatus(ex.getFineractStatus());
        FineractError error = ex.getError();

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
        if (!status.is5xxServerError() && !fieldErrors.isEmpty()) {
            problem.setProperty("errors", fieldErrors.stream()
                    .map(fe -> new FieldProblem(fe.parameterName(), fe.defaultUserMessage()))
                    .toList());
        }
        return problem;
    }

    @ExceptionHandler(FineractUnavailableException.class)
    public ProblemDetail handleUnavailable(FineractUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "The core banking system (Fineract) is not reachable. Try again in a moment.");
        problem.setTitle("Fineract unavailable");
        problem.setType(URI.create("urn:adey:problem:fineract-unavailable"));
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

    /** Anything unexpected: 500 without leaking internals (the stack trace goes to the log). */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong on our side.");
        problem.setTitle("Internal error");
        return problem;
    }

    static HttpStatus mapFineractStatus(int fineractStatus) {
        return switch (fineractStatus) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            // Fineract uses 403 for business-rule violations (e.g. "loan not in approved state").
            case 403 -> HttpStatus.UNPROCESSABLE_ENTITY;
            // 401 means OUR credentials for Fineract are wrong: a server-side problem.
            case 401 -> HttpStatus.BAD_GATEWAY;
            default -> fineractStatus >= 500 ? HttpStatus.BAD_GATEWAY : HttpStatus.BAD_REQUEST;
        };
    }

    private static String titleFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> "Invalid request";
            case NOT_FOUND -> "Not found";
            case UNPROCESSABLE_ENTITY -> "Business rule violated";
            default -> status.getReasonPhrase();
        };
    }
}
