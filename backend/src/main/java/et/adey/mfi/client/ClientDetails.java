package et.adey.mfi.client;

import java.time.LocalDate;

/** Full client profile. status is Fineract's label, e.g. "Active", "Pending". */
public record ClientDetails(
        Long id,
        String accountNo,
        String externalId,
        String displayName,
        String firstName,
        String middleName,
        String lastName,
        String mobileNo,
        LocalDate dateOfBirth,
        String gender,
        String status,
        boolean active,
        LocalDate submittedOnDate,
        LocalDate activationDate,
        Long officeId,
        String officeName,
        Long staffId,
        String staffName) {
}
