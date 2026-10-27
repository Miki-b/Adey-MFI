package et.adey.mfi.reconciliation;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * GET /api/reconciliation/{date}: the latest completed run for the date, limited to the manager's
 * office (plus findings whose office is unknown, e.g. a provider payment nobody asked for).
 */
public record ReconciliationReport(
        LocalDate date,
        Long runId,
        Instant runAt,
        String triggeredBy,
        Summary summary,
        List<FindingResponse> findings) {

    @Schema(name = "ReconciliationSummary") // unique schema name (LoanDetails has a Summary too)
    public record Summary(int matched, int paidNotPosted, int postedNotPaid, int amountMismatch, int unresolved,
                          BigDecimal providerTotal, BigDecimal fineractTotal, BigDecimal matchedTotal) {
    }

    public record FindingResponse(Long id, FindingType type, @Nullable Long officeId, @Nullable Long loanId, @Nullable Long intentId,
                                  @Nullable String reference, @Nullable String receiptNumber, @Nullable BigDecimal providerAmount,
                                  @Nullable BigDecimal intentAmount, @Nullable BigDecimal fineractAmount, @Nullable Long fineractTransactionId,
                                  @Nullable String detail, boolean resolved, @Nullable Instant resolvedAt, @Nullable String resolvedBy,
                                  @Nullable String resolutionNote) {

        static FindingResponse of(ReconciliationFinding f) {
            return new FindingResponse(f.getId(), f.getType(), f.getOfficeId(), f.getLoanId(), f.getIntentId(),
                    f.getReference(), f.getReceiptNumber(), f.getProviderAmount(), f.getIntentAmount(),
                    f.getFineractAmount(), f.getFineractTransactionId(), f.getDetail(), f.isResolved(),
                    f.getResolvedAt(), f.getResolvedBy(), f.getResolutionNote());
        }
    }
}
