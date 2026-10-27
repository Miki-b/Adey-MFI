package et.adey.mfi.client;

import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/** Full client profile. status is Fineract's label, e.g. "Active", "Pending". */
public record ClientDetails(
        Long id,
        String accountNo,
        @Nullable String externalId,
        String displayName,
        @Nullable String firstName,
        @Nullable String middleName,
        @Nullable String lastName,
        @Nullable String mobileNo,
        @Nullable LocalDate dateOfBirth,
        @Nullable String gender,
        @Nullable String status,
        boolean active,
        @Nullable LocalDate submittedOnDate,
        @Nullable LocalDate activationDate,
        Long officeId,
        String officeName,
        @Nullable Long staffId,
        @Nullable String staffName) {
}
