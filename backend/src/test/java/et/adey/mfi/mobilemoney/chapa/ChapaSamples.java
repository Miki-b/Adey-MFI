package et.adey.mfi.mobilemoney.chapa;

/**
 * Chapa payloads for tests. The webhook follows the example in Chapa's webhook docs; the charge and
 * verify answers are copies of what Chapa's test mode returned to us (phone numbers masked).
 */
final class ChapaSamples {

    private ChapaSamples() {
    }

    static final String WEBHOOK_SECRET = "test-webhook-secret-hash-0123456789";

    static final String WEBHOOK_SUCCESS = "{\"event\":\"charge.success\",\"first_name\":\"Abebe\",\"last_name\":\"Kebede\","
            + "\"email\":null,\"mobile\":\"0900123456\",\"currency\":\"ETB\",\"amount\":\"400.00\",\"charge\":\"10.00\","
            + "\"status\":\"success\",\"mode\":\"test\",\"reference\":\"CHbXq2Mvg8E9M\","
            + "\"created_at\":\"2026-10-24T08:30:00.000000Z\",\"updated_at\":\"2026-10-24T08:30:05.000000Z\","
            + "\"type\":\"API\",\"tx_ref\":\"AMP-3F9A0C2B7E41\",\"payment_method\":\"telebirr\","
            + "\"customization\":{\"title\":null,\"description\":null,\"logo\":null},\"meta\":null}";

    /** hex(HMAC-SHA256(WEBHOOK_SECRET, WEBHOOK_SUCCESS)), computed independently (Python hmac). */
    static final String WEBHOOK_SUCCESS_SIGNATURE = "3332023b4685b6a1f8e22bf7ecc33aec1d3d6d7afb23b454a134dbf40a706710";

    /** Chapa's other header, HMAC-SHA256(secret, secret): the same for every webhook. */
    static final String CONSTANT_SIGNATURE = "20f67fd144450ca32da692e2496fe9be53449c5771ada74f8911102e2ad4a550";

    static final String CHARGE_INITIATED = "{\"message\":\"Charge initiated\",\"status\":\"success\",\"data\":{"
            + "\"auth_type\":\"ussd\",\"requestID\":\"j2CXLUX1Ttec7R51v7VU\",\"payment_status\":\"PENDING\","
            + "\"meta\":{\"message\":\"Payment successfully initiated with telebirr\",\"ref_id\":\"CHbXq2Mvg8E9M\","
            + "\"verification_type\":\"ussd\",\"status\":\"success\",\"data\":null}}}";

    static final String CHARGE_FAILED = "{\"message\":\"Charge failed to initiate\",\"status\":\"failed\",\"data\":{"
            + "\"meta\":{\"message\":\"Invalid Test Number or Invalid OTP or payment method, please refer to our documentation.\","
            + "\"status\":\"failed\",\"data\":null}}}";

    static String verify(String txRef, String status, String amount) {
        return ("{\"message\":\"Payment details fetched successfully\",\"status\":\"success\",\"data\":{"
                + "\"first_name\":null,\"last_name\":null,\"email\":null,\"phone_number\":\"251900123456\","
                + "\"currency\":\"ETB\",\"amount\":%s,\"charge\":0.25,\"mode\":\"test\",\"method\":\"telebirr\","
                + "\"type\":\"Direct Charges\",\"status\":\"%s\",\"reference\":\"CHbXq2Mvg8E9M\",\"tx_ref\":\"%s\","
                + "\"customization\":{\"title\":null,\"description\":null,\"logo\":null},\"meta\":null,"
                + "\"created_at\":\"2026-10-24T12:25:47.000000Z\",\"updated_at\":\"2026-10-24T12:25:49.000000Z\"}}")
                .formatted(amount, status, txRef);
    }

    static final String VERIFY_NOT_FOUND = "{\"message\":\"Invalid transaction reference\",\"status\":\"failed\",\"data\":null}";
}
