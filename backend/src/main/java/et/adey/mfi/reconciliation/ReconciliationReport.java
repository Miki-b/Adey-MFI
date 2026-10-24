package et.adey.mfi.reconciliation;

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

    public record Summary(int matched, int paidNotPosted, int postedNotPaid, int amountMismatch, int unresolved,
                          BigDecimal providerTotal, BigDecimal fineractTotal, BigDecimal matchedTotal) {
    }

    public record FindingResponse(Long id, FindingType type, Long officeId, Long loanId, Long intentId,
                                  String reference, String receiptNumber, BigDecimal providerAmount,
                                  BigDecimal intentAmount, BigDecimal fineractAmount, Long fineractTransactionId,
                                  String detail, boolean resolved, Instant resolvedAt, String resolvedBy,
                                  String resolutionNote) {

        static FindingResponse of(ReconciliationFinding f) {
            return new FindingResponse(f.getId(), f.getType(), f.getOfficeId(), f.getLoanId(), f.getIntentId(),
                    f.getReference(), f.getReceiptNumber(), f.getProviderAmount(), f.getIntentAmount(),
                    f.getFineractAmount(), f.getFineractTransactionId(), f.getDetail(), f.isResolved(),
                    f.getResolvedAt(), f.getResolvedBy(), f.getResolutionNote());
        }
    }
}
