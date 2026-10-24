package et.adey.mfi.simulator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * "TelePay" mobile money provider, SIMULATED. In reality this is someone else's system on the
 * internet. To save memory it runs inside our app (local profile only), but it is kept apart: its
 * own in-memory state (no access to our tables or services), its own HTTP client and its own
 * signing code. It reaches our backend only over real HTTP, by calling the callback URL we gave
 * it, exactly like the real provider would.
 */
@Slf4j
@Component
@Profile("local")
public class TelePaySimulator {

    private static final ZoneId PROVIDER_ZONE = ZoneId.of("Africa/Addis_Ababa");

    private final Map<String, Charge> charges = new ConcurrentHashMap<>();
    /** Provider transaction ids must never repeat, not even after a restart: time-based + counter. */
    private final AtomicLong txnSequence = new AtomicLong();
    private static final java.time.format.DateTimeFormatter TXN_TIME =
            java.time.format.DateTimeFormatter.ofPattern("yyMMddHHmmss").withZone(PROVIDER_ZONE);
    private final RestClient http;
    private final ObjectMapper json;
    private final String webhookSecret;
    private final String apiKey;

    public TelePaySimulator(@Value("${simulator.webhook-secret}") String webhookSecret,
                            @Value("${simulator.api-key}") String apiKey, ObjectMapper json) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder().requestFactory(factory).build(); // NOT our Fineract/TelePay clients
        this.json = json;
        this.webhookSecret = webhookSecret;
        this.apiKey = apiKey;
    }

    // ---- Provider state -------------------------------------------------------------------

    public static final class Charge {
        final String chargeId;
        final String reference;
        final String phone;
        final BigDecimal amount;
        final String currency;
        final String callbackUrl;
        final Instant createdAt;
        volatile String status = "PENDING";
        volatile String providerTransactionId;
        volatile BigDecimal settledAmount;
        volatile Instant settledAt;
        final List<Map<String, Object>> deliveries = Collections.synchronizedList(new ArrayList<>());

        Charge(String chargeId, String reference, String phone, BigDecimal amount, String currency,
               String callbackUrl, Instant createdAt) {
            this.chargeId = chargeId;
            this.reference = reference;
            this.phone = phone;
            this.amount = amount;
            this.currency = currency;
            this.callbackUrl = callbackUrl;
            this.createdAt = createdAt;
        }

        Map<String, Object> view() {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("chargeId", chargeId);
            m.put("reference", reference);
            m.put("phone", phone);
            m.put("amount", amount);
            m.put("currency", currency);
            m.put("status", status);
            m.put("providerTransactionId", providerTransactionId);
            m.put("settledAmount", settledAmount);
            m.put("settledAt", settledAt);
            m.put("callbackUrl", callbackUrl);
            m.put("createdAt", createdAt);
            m.put("webhookDeliveries", List.copyOf(deliveries));
            return m;
        }
    }

    /** Chaos switches for one approval/decline. */
    public record Chaos(boolean duplicate, int delaySeconds, boolean wrongSecret, boolean drop,
                        BigDecimal reportAmount, int staleSeconds) {
    }

    boolean apiKeyValid(String authorization) {
        return authorization != null && authorization.equals("Bearer " + apiKey);
    }

    Charge create(String reference, String phone, BigDecimal amount, String currency, String callbackUrl) {
        String id = "ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        Charge c = new Charge(id, reference, phone, amount, currency, callbackUrl, Instant.now());
        charges.put(id, c);
        log.info("[TelePay sim] charge {} created for {} {} ({}), waiting for the customer", id, amount, currency, reference);
        return c;
    }

    Charge find(String chargeId) {
        return charges.get(chargeId);
    }

    List<Charge> findByReference(String reference) {
        return charges.values().stream().filter(c -> c.reference.equals(reference)).toList();
    }

    /** The customer pressed "Pay" on their phone. */
    Map<String, Object> approve(Charge c, Chaos chaos) {
        c.status = "SUCCEEDED";
        c.providerTransactionId = "TP" + TXN_TIME.format(Instant.now()) + String.format("%03d", txnSequence.incrementAndGet() % 1000);
        // "reportAmount": the provider's books (webhook AND settlement) show a different amount.
        c.settledAmount = chaos.reportAmount() != null ? chaos.reportAmount() : c.amount;
        c.settledAt = Instant.now();
        return scheduleWebhook(c, chaos);
    }

    /** The customer pressed "Decline". */
    Map<String, Object> decline(Charge c, Chaos chaos) {
        c.status = "DECLINED";
        return scheduleWebhook(c, chaos);
    }

    List<Map<String, Object>> settlements(LocalDate date) {
        return charges.values().stream()
                .filter(c -> "SUCCEEDED".equals(c.status) && c.settledAt != null
                        && c.settledAt.atZone(PROVIDER_ZONE).toLocalDate().equals(date))
                .sorted((a, b) -> a.settledAt.compareTo(b.settledAt))
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("chargeId", c.chargeId);
                    m.put("reference", c.reference);
                    m.put("providerTransactionId", c.providerTransactionId);
                    m.put("amount", c.settledAmount);
                    m.put("currency", c.currency);
                    m.put("settledAt", c.settledAt);
                    return m;
                })
                .toList();
    }

    // ---- Webhooks --------------------------------------------------------------------------

    private Map<String, Object> scheduleWebhook(Charge c, Chaos chaos) {
        Map<String, Object> plan = new LinkedHashMap<>();
        if (chaos.drop()) {
            plan.put("webhook", "NOT sent (drop=true)");
            c.deliveries.add(Map.of("at", Instant.now().toString(), "result", "dropped by chaos switch"));
            return plan;
        }
        String eventId = "evt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("eventId", eventId);
        body.put("chargeId", c.chargeId);
        body.put("reference", c.reference);
        body.put("amount", "SUCCEEDED".equals(c.status) ? c.settledAmount : c.amount);
        body.put("currency", c.currency);
        body.put("status", c.status);
        body.put("providerTransactionId", c.providerTransactionId);
        body.put("timestamp", Instant.now().toString());
        String raw = json.writeValueAsString(body);

        int times = chaos.duplicate() ? 2 : 1;
        plan.put("webhook", "eventId " + eventId + ", " + times + "x, delay " + chaos.delaySeconds() + "s"
                + (chaos.wrongSecret() ? ", WRONG secret" : "") + (chaos.staleSeconds() > 0
                ? ", timestamp " + chaos.staleSeconds() + "s old" : ""));
        // Real providers call back asynchronously, after answering the customer's phone.
        Thread.ofVirtual().name("telepay-sim-webhook-" + c.chargeId).start(() -> {
            sleep(Duration.ofSeconds(chaos.delaySeconds()));
            for (int i = 1; i <= times; i++) {
                deliver(c, eventId, raw, chaos);
            }
        });
        return plan;
    }

    private void deliver(Charge c, String eventId, String raw, Chaos chaos) {
        long timestamp = Instant.now().getEpochSecond() - chaos.staleSeconds();
        String secret = chaos.wrongSecret() ? "not-the-shared-secret-at-all-0123456789" : webhookSecret;
        String signature = "v1=" + hmacHex(secret, timestamp + "." + raw);
        Map<String, Object> delivery = new LinkedHashMap<>();
        delivery.put("at", Instant.now().toString());
        delivery.put("eventId", eventId);
        try {
            String response = http.post().uri(c.callbackUrl)
                    .header("Content-Type", "application/json")
                    .header("X-TelePay-Timestamp", String.valueOf(timestamp))
                    .header("X-TelePay-Signature", signature)
                    .body(raw)
                    .retrieve()
                    .body(String.class);
            delivery.put("httpStatus", 200);
            delivery.put("response", response);
        } catch (RestClientResponseException e) {
            delivery.put("httpStatus", e.getStatusCode().value());
            delivery.put("response", e.getResponseBodyAsString());
        } catch (RuntimeException e) {
            delivery.put("error", e.getMessage());
        }
        c.deliveries.add(delivery);
        log.info("[TelePay sim] webhook {} for {} -> {}", eventId, c.chargeId, delivery.get("httpStatus"));
    }

    /** The simulator's OWN signing code (a real provider does not share code with us). */
    private static String hmacHex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void sleep(Duration d) {
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
