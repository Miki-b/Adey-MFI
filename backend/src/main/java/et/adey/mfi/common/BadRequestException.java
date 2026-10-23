package et.adey.mfi.common;

/** A request that breaks one of our own input rules (checked before calling Fineract). HTTP 400. */
public class BadRequestException extends RuntimeException {

    private final String field;

    public BadRequestException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
