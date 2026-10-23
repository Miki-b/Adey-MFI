package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Input for a schedule preview. Range checks (e.g. principal 5,000-50,000) are the product's
 * rules and are enforced by Fineract; here we only reject obviously malformed input.
 */
public record SchedulePreviewRequest(
        @NotNull @Positive Long clientId,
        @NotNull @Positive Long productId,
        @Schema(example = "20000.00")
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal principal,
        @Schema(example = "12")
        @NotNull @Min(1) Integer numberOfRepayments,
        @Schema(description = "ISO date (yyyy-MM-dd). Defaults to today.", example = "2026-10-23")
        LocalDate expectedDisbursementDate) {
}
