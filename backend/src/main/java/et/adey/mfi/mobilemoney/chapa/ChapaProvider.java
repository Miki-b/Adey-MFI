package et.adey.mfi.mobilemoney.chapa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import et.adey.mfi.common.Phones;
import et.adey.mfi.mobilemoney.MobileMoneyProvider;
import et.adey.mfi.mobilemoney.PaymentMethod;
import et.adey.mfi.mobilemoney.ProviderUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ADAPTER for Chapa (app.mobile-money.provider=chapa), TEST MODE ONLY.
 *
 * Translates our MobileMoneyProvider interface into Chapa's API:
 *   createCharge  -> POST /charges?type=telebirr|mpesa   (multipart form: amount, currency, tx_ref, mobile)
 *   verifyCharge  -> GET  /transaction/verify/{tx_ref}
 *   webhooks      -> x-chapa-signature = HMAC-SHA256(secret hash, body); only a HINT (needsVerification)
 *   settlements   -> Chapa has no listing for direct charges: empty, reconciliation verifies our intents
 * Our payment reference is Chapa's tx_ref (unique per attempt). Chapa's own id ("reference",
 * e.g. CHbXq2Mvg8E9M) becomes the receipt number in Fineract.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mobile-money", name = "provider", havingValue = "chapa")
public class ChapaProvider implements MobileMoneyProvider {

    static final String TEST_KEY_PREFIX = "CHASECK_TEST-";

    private final RestClient rest;
    private final ChapaProperties props;
    private final ObjectMapper json;

    @Autowired // two constructors: tell Spring which one to use
    public ChapaProvider(RestClient.Builder builder, ChapaProperties props, ObjectMapper json) {
        this(buildClient(builder, props), props, json);
    }

    /** For tests: a RestClient bound to MockRestServiceServer. */
    ChapaProvider(RestClient rest, ChapaProperties props, ObjectMapper json) {
        requireTestKey(props.secretKey());
        this.rest = rest;
        this.props = props;
        this.json = json;
        if (!StringUtils.hasText(props.webhookSecret())) {
            log.warn("CHAPA_WEBHOOK_SECRET is not set: every Chapa webhook will be refused (401). "
                    + "Payments still complete through verify polling.");
        }
        log.info("Mobile money provider: chapa (TEST mode, {})", props.baseUrl());
    }

    /**
     * Safety rule of this project: only Chapa TEST keys. A live key moves real money, and nothing
     * here has been reviewed for that. The key itself is never logged or put in a message.
     */
    static void requireTestKey(String secretKey) {
        if (!StringUtils.hasText(secretKey)) {
            throw new IllegalStateException("app.mobile-money.provider=chapa needs CHAPA_SECRET_KEY (app.chapa.secret-key)");
        }
        if (!secretKey.startsWith(TEST_KEY_PREFIX)) {
            throw new IllegalStateException("Refusing to start the Chapa provider: CHAPA_SECRET_KEY is not a test key "
                    + "(expected prefix " + TEST_KEY_PREFIX + "). Live keys are not allowed in this project.");
        }
    }

