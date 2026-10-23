package et.adey.mfi.fineract;

/** Could not reach Fineract at all (down, refused connection, timeout, TLS failure). */
public class FineractUnavailableException extends RuntimeException {

    public FineractUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
