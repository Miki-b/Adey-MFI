package et.adey.mfi.reconciliation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * RECONCILIATION = comparing independent records of the same money until they agree.
 * Webhooks can be lost, duplicated or forged; posting can fail; people make manual entries.
 * Every day we line up three sources and explain every difference:
 *
 *   provider settlements   (what the provider says was really paid)
 *   our payment intents    (what we asked for, by our reference)
 *   Fineract repayments    (what is on the clients' loan accounts), matched by RECEIPT NUMBER,
 *                          which is the provider's transaction id we put there when posting.
 *
 * Pure function: no database, no HTTP, so every rule is easy to unit test.
 */
public final class ReconciliationMatcher {

    private ReconciliationMatcher() {
    }

    public record ProviderPayment(String chargeId, String reference, String receiptNumber, BigDecimal amount,
                                  Instant paidAt) {
    }

    public record IntentInfo(Long intentId, String reference, Long loanId, Long officeId, BigDecimal amount,
                             String status) {
    }

    public record FineractPayment(Long loanId, Long officeId, Long transactionId, String receiptNumber,
                                  BigDecimal amount) {
    }

    public record Finding(FindingType type, Long officeId, Long loanId, Long intentId, String reference,
                          String receiptNumber, BigDecimal providerAmount, Instant providerPaidAt,
                          BigDecimal intentAmount, BigDecimal fineractAmount, Long fineractTransactionId,
                          String detail) {
    }

    public static List<Finding> match(List<ProviderPayment> provider, List<IntentInfo> intents,
                                      List<FineractPayment> fineract) {
        Map<String, IntentInfo> intentByRef = new LinkedHashMap<>();
        intents.forEach(i -> intentByRef.put(i.reference(), i));
        Map<String, FineractPayment> fineractByReceipt = new LinkedHashMap<>();
        fineract.forEach(f -> fineractByReceipt.put(f.receiptNumber(), f));

        List<Finding> findings = new ArrayList<>();
        for (ProviderPayment p : provider) {
            IntentInfo i = intentByRef.get(p.reference());
            FineractPayment f = fineractByReceipt.remove(p.receiptNumber());
            Long office = i != null ? i.officeId() : (f != null ? f.officeId() : null);
            Long loan = i != null ? i.loanId() : (f != null ? f.loanId() : null);
            BigDecimal intentAmount = i == null ? null : i.amount();

            FindingType type;
            String detail;
            if (f != null) {
                boolean providerVsFineract = same(p.amount(), f.amount());
                boolean providerVsIntent = i == null || same(p.amount(), i.amount());
                if (providerVsFineract && providerVsIntent) {
                    type = FindingType.MATCHED;
                    detail = "paid and posted";
                } else {
                    type = FindingType.AMOUNT_MISMATCH;
                    detail = "provider %s, Fineract %s, requested %s".formatted(p.amount(), f.amount(), intentAmount);
                }
            } else if (i != null && !same(p.amount(), i.amount())) {
                type = FindingType.AMOUNT_MISMATCH;
                detail = "provider %s, requested %s; not posted (intent %s)".formatted(p.amount(), i.amount(), i.status());
            } else {
                type = FindingType.PAID_NOT_POSTED;
                detail = i == null ? "paid at the provider, unknown to us (no intent with this reference)"
                        : "paid at the provider, no Fineract repayment with this receipt (intent " + i.status() + ")";
            }
            findings.add(new Finding(type, office, loan, i == null ? null : i.intentId(), p.reference(),
                    p.receiptNumber(), p.amount(), p.paidAt(), intentAmount, f == null ? null : f.amount(),
                    f == null ? null : f.transactionId(), detail));
        }
        // Whatever is left in Fineract has no provider payment behind it.
        for (FineractPayment f : fineractByReceipt.values()) {
            findings.add(new Finding(FindingType.POSTED_NOT_PAID, f.officeId(), f.loanId(), null, null,
                    f.receiptNumber(), null, null, null, f.amount(), f.transactionId(),
                    "Mobile Money repayment in Fineract without a provider payment"));
        }
        return findings;
    }

    private static boolean same(BigDecimal a, BigDecimal b) {
        return a != null && b != null ? a.compareTo(b) == 0 : Objects.equals(a, b);
    }
}
