package et.adey.mfi.mobilemoney;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

/**
 * WEBHOOK SIGNATURE + REPLAY PROTECTION.
 *
 * Our webhook URL is public (the provider has no JWT), so anyone could POST "payment succeeded" to it.
 * The provider and we share a secret. For every webhook the provider computes
 *     signature = HMAC-SHA256(secret, timestamp + "." + body)
 * and sends it in X-TelePay-Signature ("v1=<hex>") with the timestamp in X-TelePay-Timestamp.
 * We recompute it over the EXACT bytes we received: only someone who knows the secret can produce
 * a matching signature, and changing one character of the body or the timestamp breaks it.
 *
 * The timestamp is inside the signature, so it can't be changed. Refusing timestamps older than 5
 * minutes stops REPLAY: an attacker who recorded a genuine webhook can't resend it tomorrow.
 * (Inside the 5 minutes, the stored eventId catches repeats.)
 *
 * The comparison is CONSTANT-TIME (MessageDigest.isEqual): a normal equals() stops at the first
 * wrong character, and measuring how long that takes would let an attacker guess the signature
 * one character at a time.
 */
public final class WebhookSignature {

    public static final String TIMESTAMP_HEADER = "X-TelePay-Timestamp";
    public static final String SIGNATURE_HEADER = "X-TelePay-Signature";
    private static final String VERSION_PREFIX = "v1=";

    private WebhookSignature() {
    }

    public enum Result { VALID, MISSING, BAD_SIGNATURE, STALE_TIMESTAMP }

    /** hex(HMAC-SHA256(secret, timestamp + "." + body)) */
    public static String sign(String secret, long timestampSeconds, String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((timestampSeconds + "." + body).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Result verify(String secret, String timestampHeader, String signatureHeader, String body,
                                Instant now, Duration tolerance) {
        if (timestampHeader == null || signatureHeader == null || body == null
                || !signatureHeader.startsWith(VERSION_PREFIX)) {
            return Result.MISSING;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(timestampHeader.trim());
        } catch (NumberFormatException e) {
            return Result.MISSING;
        }
        // 1. Signature first: only a correctly signed timestamp is worth looking at.
        byte[] expected = sign(secret, timestamp, body).getBytes(StandardCharsets.US_ASCII);
        byte[] given = signatureHeader.substring(VERSION_PREFIX.length()).trim().getBytes(StandardCharsets.US_ASCII);
        if (!MessageDigest.isEqual(expected, given)) {
            return Result.BAD_SIGNATURE;
        }
        // 2. Freshness: within the tolerance in either direction (clocks are never exactly equal).
        long ageSeconds = Math.abs(now.getEpochSecond() - timestamp);
        if (ageSeconds > tolerance.toSeconds()) {
            return Result.STALE_TIMESTAMP;
        }
        return Result.VALID;
    }

    public static String header(String signatureHex) {
        return VERSION_PREFIX + signatureHex;
    }
}
