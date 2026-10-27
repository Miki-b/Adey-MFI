package et.adey.mfi.auth;

import et.adey.mfi.common.ApiException;
import org.springframework.http.HttpStatus;

/**
 * The Fineract login is valid, but the user is not linked to a staff member with our Loan Officer or
 * Branch Manager role (e.g. the Fineract admin "mifos"). 403 NOT_AN_OFFICER, so the app can say exactly
 * that instead of a generic "forbidden".
 */
public class NotAnOfficerException extends ApiException {

    public NotAnOfficerException() {
        super(HttpStatus.FORBIDDEN, "NOT_AN_OFFICER",
                "This account is not set up as an Adey MFI loan officer or branch manager.");
    }
}
