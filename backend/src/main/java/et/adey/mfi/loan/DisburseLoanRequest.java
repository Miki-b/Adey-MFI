package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Pays out the approved amount. All optional. */
public record DisburseLoanRequest(
        @Schema(description = "ISO date. Defaults to today.") LocalDate actualDisbursementDate,
        @Schema(description = "How the money was handed over (see GET /api/payment-types).")
        @Positive Long paymentTypeId,
        @Size(max = 1000) String note) {
}
