package et.adey.mfi.reconciliation;

import et.adey.mfi.reconciliation.ReconciliationMatcher.FineractPayment;
import et.adey.mfi.reconciliation.ReconciliationMatcher.Finding;
import et.adey.mfi.reconciliation.ReconciliationMatcher.IntentInfo;
import et.adey.mfi.reconciliation.ReconciliationMatcher.ProviderPayment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Every finding type of the three-way comparison. */
class ReconciliationMatcherTest {

    private static final Instant PAID = Instant.parse("2026-10-24T07:03:33Z");

    private static ProviderPayment provider(String ref, String receipt, String amount) {
        return new ProviderPayment("ch_" + ref, ref, receipt, new BigDecimal(amount), PAID);
    }

    private static IntentInfo intent(long id, String ref, String amount, String status) {
        return new IntentInfo(id, ref, 1L, 2L, new BigDecimal(amount), status);
    }

    private static FineractPayment fineract(long txId, String receipt, String amount) {
        return new FineractPayment(1L, 2L, txId, receipt, new BigDecimal(amount));
    }

    @Test
    void everyFindingType() {
        List<Finding> findings = ReconciliationMatcher.match(
                List.of(
                        provider("AMP-OK", "TP1", "1833.60"),      // paid, posted, same amount
                        provider("AMP-LOST", "TP2", "500.00"),     // paid, webhook lost
                        provider("AMP-DIFF", "TP3", "1000.00"),    // provider reports less than requested
                        provider("AMP-POSTDIFF", "TP4", "300.00")),// posted with a different amount
                List.of(
                        intent(1, "AMP-OK", "1833.60", "POSTED"),
                        intent(2, "AMP-LOST", "500.00", "PENDING"),
                        intent(3, "AMP-DIFF", "1833.60", "MISMATCH"),
                        intent(4, "AMP-POSTDIFF", "300.00", "POSTED")),
                List.of(
                        fineract(42, "TP1", "1833.6"),             // 1833.6 == 1833.60 (compareTo, not equals)
                        fineract(44, "TP4", "350.00"),
                        fineract(45, "TP-GHOST", "250.00")));      // nobody paid this

        assertThat(findings).extracting(Finding::type).containsExactly(
                FindingType.MATCHED, FindingType.PAID_NOT_POSTED, FindingType.AMOUNT_MISMATCH,
                FindingType.AMOUNT_MISMATCH, FindingType.POSTED_NOT_PAID);

        Finding lost = findings.get(1);
        assertThat(lost.intentId()).isEqualTo(2L);
        assertThat(lost.receiptNumber()).isEqualTo("TP2");
        assertThat(lost.providerPaidAt()).isEqualTo(PAID);
        assertThat(lost.detail()).contains("PENDING");

        assertThat(findings.get(2).detail()).contains("provider 1000.00, requested 1833.60");
        assertThat(findings.get(3).fineractTransactionId()).isEqualTo(44L);

        Finding ghost = findings.get(4);
        assertThat(ghost.receiptNumber()).isEqualTo("TP-GHOST");
        assertThat(ghost.fineractTransactionId()).isEqualTo(45L);
        assertThat(ghost.providerAmount()).isNull();
        assertThat(ghost.officeId()).isEqualTo(2L);
    }

    @Test
    void providerPaymentUnknownToUsIsPaidNotPostedWithoutIntent() {
        List<Finding> findings = ReconciliationMatcher.match(List.of(provider("AMP-WHO", "TP9", "100.00")),
                List.of(), List.of());

        assertThat(findings).singleElement().satisfies(f -> {
            assertThat(f.type()).isEqualTo(FindingType.PAID_NOT_POSTED);
            assertThat(f.intentId()).isNull();
            assertThat(f.officeId()).isNull();
            assertThat(f.detail()).contains("unknown to us");
        });
    }

    @Test
    void nothingAnywhereMeansNoFindings() {
        assertThat(ReconciliationMatcher.match(List.of(), List.of(), List.of())).isEmpty();
    }
}
