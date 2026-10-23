package et.adey.mfi.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Edit basic details. Only the fields you send are changed; at least one is required. */
public record UpdateClientRequest(
        @Size(min = 1, max = 50) String firstname,
        @Size(min = 1, max = 50) String lastname,
        @Schema(example = "0911223344")
        @Pattern(regexp = ClientRules.MOBILE_PATTERN, message = "must be 10 digits starting with 09 or 07")
        String mobileNo,
        Gender gender,
        @Past LocalDate dateOfBirth,
        @Size(max = 100) String externalId) {

    public boolean isEmpty() {
        return firstname == null && lastname == null && mobileNo == null && gender == null
                && dateOfBirth == null && externalId == null;
    }
}
