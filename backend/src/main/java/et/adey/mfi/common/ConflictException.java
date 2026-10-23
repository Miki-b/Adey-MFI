package et.adey.mfi.common;

/**
 * One of OUR business rules blocks the request (e.g. "client already has an open loan").
 * Becomes HTTP 409 Conflict. code is a stable identifier the app can branch on.
 */
public class ConflictException extends RuntimeException {

    private final String code;

    public ConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
