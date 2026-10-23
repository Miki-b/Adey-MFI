package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RepaymentRequest(
        @Schema(description = "ISO date. Defaults to today.") LocalDate transactionDate,
        @Schema(example = "1833.60")
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount,
        @Schema(description = "How the client paid (see GET /api/payment-types).")
        @Positive Long paymentTypeId,
        @Size(max = 1000) String note) {
}
