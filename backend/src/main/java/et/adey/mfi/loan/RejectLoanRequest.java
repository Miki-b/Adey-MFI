package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RejectLoanRequest(
        @Schema(description = "ISO date. Defaults to today.") LocalDate rejectedOnDate,
        @Schema(description = "Why the application was rejected.") @Size(max = 1000) String note) {
}
