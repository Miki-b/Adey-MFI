package et.adey.mfi.fineract;

import et.adey.mfi.fineract.model.FineractError;

/**
 * Fineract answered, but with an error status (4xx/5xx).
 * Carries Fineract's status and parsed error body; GlobalExceptionHandler turns it into a ProblemDetail.
 */
public class FineractApiException extends RuntimeException {

    private final int fineractStatus;
    private final FineractError error;

    public FineractApiException(int fineractStatus, FineractError error) {
        super("Fineract returned HTTP %d: %s".formatted(fineractStatus, error.defaultUserMessage()));
        this.fineractStatus = fineractStatus;
        this.error = error;
    }

    public int getFineractStatus() {
        return fineractStatus;
    }

    public FineractError getError() {
        return error;
    }
}
