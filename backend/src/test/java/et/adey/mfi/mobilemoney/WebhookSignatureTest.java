package et.adey.mfi.mobilemoney;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static et.adey.mfi.mobilemoney.WebhookSignature.Result.BAD_SIGNATURE;
import static et.adey.mfi.mobilemoney.WebhookSignature.Result.MISSING;
import static et.adey.mfi.mobilemoney.WebhookSignature.Result.STALE_TIMESTAMP;
import static et.adey.mfi.mobilemoney.WebhookSignature.Result.VALID;
import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureTest {

    private static final String SECRET = "test-webhook-secret-0123456789abcdef0123";
    private static final String BODY = "{\"eventId\":\"evt_1\",\"amount\":1833.60}";
    private static final Instant NOW = Instant.parse("2026-10-24T08:00:00Z");
    private static final Duration FIVE_MIN = Duration.ofMinutes(5);
    private static final long TS = NOW.getEpochSecond();

    private static WebhookSignature.Result verify(String secretUsedBySender, long ts, String signedBody, String sentBody) {
        String header = WebhookSignature.header(WebhookSignature.sign(secretUsedBySender, ts, signedBody));
        return WebhookSignature.verify(SECRET, String.valueOf(ts), header, sentBody, NOW, FIVE_MIN);
    }

    @Test
    void validSignature() {
        assertThat(verify(SECRET, TS, BODY, BODY)).isEqualTo(VALID);
    }

    @Test
    void wrongSecretIsRejected() {
        assertThat(verify("another-secret-0123456789abcdef0123456789", TS, BODY, BODY)).isEqualTo(BAD_SIGNATURE);
    }

    @Test
    void tamperedBodyIsRejected() {
        assertThat(verify(SECRET, TS, BODY, BODY.replace("1833.60", "18336.00"))).isEqualTo(BAD_SIGNATURE);
        // Even whitespace counts: the signature covers the exact bytes.
        assertThat(verify(SECRET, TS, BODY, BODY.replace(",", ", "))).isEqualTo(BAD_SIGNATURE);
    }

    @Test
    void timestampOutsideFiveMinutesIsRejectedButBoundaryIsAccepted() {
        assertThat(verify(SECRET, TS - 301, BODY, BODY)).isEqualTo(STALE_TIMESTAMP);
        assertThat(verify(SECRET, TS + 301, BODY, BODY)).isEqualTo(STALE_TIMESTAMP); // clock far ahead
        assertThat(verify(SECRET, TS - 300, BODY, BODY)).isEqualTo(VALID);
    }

    @Test
    void changingTheTimestampHeaderBreaksTheSignature() {
        // Attacker replays an old webhook with a fresh timestamp: the signature was over the old one.
        String header = WebhookSignature.header(WebhookSignature.sign(SECRET, TS - 3600, BODY));
        assertThat(WebhookSignature.verify(SECRET, String.valueOf(TS), header, BODY, NOW, FIVE_MIN))
                .isEqualTo(BAD_SIGNATURE);
    }

    @Test
    void missingOrGarbledHeaders() {
        String good = WebhookSignature.header(WebhookSignature.sign(SECRET, TS, BODY));
        assertThat(WebhookSignature.verify(SECRET, null, good, BODY, NOW, FIVE_MIN)).isEqualTo(MISSING);
        assertThat(WebhookSignature.verify(SECRET, String.valueOf(TS), null, BODY, NOW, FIVE_MIN)).isEqualTo(MISSING);
        assertThat(WebhookSignature.verify(SECRET, "yesterday", good, BODY, NOW, FIVE_MIN)).isEqualTo(MISSING);
        assertThat(WebhookSignature.verify(SECRET, String.valueOf(TS), good.substring(3), BODY, NOW, FIVE_MIN))
                .isEqualTo(MISSING); // no "v1=" prefix
    }
}
