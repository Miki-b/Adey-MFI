package et.adey.mfi.mobilemoney;

import java.math.BigDecimal;
import java.time.Instant;

/** The provider's webhook body (status: SUCCEEDED or DECLINED). */
public record TelePayWebhook(
        String eventId,
        String chargeId,
        String reference,
        BigDecimal amount,
        String currency,
        String status,
        String providerTransactionId,
        Instant timestamp) {
}
