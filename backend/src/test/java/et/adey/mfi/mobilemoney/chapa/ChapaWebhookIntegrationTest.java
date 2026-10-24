package et.adey.mfi.mobilemoney.chapa;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.mobilemoney.MobileMoneyProvider;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.ChargeResult;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.Verification;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.VerifiedStatus;
import et.adey.mfi.mobilemoney.PaymentIntent;
import et.adey.mfi.mobilemoney.PaymentIntentTestAccess;
import et.adey.mfi.mobilemoney.PaymentMethod;
import et.adey.mfi.mobilemoney.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Instant;

import static et.adey.mfi.TestOfficers.SAMUEL;
import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SECRET;
import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SUCCESS;
import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SUCCESS_SIGNATURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The whole app with provider=chapa: the right adapter is wired, the webhook endpoint uses Chapa's
 * signature, and an authentic webhook still only counts once Chapa's verify API confirms it.
 * (ChapaProvider is real but spied: its HTTP calls to Chapa are replaced in each test.)
 */
@TestPropertySource(properties = {
        "app.mobile-money.provider=chapa",
        "app.chapa.base-url=http://chapa.invalid/v1",
        "app.chapa.secret-key=CHASECK_TEST-integration-test-not-real",
        "app.chapa.webhook-secret=" + WEBHOOK_SECRET})
class ChapaWebhookIntegrationTest extends ApiTestBase {

    private static final String REF = "AMP-3F9A0C2B7E41"; // the tx_ref in the sample webhook

    @MockitoSpyBean
    private ChapaProvider chapa;
    @Autowired
    private MobileMoneyProvider provider;

    @Test
    void theChapaAdapterIsTheOnlyProvider() {
        assertThat(provider.name()).isEqualTo("chapa");
        assertThat(provider).isSameAs(chapa);
    }

    @Test
    void authenticWebhookIsVerifiedThenAcceptedOnceAndTamperedIs401() throws Exception {
        PaymentIntent intent = pendingIntent("400.00");
        doReturn(new Verification(VerifiedStatus.SUCCEEDED, "success", REF, "CHbXq2Mvg8E9M", new BigDecimal("400"),
                "ETB", Instant.parse("2026-10-24T08:30:05Z"), "{}")).when(chapa).verifyCharge(REF);

        webhook(WEBHOOK_SUCCESS, WEBHOOK_SUCCESS_SIGNATURE)
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("RECEIVED"));
        // Chapa re-delivers (it retries until 200, or just twice): nothing happens again.
        webhook(WEBHOOK_SUCCESS, WEBHOOK_SUCCESS_SIGNATURE)
                .andExpect(status().isOk()).andExpect(jsonPath("$.result").value("IGNORED"));
        // Same signature on a changed body: refused before anything is looked up.
        webhook(WEBHOOK_SUCCESS.replace("400.00", "4000.00"), WEBHOOK_SUCCESS_SIGNATURE)
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("WEBHOOK_SIGNATURE_INVALID"));
        // TelePay-style headers mean nothing to the Chapa adapter.
        mvc.perform(post("/api/webhooks/mobile-money").contentType(MediaType.APPLICATION_JSON).content(WEBHOOK_SUCCESS))
                .andExpect(status().isUnauthorized());

        PaymentIntent i = paymentIntents.findById(intent.getId()).orElseThrow();
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.RECEIVED);
        assertThat(i.getProviderTransactionId()).isEqualTo("CHbXq2Mvg8E9M");
        assertThat(webhookEvents.findAll()).singleElement()
                .satisfies(e -> assertThat(e.getRawBody()).isEqualTo(WEBHOOK_SUCCESS));
        verify(fineract, never()).repayLoan(org.mockito.ArgumentMatchers.anyLong(), any());
    }

    @Test
    void officerRequestsAnMpesaPaymentThroughChapa() throws Exception {
        stubActiveLoan();
        doReturn(ChargeResult.accepted("CH4kxstLNY0px")).when(chapa).createCharge(any());

        mvc.perform(post("/api/loans/1/mobile-payments").header("Authorization", bearer(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100, \"phone\": \"0700123456\", \"method\": \"MPESA\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.provider").value("chapa"))
                .andExpect(jsonPath("$.method").value("MPESA"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.providerChargeId").value("CH4kxstLNY0px"));

        PaymentIntent saved = paymentIntents.findAll().getFirst();
        assertThat(saved.getNextVerifyAt()).isNotNull(); // the verify poller will follow it up
        verify(chapa).createCharge(org.mockito.ArgumentMatchers.argThat(r -> r.method() == PaymentMethod.MPESA));
    }

    @Test
    void methodMustFitThePhoneNetwork() throws Exception {
        stubActiveLoan();
        mvc.perform(post("/api/loans/1/mobile-payments").header("Authorization", bearer(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100, \"phone\": \"0900123456\", \"method\": \"MPESA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PHONE_METHOD_MISMATCH"));
        verify(chapa, never()).createCharge(any());
    }

    @Test
    void chapaDownDuringTheChargeKeepsItPendingForVerify() throws Exception {
        stubActiveLoan();
        // chapa.invalid does not resolve: the real HTTP call fails like a network outage.
        mvc.perform(post("/api/loans/1/mobile-payments").header("Authorization", bearer(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 100, \"phone\": \"0900123456\", \"method\": \"TELEBIRR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.lastError").value(org.hamcrest.Matchers.containsString("Chapa not reachable")));
        verify(chapa, never()).verifyCharge(anyString()); // later, by the poller
    }

    private void stubActiveLoan() {
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));
        when(fineract.getLoanWithSchedule(1L)).thenReturn(json.readValue("""
                {"id": 1, "accountNo": "000000001", "clientId": 1, "status": {"id": 300, "value": "Active"},
                 "summary": {"totalOutstanding": 5000.00}}
                """, et.adey.mfi.fineract.model.FineractLoan.class));
    }

    private PaymentIntent pendingIntent(String amount) {
        PaymentIntent i = new PaymentIntent(REF, 1, 1, 2, 1L, "samuel", "0900123456", new BigDecimal(amount), "ETB",
                "chapa", PaymentMethod.TELEBIRR, Instant.now());
        PaymentIntentTestAccess.chargeCreated(i, "CHbXq2Mvg8E9M");
        return paymentIntents.save(i);
    }

    private ResultActions webhook(String body, String signature) throws Exception {
        return mvc.perform(post("/api/webhooks/mobile-money").contentType(MediaType.APPLICATION_JSON)
                .header("x-chapa-signature", signature)
                .header("chapa-signature", ChapaSamples.CONSTANT_SIGNATURE)
                .content(body));
    }
}
