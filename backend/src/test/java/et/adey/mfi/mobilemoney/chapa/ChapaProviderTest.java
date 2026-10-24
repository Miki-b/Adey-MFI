package et.adey.mfi.mobilemoney.chapa;

import et.adey.mfi.TestJson;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.ChargeRequest;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.ChargeResult;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.Verification;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.VerifiedStatus;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.WebhookCheck;
import et.adey.mfi.mobilemoney.PaymentMethod;
import et.adey.mfi.mobilemoney.ProviderUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SECRET;
import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SUCCESS;
import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SUCCESS_SIGNATURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** ChapaProvider against a fake Chapa (MockRestServiceServer), with payloads Chapa's test mode really sent. */
class ChapaProviderTest {

    private static final String BASE = "https://chapa.test/v1";
    /** Fake test key, built from parts so secret scanners don't flag it. */
    private static final String TEST_KEY = "CHASECK_TEST-" + "unit-test-not-a-real-key";

    private MockRestServiceServer server;
    private ChapaProvider chapa;

    @BeforeEach
    void setUp() {
        ChapaProperties props = props(TEST_KEY, WEBHOOK_SECRET);
        RestClient.Builder builder = ChapaProvider.configure(RestClient.builder(), props);
        server = MockRestServiceServer.bindTo(builder).build();
        chapa = new ChapaProvider(builder.build(), props, TestJson.mapper());
    }

    private static ChapaProperties props(String key, String webhookSecret) {
        return new ChapaProperties(BASE, key, webhookSecret, Duration.ofSeconds(5), Duration.ofSeconds(20));
    }

    // ---- Test-key rule ---------------------------------------------------------------------

