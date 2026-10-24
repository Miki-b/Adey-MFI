package et.adey.mfi.common;

import org.springframework.http.HttpStatus;

/** An error with a fixed HTTP status and one of our stable codes (see backend/README.md). */
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
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
