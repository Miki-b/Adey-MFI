package et.adey.mfi.client;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/** One row in a client list/search result. */
public record ClientSummary(
        Long id,
        String displayName,
        String accountNo,
        String officeName,
        @Nullable String staffName,
        boolean active,
        @Nullable LocalDate activationDate) {
}
