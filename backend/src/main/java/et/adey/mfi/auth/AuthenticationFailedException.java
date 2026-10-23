package et.adey.mfi.auth;

/** Login or refresh refused. Always HTTP 401, with a deliberately vague message (don't help guessers). */
public class AuthenticationFailedException extends RuntimeException {

    private final String code;

    public AuthenticationFailedException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