    @Test
    void refusesToStartWithoutATestKey() {
        assertThatThrownBy(() -> ChapaProvider.requireTestKey("CHASECK-" + "live-looking-key"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a test key")
                .hasMessageNotContaining("live-looking-key"); // the key never appears in messages
        assertThatThrownBy(() -> ChapaProvider.requireTestKey(""))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("CHAPA_SECRET_KEY");
        assertThatThrownBy(() -> new ChapaProvider(RestClient.builder(), props(null, null), TestJson.mapper()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ---- Charge ----------------------------------------------------------------------------

    @Test
    void telebirrChargeIsAMultipartPostWithTheBearerKey() {
        server.expect(requestTo(BASE + "/charges?type=telebirr"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_KEY))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.MULTIPART_FORM_DATA_VALUE)))
                .andExpect(content().multipartData(multipart("1833.60", "AMP-3F9A0C2B7E41", "0900123456")))
                .andRespond(withSuccess(ChapaSamples.CHARGE_INITIATED, MediaType.APPLICATION_JSON));

        ChargeResult r = chapa.createCharge(new ChargeRequest("AMP-3F9A0C2B7E41", "0900123456",
                new BigDecimal("1833.60"), "ETB", PaymentMethod.TELEBIRR));

        assertThat(r.accepted()).isTrue();
        assertThat(r.providerChargeId()).isEqualTo("CHbXq2Mvg8E9M");
        server.verify();
    }

    @Test
    void mpesaUsesTypeMpesa() {
        server.expect(requestTo(BASE + "/charges?type=mpesa"))
                .andRespond(withSuccess(ChapaSamples.CHARGE_INITIATED, MediaType.APPLICATION_JSON));
        assertThat(chapa.createCharge(new ChargeRequest("AMP-1", "0700123456", BigDecimal.TEN, "ETB",
                PaymentMethod.MPESA)).accepted()).isTrue();
        server.verify();
    }

    @Test
    void failedInitiationWithHttp200IsNotAccepted() {
        server.expect(requestTo(BASE + "/charges?type=telebirr"))
                .andRespond(withSuccess(ChapaSamples.CHARGE_FAILED, MediaType.APPLICATION_JSON));

        ChargeResult r = chapa.createCharge(new ChargeRequest("AMP-2", "0911000000", BigDecimal.TEN, "ETB",
                PaymentMethod.TELEBIRR));

        assertThat(r.accepted()).isFalse();
        assertThat(r.providerChargeId()).isNull();
        assertThat(r.message()).startsWith("Invalid Test Number");
    }

    @Test
    void validationErrorWithAnObjectMessageIsNotAccepted() {
        server.expect(requestTo(BASE + "/charges?type=telebirr"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":{\"amount\":[\"The amount field is required.\"]},\"status\":\"failed\",\"data\":null}"));

        ChargeResult r = chapa.createCharge(new ChargeRequest("AMP-3", "0900123456", BigDecimal.TEN, "ETB", null));

        assertThat(r.accepted()).isFalse();
        assertThat(r.message()).contains("amount field is required");
    }

    @Test
    void serverErrorsAndNetworkProblemsAreProviderUnavailable() {
        server.expect(requestTo(BASE + "/charges?type=telebirr")).andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        server.expect(requestTo(BASE + "/transaction/verify/AMP-4")).andRespond(withException(new IOException("reset")));

        assertThatThrownBy(() -> chapa.createCharge(new ChargeRequest("AMP-4", "0900123456", BigDecimal.TEN, "ETB",
                PaymentMethod.TELEBIRR))).isInstanceOf(ProviderUnavailableException.class).hasMessageContaining("502");
        assertThatThrownBy(() -> chapa.verifyCharge("AMP-4")).isInstanceOf(ProviderUnavailableException.class);
    }

    // ---- Verify ----------------------------------------------------------------------------

    @Test
    void verifiedSuccessCarriesChapaReferenceAmountAndTime() {
        server.expect(requestTo(BASE + "/transaction/verify/AMP-5"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + TEST_KEY))
                .andRespond(withSuccess(ChapaSamples.verify("AMP-5", "success", "1833.6"), MediaType.APPLICATION_JSON));

        Verification v = chapa.verifyCharge("AMP-5");

        assertThat(v.status()).isEqualTo(VerifiedStatus.SUCCEEDED);
        assertThat(v.reference()).isEqualTo("AMP-5");
        assertThat(v.providerReference()).isEqualTo("CHbXq2Mvg8E9M");
        assertThat(v.amount()).isEqualByComparingTo("1833.60"); // exact BigDecimal, no double rounding
        assertThat(v.currency()).isEqualTo("ETB");
        assertThat(v.paidAt()).isEqualTo(Instant.parse("2026-10-24T12:25:49Z"));
        assertThat(v.summary()).contains("251900****56").doesNotContain("251900123456"); // phone masked
    }

    @Test
    void verifiedFailedCancelledAndUnknownAndNotFound() {
        server.expect(requestTo(BASE + "/transaction/verify/AMP-6"))
                .andRespond(withSuccess(ChapaSamples.verify("AMP-6", "failed/cancelled", "10"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/transaction/verify/AMP-7"))
                .andRespond(withSuccess(ChapaSamples.verify("AMP-7", "reversed", "10"), MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/transaction/verify/AMP-8"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body(ChapaSamples.VERIFY_NOT_FOUND));

        assertThat(chapa.verifyCharge("AMP-6").status()).isEqualTo(VerifiedStatus.DECLINED);
        Verification unknown = chapa.verifyCharge("AMP-7");
        assertThat(unknown.status()).isEqualTo(VerifiedStatus.UNKNOWN);
        assertThat(unknown.rawStatus()).isEqualTo("reversed");
        assertThat(chapa.verifyCharge("AMP-8").status()).isEqualTo(VerifiedStatus.NOT_FOUND);
        server.verify();
    }

    // ---- Webhook ---------------------------------------------------------------------------

    @Test
    void signedWebhookIsOnlyAHintWithTheTxRef() {
        WebhookCheck check = chapa.parseAndVerifyWebhook(signed(WEBHOOK_SUCCESS_SIGNATURE), WEBHOOK_SUCCESS, Instant.now());

        assertThat(check).isInstanceOfSatisfying(WebhookCheck.Hint.class, h -> {
            assertThat(h.reference()).isEqualTo("AMP-3F9A0C2B7E41");
            assertThat(h.event()).isEqualTo("charge.success");
        });
        assertThat(chapa.needsVerification()).isTrue();
        assertThat(chapa.listSettlements(java.time.LocalDate.now())).isEmpty();
    }

    @Test
    void tamperedOrUnsignedWebhookIs401() {
        String tampered = WEBHOOK_SUCCESS.replace("400.00", "4000.00");
        assertThat(chapa.parseAndVerifyWebhook(signed(WEBHOOK_SUCCESS_SIGNATURE), tampered, Instant.now()))
                .isInstanceOfSatisfying(WebhookCheck.Rejected.class, r -> {
                    assertThat(r.httpStatus()).isEqualTo(401);
                    assertThat(r.code()).isEqualTo("WEBHOOK_SIGNATURE_INVALID");
                });
        assertThat(chapa.parseAndVerifyWebhook(new HttpHeaders(), WEBHOOK_SUCCESS, Instant.now()))
                .isInstanceOf(WebhookCheck.Rejected.class);

        ChapaProvider noSecret = new ChapaProvider(RestClient.create(), props(TEST_KEY, ""), TestJson.mapper());
        assertThat(noSecret.parseAndVerifyWebhook(signed(WEBHOOK_SUCCESS_SIGNATURE), WEBHOOK_SUCCESS, Instant.now()))
                .isInstanceOfSatisfying(WebhookCheck.Rejected.class, r -> assertThat(r.httpStatus()).isEqualTo(401));
    }

    @Test
    void signedWebhookWithoutTxRefIs400() {
        String body = "{\"event\":\"charge.success\"}";
        assertThat(chapa.parseAndVerifyWebhook(signed(ChapaWebhookSignature.sign(WEBHOOK_SECRET, body)), body, Instant.now()))
                .isInstanceOfSatisfying(WebhookCheck.Rejected.class, r -> assertThat(r.httpStatus()).isEqualTo(400));
    }

    private static HttpHeaders signed(String signature) {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Chapa-Signature", signature); // header names are case-insensitive
        return h;
    }

    private static org.springframework.util.MultiValueMap<String, String> multipart(String amount, String txRef,
                                                                                     String mobile) {
        var m = new org.springframework.util.LinkedMultiValueMap<String, String>();
        m.add("amount", amount);
        m.add("currency", "ETB");
        m.add("tx_ref", txRef);
        m.add("mobile", mobile);
        return m;
    }
}
