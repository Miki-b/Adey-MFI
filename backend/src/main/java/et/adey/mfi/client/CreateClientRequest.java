package et.adey.mfi.client;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** A new client. The office is always the caller's; the loan officer is the caller (officer) or chosen (manager). */
public record CreateClientRequest(
        @NotBlank @Size(max = 50) String firstname,
        @NotBlank @Size(max = 50) String lastname,
        @Schema(description = "Ethiopian mobile number, 10 digits starting 09 or 07", example = "0911223344")
        @NotBlank @Pattern(regexp = ClientRules.MOBILE_PATTERN, message = "must be 10 digits starting with 09 or 07")
        String mobileNo,
        @NotNull Gender gender,
        @Schema(description = "ISO date. The client must be at least 18 years old.", example = "1990-05-17")
        @NotNull @Past LocalDate dateOfBirth,
        @Schema(description = "National ID number (stored as Fineract externalId). Optional, must be unique.")
        @Size(max = 100) String externalId,
        @Schema(description = "Managers only: the loan officer (staff id) of your office to assign. "
                + "Officers leave it empty; the client is assigned to them.")
        @Positive Long staffId,
        @Schema(description = "Activate immediately (default true). Inactive clients cannot get loans.")
        Boolean activate) {
}