    private static RestClient buildClient(RestClient.Builder builder, ChapaProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeout());
        factory.setReadTimeout(props.readTimeout());
        return configure(builder.clone().requestFactory(factory), props).build();
    }

    /** Base URL + "Authorization: Bearer <secret key>" on every call (tests reuse this). */
    static RestClient.Builder configure(RestClient.Builder builder, ChapaProperties props) {
        requireTestKey(props.secretKey());
        return builder.baseUrl(props.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.secretKey());
    }

    @Override
    public String name() {
        return "chapa";
    }

    // ---- Charge ----------------------------------------------------------------------------

    @Override
    public ChargeResult createCharge(ChargeRequest request) {
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("amount", request.amount().toPlainString());
        form.add("currency", request.currency());
        form.add("tx_ref", request.reference());
        form.add("mobile", request.phone());
        String type = chapaType(request.method());

        HttpResult answer = call("charge " + request.reference(), () -> rest.post()
                .uri("/charges?type={type}", type)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(form)
                .exchange((req, res) -> new HttpResult(res.getStatusCode().value(),
                        StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8))));
        ChargeResult result = toChargeResult(answer);
        log.info("Chapa {} charge {} ({} {}) to {}: {}", type, request.reference(), request.amount(),
                request.currency(), Phones.mask(request.phone()),
                result.accepted() ? "initiated, Chapa ref " + result.providerChargeId() : "NOT initiated: " + result.message());
        return result;
    }

    static String chapaType(PaymentMethod method) {
        return switch (method == null ? PaymentMethod.TELEBIRR : method) {
            case TELEBIRR -> "telebirr";
            case MPESA -> "mpesa";
        };
    }

    /**
     * Seen live (test mode):
     *   {"message":"Charge initiated","status":"success","data":{"auth_type":"ussd","payment_status":"PENDING",
     *    "meta":{"ref_id":"CHbXq2Mvg8E9M","status":"success",...}}}
     *   {"message":"Charge failed to initiate","status":"failed","data":{"meta":{"message":"Invalid Test Number..."}}}
     * Note "failed" can come with HTTP 200, so the body decides, not the HTTP status.
     */
    ChargeResult toChargeResult(HttpResult answer) {
        ChargeResponse body = parse(answer.body(), ChargeResponse.class);
        Meta meta = body == null || body.data() == null ? null : body.data().meta();
        String refId = meta == null ? null : meta.refId();
        if (answer.status() < 300 && body != null && "success".equalsIgnoreCase(body.status())) {
            return ChargeResult.accepted(refId);
        }
        String why = meta != null && meta.message() != null ? meta.message()
                : body != null && body.message() != null ? String.valueOf(body.message())
                : "HTTP " + answer.status();
        return ChargeResult.notAccepted(refId, why);
    }

    // ---- Verify ----------------------------------------------------------------------------

    @Override
    public Verification verifyCharge(String reference) {
        HttpResult answer = call("verify " + reference, () -> rest.get()
                .uri("/transaction/verify/{txRef}", reference)
                .exchange((req, res) -> new HttpResult(res.getStatusCode().value(),
                        StreamUtils.copyToString(res.getBody(), StandardCharsets.UTF_8))));
        return toVerification(reference, answer);
    }

    /**
     * Seen live (test mode):
     *   {"message":"Payment details fetched successfully","status":"success","data":{"amount":10,"charge":0.25,
     *    "currency":"ETB","mode":"test","method":"telebirr","status":"success","reference":"CHbXq2Mvg8E9M",
     *    "tx_ref":"AMP-...","phone_number":"2519...","created_at":"2026-10-24T12:25:47.000000Z","updated_at":...}}
     *   unknown tx_ref: {"message":"Invalid transaction reference","status":"failed","data":null}
     * Top-level "status" only says whether the LOOKUP worked; the payment's status is data.status.
     */
    Verification toVerification(String reference, HttpResult answer) {
        VerifyResponse body = parse(answer.body(), VerifyResponse.class);
        VerifyData d = body == null ? null : body.data();
        if (d == null) {
            if (answer.status() >= 500) {
                throw new ProviderUnavailableException("Chapa verify answered HTTP " + answer.status());
            }
            String why = body != null && body.message() != null ? String.valueOf(body.message()) : "HTTP " + answer.status();
            return new Verification(VerifiedStatus.NOT_FOUND, null, reference, null, null, null, null,
                    summary(Map.of("lookup", why)));
        }
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("status", d.status());
        s.put("amount", d.amount());
        s.put("currency", d.currency());
        s.put("reference", d.reference());
        s.put("tx_ref", d.txRef());
        s.put("method", d.method());
        s.put("mode", d.mode());
        s.put("phone", Phones.mask(d.phoneNumber()));
        s.put("updated_at", d.updatedAt() == null ? null : d.updatedAt().toString());
        return new Verification(ChapaStatus.fromVerify(d.status()), d.status(), d.txRef(), d.reference(), d.amount(),
                d.currency(), d.updatedAt() != null ? d.updatedAt() : d.createdAt(), summary(s));
    }

    // ---- Webhook ---------------------------------------------------------------------------

    @Override
    public WebhookCheck parseAndVerifyWebhook(HttpHeaders headers, String rawBody, Instant now) {
        if (!StringUtils.hasText(props.webhookSecret())) {
            return new WebhookCheck.Rejected(401, "WEBHOOK_SIGNATURE_INVALID", "Webhook signature missing or invalid.",
                    "chapa: no webhook secret configured (CHAPA_WEBHOOK_SECRET)");
        }
        String signature = headers.getFirst(ChapaWebhookSignature.HEADER);
        if (!ChapaWebhookSignature.matches(props.webhookSecret(), signature, rawBody)) {
            return new WebhookCheck.Rejected(401, "WEBHOOK_SIGNATURE_INVALID", "Webhook signature missing or invalid.",
                    "chapa: " + ChapaWebhookSignature.HEADER + (signature == null ? " missing" : " does not match the body"));
        }
        ChapaWebhook w;
        try {
            w = json.readValue(rawBody, ChapaWebhook.class);
        } catch (JacksonException e) {
            return new WebhookCheck.Rejected(400, "MALFORMED_REQUEST", "Webhook body is not valid JSON.",
                    "chapa: signed but unreadable body");
        }
        if (w == null || !StringUtils.hasText(w.txRef())) {
            return new WebhookCheck.Rejected(400, "VALIDATION_FAILED", "Webhook has no tx_ref.", "chapa: signed but no tx_ref");
        }
        // Only the reference is used; what the webhook says about status/amount is re-read from verify.
        return new WebhookCheck.Hint(w.txRef(), w.event() != null ? w.event() : w.status());
    }

    @Override
    public Optional<List<Settlement>> listSettlements(LocalDate date) {
        return Optional.empty(); // no settlement listing for direct charges in Chapa's API
    }

    @Override
    public boolean needsVerification() {
        return true;
    }

    // ---- Plumbing --------------------------------------------------------------------------

    record HttpResult(int status, String body) {
    }

    private interface Call {
        HttpResult run();
    }

    /** Network problems and 5xx: Chapa unavailable (we don't know what happened, so we verify later). */
    private HttpResult call(String what, Call call) {
        HttpResult result;
        try {
            result = call.run();
        } catch (RestClientException e) {
            log.warn("Chapa {} failed: {}", what, e.getMessage());
            throw new ProviderUnavailableException("Chapa not reachable: " + e.getMessage());
        }
        if (result.status() >= 500 || result.status() == 401 || result.status() == 403) {
            log.warn("Chapa {} answered HTTP {}", what, result.status());
            throw new ProviderUnavailableException("Chapa answered HTTP " + result.status());
        }
        return result;
    }

    private <T> T parse(String body, Class<T> type) {
        if (!StringUtils.hasText(body)) {
            return null;
        }
        try {
            return json.readValue(body, type);
        } catch (JacksonException e) {
            log.warn("Chapa answer is not the JSON we expect ({}): {}", type.getSimpleName(), e.getOriginalMessage());
            return null;
        }
    }

    private String summary(Map<String, ?> values) {
        return json.writeValueAsString(values);
    }

    // ---- Chapa's JSON (only the fields we use) ---------------------------------------------

    /** "message" is a string, or an object of field errors on validation failures, hence Object. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChargeResponse(Object message, String status, ChargeData data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChargeData(@JsonProperty("payment_status") String paymentStatus, Meta meta) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Meta(String message, @JsonProperty("ref_id") String refId, String status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VerifyResponse(Object message, String status, VerifyData data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VerifyData(BigDecimal amount, String currency, String status, String reference,
                      @JsonProperty("tx_ref") String txRef, String method, String mode,
                      @JsonProperty("phone_number") String phoneNumber,
                      @JsonProperty("created_at") Instant createdAt,
                      @JsonProperty("updated_at") Instant updatedAt) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChapaWebhook(String event, String status, @JsonProperty("tx_ref") String txRef, String reference) {
    }
}
