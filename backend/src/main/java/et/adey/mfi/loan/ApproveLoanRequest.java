package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** All optional: approve today, for the requested principal. */
public record ApproveLoanRequest(
        @Schema(description = "ISO date. Defaults to today.") LocalDate approvedOnDate,
        @Schema(description = "Approve less than requested. Defaults to the requested principal.")
        @Positive @Digits(integer = 12, fraction = 2) BigDecimal approvedAmount,
        @Size(max = 1000) String note) {
}
