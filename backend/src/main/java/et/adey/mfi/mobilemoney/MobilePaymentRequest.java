package et.adey.mfi.mobilemoney;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Collect a repayment from the client's mobile wallet. */
public record MobilePaymentRequest(
        @Schema(example = "1833.60")
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount,
        @Schema(description = "Wallet phone number, 10 digits starting 09 or 07", example = "0922100001")
        @NotBlank @Pattern(regexp = "^0[79][0-9]{8}$", message = "must be 10 digits starting with 09 or 07")
        String phone) {
}
