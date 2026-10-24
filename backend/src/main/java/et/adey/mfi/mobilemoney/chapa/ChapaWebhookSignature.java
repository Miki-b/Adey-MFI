package et.adey.mfi.mobilemoney.chapa;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

/**
 * Chapa's webhook signature: header x-chapa-signature = hex(HMAC-SHA256(secret hash, body)).
 *
 * Chapa also sends "chapa-signature" = HMAC-SHA256(secret, secret). It is the same value on EVERY
 * webhook and does not depend on the body, so anyone who saw one webhook could copy it onto a
 * forged body. We ignore it and only check the body signature.
 *
 * Differences from our TelePay simulator's scheme: there is no signed timestamp and no event id,
 * so a recorded genuine webhook could be replayed. That is acceptable only because we never act
 * on a Chapa webhook directly: it just triggers a call to Chapa's verify API (see PaymentVerifier).
 */
public final class ChapaWebhookSignature {

    public static final String HEADER = "x-chapa-signature";

    private ChapaWebhookSignature() {
    }

    /** hex(HMAC-SHA256(secret, body)), lowercase. */
    public static String sign(String secret, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Constant-time comparison over the exact bytes received (see WebhookSignature for why). */
    public static boolean matches(String secret, String signatureHeader, String body) {
        if (secret == null || secret.isEmpty() || signatureHeader == null || body == null) {
            return false;
        }
        byte[] expected = sign(secret, body).getBytes(StandardCharsets.US_ASCII);
        byte[] given = signatureHeader.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(expected, given);
    }
}
