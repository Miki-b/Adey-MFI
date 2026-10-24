package et.adey.mfi.mobilemoney;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Instant;

import static et.adey.mfi.TestOfficers.HANA;
import static et.adey.mfi.TestOfficers.SAMUEL;
import static et.adey.mfi.TestOfficers.TADESSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The webhook endpoint (signature, replay, inbox) and the officer's request/poll endpoints. */
class MobileMoneyIntegrationTest extends ApiTestBase {

    @Autowired
    private MobileMoneyProperties props;

    private PaymentIntent pending;

    @BeforeEach
    void anIntentWaitingForThePhone() {
        PaymentIntent i = new PaymentIntent("AMP-TEST00000001", 1, 1, 2, 1L, "samuel", "0922100001",
                new BigDecimal("1833.60"), "ETB", Instant.now());
        i.chargeCreated("ch_test1");
        pending = paymentIntents.save(i);
    }

    // ---- Webhook ---------------------------------------------------------------------------

    @Test
    void validWebhookIsStoredAndMovesTheIntentToReceived() throws Exception {
        webhook(body("evt_1", "1833.60", "SUCCEEDED"), Instant.now().getEpochSecond(), props.webhookSecret())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ACCEPTED"));

        PaymentIntent i = paymentIntents.findById(pending.getId()).orElseThrow();
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.RECEIVED);
        assertThat(i.getProviderTransactionId()).isEqualTo("TP123");
        assertThat(i.getNextAttemptAt()).isNotNull(); // the posting worker will pick it up
        assertThat(webhookEvents.findAll()).singleElement()
                .satisfies(e -> assertThat(e.getOutcome()).isEqualTo(WebhookEvent.Outcome.ACCEPTED));
        // Nothing is posted inside the webhook (inbox pattern).
        verify(fineract, never()).repayLoan(anyLong(), any());
        assertThat(auditLog.findAll()).anySatisfy(r -> {
            assertThat(r.getAction()).isEqualTo(AuditAction.WEBHOOK);
            assertThat(r.getOutcome()).isEqualTo(AuditOutcome.SUCCESS);
        });
    }

    @Test
    void repeatedEventIdChangesNothing() throws Exception {
        String body = body("evt_1", "1833.60", "SUCCEEDED");
        long ts = Instant.now().getEpochSecond();
        webhook(body, ts, props.webhookSecret()).andExpect(jsonPath("$.result").value("ACCEPTED"));
        webhook(body, ts, props.webhookSecret()).andExpect(status().isOk()).andExpect(jsonPath("$.result").value("DUPLICATE"));

        assertThat(webhookEvents.count()).isEqualTo(1);
        assertThat(paymentIntents.findById(pending.getId()).orElseThrow().getTransitions()).hasSize(2);
    }

    @Test
    void wrongSignatureIs401AndNothingIsStored() throws Exception {
        webhook(body("evt_2", "1833.60", "SUCCEEDED"), Instant.now().getEpochSecond(), "not-the-secret-0123456789abcdef01234")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("WEBHOOK_SIGNATURE_INVALID"));
        mvc.perform(post("/api/webhooks/mobile-money").contentType(MediaType.APPLICATION_JSON)
                        .content(body("evt_3", "1833.60", "SUCCEEDED")))
                .andExpect(status().isUnauthorized());

        assertThat(webhookEvents.count()).isZero();
        assertThat(paymentIntents.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(auditLog.findAll()).filteredOn(r -> r.getOutcome() == AuditOutcome.DENIED).hasSize(2);
    }

    @Test
    void oldTimestampIs401() throws Exception {
        webhook(body("evt_4", "1833.60", "SUCCEEDED"), Instant.now().minusSeconds(600).getEpochSecond(),
                props.webhookSecret())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("WEBHOOK_TIMESTAMP_INVALID"));
        assertThat(webhookEvents.count()).isZero();
    }

    @Test
    void differentAmountIsStoredAsMismatchAndNotPosted() throws Exception {
        webhook(body("evt_5", "1000.00", "SUCCEEDED"), Instant.now().getEpochSecond(), props.webhookSecret())
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("MISMATCH"));

        PaymentIntent i = paymentIntents.findById(pending.getId()).orElseThrow();
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.MISMATCH);
        assertThat(i.getProviderAmount()).isEqualByComparingTo("1000.00");
        assertThat(i.getStatus().isAwaitingPosting()).isFalse();
        assertThat(webhookEvents.findAll()).singleElement()
                .satisfies(e -> assertThat(e.getOutcome()).isEqualTo(WebhookEvent.Outcome.MISMATCH));
    }

    @Test
    void declinedAndUnknownReference() throws Exception {
        webhook(body("evt_6", "1833.60", "DECLINED"), Instant.now().getEpochSecond(), props.webhookSecret())
                .andExpect(jsonPath("$.result").value("ACCEPTED"));
        assertThat(paymentIntents.findById(pending.getId()).orElseThrow().getStatus()).isEqualTo(PaymentStatus.DECLINED);

        String unknown = body("evt_7", "50.00", "SUCCEEDED").replace("AMP-TEST00000001", "AMP-NOBODY");
        webhook(unknown, Instant.now().getEpochSecond(), props.webhookSecret())
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("UNKNOWN_REFERENCE"));
    }

    // ---- Officer endpoints -----------------------------------------------------------------

    @Test
    void officerRequestsAPaymentAndPollsItWithoutFineract() throws Exception {
        stubLoanWithOutstanding("5000.00", 300);
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));
        when(telePay.createCharge(any())).thenReturn(new TelePayClient.Charge("ch_new", "x", "PENDING"));

        String response = mvc.perform(post("/api/loans/1/mobile-payments").header("Authorization", bearer(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 1833.60, \"phone\": \"0922100001\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.providerChargeId").value("ch_new"))
                .andReturn().getResponse().getContentAsString();
        long id = json.readTree(response).path("id").asLong();

        org.mockito.Mockito.clearInvocations(fineract);
        mvc.perform(get("/api/mobile-payments/" + id).header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.history[0].to").value("PENDING"));
        mvc.perform(get("/api/mobile-payments/" + id).header("Authorization", bearer(TADESSE))).andExpect(status().isOk());
        mvc.perform(get("/api/mobile-payments/" + id).header("Authorization", bearer(HANA))).andExpect(status().isForbidden());
        verify(fineract, never()).getClient(anyLong()); // polling works while Fineract is down
    }

    @Test
    void officerCannotCollectOnAnotherOfficersClient() throws Exception {
        stubLoanWithOutstanding("5000.00", 300);
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));

        request(HANA, "100").andExpect(status().isForbidden());
        verify(telePay, never()).createCharge(any());
    }

    @Test
    void amountAndLoanStateRules() throws Exception {
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));
        stubLoanWithOutstanding("3000.00", 300);
        // 3000 outstanding - 1833.60 already requested (the pending intent) = 1166.40 left.
        request(SAMUEL, "1166.41").andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("AMOUNT_EXCEEDS_OUTSTANDING"));

        stubLoanWithOutstanding("3000.00", 200); // approved, not disbursed
        request(SAMUEL, "100").andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("LOAN_NOT_ACTIVE"));
        verify(telePay, never()).createCharge(any());
    }

    @Test
    void providerDownMarksTheIntentChargeFailed() throws Exception {
        stubLoanWithOutstanding("5000.00", 300);
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));
        when(telePay.createCharge(any())).thenThrow(new ProviderUnavailableException("connection refused"));

        request(SAMUEL, "100").andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PROVIDER_UNAVAILABLE"));
        assertThat(paymentIntents.findAll()).filteredOn(i -> i.getStatus() == PaymentStatus.CHARGE_FAILED).hasSize(1);
    }

    // ---- Helpers ---------------------------------------------------------------------------

    private String body(String eventId, String amount, String status) {
        return ("{\"eventId\":\"%s\",\"chargeId\":\"ch_test1\",\"reference\":\"AMP-TEST00000001\",\"amount\":%s,"
                + "\"currency\":\"ETB\",\"status\":\"%s\",\"providerTransactionId\":\"TP123\","
                + "\"timestamp\":\"2026-10-24T07:00:00Z\"}").formatted(eventId, amount, status);
    }

    private ResultActions webhook(String body, long timestamp, String secret) throws Exception {
        return mvc.perform(post("/api/webhooks/mobile-money").contentType(MediaType.APPLICATION_JSON)
                .header(WebhookSignature.TIMESTAMP_HEADER, String.valueOf(timestamp))
                .header(WebhookSignature.SIGNATURE_HEADER, WebhookSignature.header(WebhookSignature.sign(secret, timestamp, body)))
                .content(body));
    }

    private ResultActions request(et.adey.mfi.security.Officer officer, String amount) throws Exception {
        return mvc.perform(post("/api/loans/1/mobile-payments").header("Authorization", bearer(officer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": %s, \"phone\": \"0922100001\"}".formatted(amount)));
    }

    private void stubLoanWithOutstanding(String outstanding, int statusId) {
        when(fineract.getLoanWithSchedule(1L)).thenReturn(json.readValue("""
                {"id": 1, "accountNo": "000000001", "clientId": 1, "status": {"id": %d, "value": "x"},
                 "summary": {"totalOutstanding": %s}}
                """.formatted(statusId, outstanding), et.adey.mfi.fineract.model.FineractLoan.class));
    }
}
