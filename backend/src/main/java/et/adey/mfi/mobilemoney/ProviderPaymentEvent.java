package et.adey.mfi.mobilemoney;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A payment result in OUR words (status SUCCEEDED or DECLINED), whatever provider it came from:
 * the simulator's signed webhook body maps 1:1 onto it, and for Chapa PaymentVerifier builds it from
 * Chapa's verify answer. This is what the inbox (WebhookInbox) stores and applies.
 */
public record ProviderPaymentEvent(
        String eventId,
        String chargeId,
        String reference,
        BigDecimal amount,
        String currency,
        String status,
        String providerTransactionId,
        Instant timestamp) {
}
