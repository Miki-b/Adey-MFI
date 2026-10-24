package et.adey.mfi.reconciliation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/** note is required for every finding except PAID_NOT_POSTED with a known payment (that one is posted). */
public record ResolveRequest(
        @Schema(example = "Client confirmed the 1000 ETB; difference collected in cash.")
        @Size(max = 1000) String note) {
}
