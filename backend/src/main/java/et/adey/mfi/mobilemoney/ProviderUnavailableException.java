package et.adey.mfi.mobilemoney;

import et.adey.mfi.common.ApiException;
import org.springframework.http.HttpStatus;

/** The mobile money provider could not be reached or refused the call. HTTP 503 PROVIDER_UNAVAILABLE. */
public class ProviderUnavailableException extends ApiException {

    public ProviderUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, "PROVIDER_UNAVAILABLE", message);
    }
}
