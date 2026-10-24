package et.adey.mfi.mobilemoney;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * "app.mobile-money.*". provider picks the adapter (simulator | chapa). The TelePay settings
 * (provider URL, API key, webhook secret, callback URL) are only required when provider=simulator;
 * SimulatorProvider checks them at startup. Chapa has its own settings (ChapaProperties).
 */
@Validated
@ConfigurationProperties(prefix = "app.mobile-money")
public record MobileMoneyProperties(
        @DefaultValue("simulator") String provider,
        String providerBaseUrl,
        String apiKey,
        /* Shared secret for TelePay webhook signatures, 32+ characters. */
        String webhookSecret,
        /* Where TelePay sends webhooks (our public URL). */
        String callbackUrl,
        /* Webhooks with a timestamp further from "now" than this are refused (replay protection). */
        @DefaultValue("5m") Duration timestampTolerance,
        @DefaultValue("ETB") String currency,
        /* Name of the Fineract payment type used for these repayments. */
        @DefaultValue("Mobile Money") String paymentTypeName,
        /* Background jobs on/off (off in tests, which call them directly). */
        @DefaultValue("true") boolean workersEnabled,
        @Valid @NotNull Posting posting,
        @Valid @NotNull Verify verify) {

    /** Retry with exponential backoff: baseDelay, x2 each failure, capped at maxDelay, maxAttempts in total. */
    public record Posting(
            @DefaultValue("8") @Min(1) int maxAttempts,
            @DefaultValue("10s") Duration baseDelay,
            @DefaultValue("2m") Duration maxDelay) {
    }

    /**
     * Polling the provider's verify endpoint for PENDING payments (providers whose webhooks are only
     * hints, i.e. Chapa). firstCheckAfter gives the webhook a head start; then backoff from
     * baseDelay, doubling up to maxDelay, maxAttempts checks in total before a person must look.
     */
    public record Verify(
            @DefaultValue("true") boolean pollingEnabled,
            @DefaultValue("10s") Duration firstCheckAfter,
            @DefaultValue("20") @Min(1) int maxAttempts,
            @DefaultValue("5s") Duration baseDelay,
            @DefaultValue("2m") Duration maxDelay) {
    }

    @Override
    public String toString() {
        return "MobileMoneyProperties[provider=%s, providerBaseUrl=%s, apiKey=***, webhookSecret=***, callbackUrl=%s]"
                .formatted(provider, providerBaseUrl, callbackUrl);
    }
}
