package et.adey.mfi.client;

import java.time.LocalDate;

/** One row in a client list/search result. */
public record ClientSummary(
        Long id,
        String displayName,
        String accountNo,
        String officeName,
        String staffName,
        boolean active,
        LocalDate activationDate) {
}
