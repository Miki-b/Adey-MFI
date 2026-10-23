package et.adey.mfi.client;

import et.adey.mfi.common.BadRequestException;
import et.adey.mfi.common.FineractDates;

import java.time.LocalDate;

/** Adey MFI's own rules for client data (ours, not Fineract's). */
final class ClientRules {

    private ClientRules() {
    }

    /** Ethio Telecom numbers start 09, Safaricom Ethiopia 07; 10 digits in national format. */
    static final String MOBILE_PATTERN = "^0[79][0-9]{8}$";

    /** Lending to minors is not allowed. */
    static final int MINIMUM_AGE = 18;

    static void requireAdult(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.plusYears(MINIMUM_AGE).isAfter(FineractDates.today())) {
            throw new BadRequestException("dateOfBirth", "The client must be at least " + MINIMUM_AGE + " years old.");
        }
    }
}
