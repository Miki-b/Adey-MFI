package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A new loan application. Same terms as a schedule preview, plus the responsible loan officer. */
public record LoanApplicationRequest(
        @NotNull @Positive Long clientId,
        @NotNull @Positive Long productId,
        @Schema(example = "20000.00")
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal principal,
        @Schema(example = "12")
        @NotNull @Min(1) Integer numberOfRepayments,
        @Schema(description = "ISO date. Defaults to today.")
        LocalDate expectedDisbursementDate,
        @Schema(description = "Loan officer (staff) id. Optional.")
        @Positive Long staffId) {
}
