package et.adey.mfi.reconciliation;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanCommands;
import et.adey.mfi.fineract.model.FineractPage;
import et.adey.mfi.fineract.model.FineractPaymentType;
import et.adey.mfi.mobilemoney.PaymentIntent;
import et.adey.mfi.mobilemoney.PaymentIntentTestAccess;
import et.adey.mfi.mobilemoney.PaymentStatus;
import et.adey.mfi.mobilemoney.TelePayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static et.adey.mfi.TestOfficers.SAMUEL;
import static et.adey.mfi.TestOfficers.SELAMAWIT;
import static et.adey.mfi.TestOfficers.TADESSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Run, view (office-scoped), resolve, and carry resolutions forward, through the HTTP API. */
class ReconciliationIntegrationTest extends ApiTestBase {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 24);
    private static final Instant PAID = Instant.parse("2026-10-24T07:03:33Z");

    private PaymentIntent lost;

    @BeforeEach
    void threeSourcesForOneDay() {
        // Our side: one payment whose webhook never came (still PENDING), one posted normally.
        lost = intent("AMP-LOST", "ch_lost", "1833.60");
        intent("AMP-OK", "ch_ok", "400.00");

        // Provider: both were paid.
        when(telePay.settlements(DAY)).thenReturn(List.of(
                new TelePayClient.Settlement("ch_lost", "AMP-LOST", "TP-LOST", new BigDecimal("1833.60"), "ETB", PAID),
                new TelePayClient.Settlement("ch_ok", "AMP-OK", "TP-OK", new BigDecimal("400.00"), "ETB", PAID)));

        // Fineract: TP-OK posted, plus a Mobile Money repayment nobody paid (TP-GHOST).
        when(fineract.listLoans(0, 200)).thenReturn(new FineractPage<>(1, List.of(json.readValue(
                "{\"id\": 1, \"clientId\": 1, \"clientOfficeId\": 2, \"status\": {\"id\": 300}}", FineractLoan.class))));
        when(fineract.getLoanWithScheduleAndTransactions(1L)).thenReturn(json.readValue("""
                {"id": 1, "clientId": 1, "status": {"id": 300}, "transactions": [
                  {"id": 45, "type": {"code": "loanTransactionType.repayment"}, "date": [2026, 10, 24], "amount": 400.0,
                   "paymentDetailData": {"paymentType": {"id": 5, "name": "Mobile Money"}, "receiptNumber": "TP-OK"}},
                  {"id": 44, "type": {"code": "loanTransactionType.repayment"}, "date": [2026, 10, 24], "amount": 250.0,
                   "paymentDetailData": {"paymentType": {"id": 5, "name": "Mobile Money"}, "receiptNumber": "TP-GHOST"}},
                  {"id": 39, "type": {"code": "loanTransactionType.repayment"}, "date": [2026, 10, 24], "amount": 500.0,
                   "paymentDetailData": {"paymentType": {"id": 4, "name": "Cash"}}}
                ]}""", FineractLoan.class));
    }

    @Test
    void managerRunsAndSeesFindingsOfTheirOfficeOnly() throws Exception {
        mvc.perform(post("/api/reconciliation/run?date=2026-10-24").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/reconciliation/run?date=2026-10-24").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.matched").value(1))
                .andExpect(jsonPath("$.summary.paidNotPosted").value(1))
                .andExpect(jsonPath("$.summary.postedNotPaid").value(1))
                .andExpect(jsonPath("$.summary.amountMismatch").value(0))
                .andExpect(jsonPath("$.summary.providerTotal").value(2233.6))
                .andExpect(jsonPath("$.summary.fineractTotal").value(650.0)) // Cash repayment ignored
                .andExpect(jsonPath("$.findings[0].type").value("PAID_NOT_POSTED"))
                .andExpect(jsonPath("$.findings[0].intentId").value(lost.getId()));

        mvc.perform(get("/api/reconciliation/2026-10-24").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.findings.length()").value(3));
        // Adama's manager: none of Bishoftu's findings.
        mvc.perform(get("/api/reconciliation/2026-10-24").header("Authorization", bearer(SELAMAWIT)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.findings.length()").value(0));
        mvc.perform(get("/api/reconciliation/2026-10-23").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isNotFound());
        assertThat(auditLog.findAll()).anySatisfy(r -> assertThat(r.getAction()).isEqualTo(AuditAction.RECONCILIATION_RUN));
    }

    @Test
    void resolvingPaidNotPostedPostsTheRepayment() throws Exception {
        when(fineract.getPaymentTypes()).thenReturn(List.of(new FineractPaymentType(5L, "Mobile Money", null, false, 2, false)));
        when(fineract.repayLoan(eq(1L), any())).thenReturn(new FineractCommandResult(2L, 1L, 1L, 46L, Map.of()));
        long findingId = runAndFind(FindingType.PAID_NOT_POSTED);

        mvc.perform(post("/api/reconciliation/findings/" + findingId + "/resolve").header("Authorization", bearer(SELAMAWIT)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/reconciliation/findings/" + findingId + "/resolve").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved").value(true))
                .andExpect(jsonPath("$.resolutionNote").value("Posted via reconciliation as Fineract transaction 46"));

        PaymentIntent posted = paymentIntents.findById(lost.getId()).orElseThrow();
        assertThat(posted.getStatus()).isEqualTo(PaymentStatus.POSTED);
        assertThat(posted.getProviderTransactionId()).isEqualTo("TP-LOST");
        verify(fineract).repayLoan(eq(1L), org.mockito.ArgumentMatchers.argThat(r -> r instanceof FineractLoanCommands.Repayment rep
                && "TP-LOST".equals(rep.receiptNumber())
                && rep.transactionAmount().compareTo(new BigDecimal("1833.60")) == 0));

        mvc.perform(post("/api/reconciliation/findings/" + findingId + "/resolve").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("FINDING_ALREADY_RESOLVED"));
    }

    @Test
    void otherFindingsNeedANoteAndStayResolvedInLaterRuns() throws Exception {
        long ghost = runAndFind(FindingType.POSTED_NOT_PAID);

        mvc.perform(post("/api/reconciliation/findings/" + ghost + "/resolve").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("RESOLUTION_NOTE_REQUIRED"));
        mvc.perform(post("/api/reconciliation/findings/" + ghost + "/resolve").header("Authorization", bearer(TADESSE))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"note\": \"Test entry, reversed in Fineract.\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resolved").value(true));

        // Next day's run sees the same difference again, already explained.
        mvc.perform(post("/api/reconciliation/run?date=2026-10-24").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.findings[?(@.type == 'POSTED_NOT_PAID')].resolved").value(true))
                .andExpect(jsonPath("$.summary.unresolved").value(1)); // only PAID_NOT_POSTED left
    }

    private long runAndFind(FindingType type) throws Exception {
        String body = mvc.perform(post("/api/reconciliation/run?date=2026-10-24").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        for (var f : json.readTree(body).path("findings")) {
            if (type.name().equals(f.path("type").asString())) {
                return f.path("id").asLong();
            }
        }
        throw new AssertionError("no " + type + " finding");
    }

    private PaymentIntent intent(String ref, String chargeId, String amount) {
        PaymentIntent i = new PaymentIntent(ref, 1, 1, 2, 1L, "samuel", "0922100001", new BigDecimal(amount), "ETB",
                Instant.now());
        PaymentIntentTestAccess.chargeCreated(i, chargeId);
        return paymentIntents.save(i);
    }
}
