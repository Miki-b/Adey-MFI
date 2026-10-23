package et.adey.mfi.idempotency;

import org.springframework.http.HttpStatus;

/** A retried request that cannot be answered safely. Carries the HTTP status and our stable code. */
public class IdempotencyException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public IdempotencyException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
