package et.adey.mfi.mobilemoney.chapa;

import org.junit.jupiter.api.Test;

import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.CONSTANT_SIGNATURE;
import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SECRET;
import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SUCCESS;
import static et.adey.mfi.mobilemoney.chapa.ChapaSamples.WEBHOOK_SUCCESS_SIGNATURE;
import static org.assertj.core.api.Assertions.assertThat;

class ChapaWebhookSignatureTest {

    @Test
    void matchesAnIndependentlyComputedHmac() {
        assertThat(ChapaWebhookSignature.sign(WEBHOOK_SECRET, WEBHOOK_SUCCESS)).isEqualTo(WEBHOOK_SUCCESS_SIGNATURE);
        assertThat(ChapaWebhookSignature.matches(WEBHOOK_SECRET, WEBHOOK_SUCCESS_SIGNATURE, WEBHOOK_SUCCESS)).isTrue();
        // Hex case and surrounding spaces don't matter.
        assertThat(ChapaWebhookSignature.matches(WEBHOOK_SECRET, " " + WEBHOOK_SUCCESS_SIGNATURE.toUpperCase() + " ",
                WEBHOOK_SUCCESS)).isTrue();
    }

    @Test
    void oneChangedCharacterBreaksIt() {
        String tampered = WEBHOOK_SUCCESS.replace("\"amount\":\"400.00\"", "\"amount\":\"4000.00\"");
        assertThat(ChapaWebhookSignature.matches(WEBHOOK_SECRET, WEBHOOK_SUCCESS_SIGNATURE, tampered)).isFalse();
        // Re-formatted JSON (same data, extra space) is different bytes: also refused.
        assertThat(ChapaWebhookSignature.matches(WEBHOOK_SECRET, WEBHOOK_SUCCESS_SIGNATURE,
                WEBHOOK_SUCCESS.replace("\"event\":", "\"event\": "))).isFalse();
    }

    @Test
    void wrongSecretMissingHeaderAndTheConstantSignatureAreRefused() {
        assertThat(ChapaWebhookSignature.matches("another-secret-hash-0123456789abc", WEBHOOK_SUCCESS_SIGNATURE,
                WEBHOOK_SUCCESS)).isFalse();
        assertThat(ChapaWebhookSignature.matches(WEBHOOK_SECRET, null, WEBHOOK_SUCCESS)).isFalse();
        assertThat(ChapaWebhookSignature.matches("", WEBHOOK_SUCCESS_SIGNATURE, WEBHOOK_SUCCESS)).isFalse();
        // chapa-signature = HMAC(secret, secret) proves nothing about the body.
        assertThat(ChapaWebhookSignature.matches(WEBHOOK_SECRET, CONSTANT_SIGNATURE, WEBHOOK_SUCCESS)).isFalse();
    }
}
