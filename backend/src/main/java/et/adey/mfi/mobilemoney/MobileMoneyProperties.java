package et.adey.mfi.mobilemoney;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * "app.mobile-money.*". Outside the local profile the provider URL, API key and webhook secret
 * MUST come from environment variables; without them the application refuses to start.
 */
@Validated
@ConfigurationProperties(prefix = "app.mobile-money")
public record MobileMoneyProperties(
        @NotBlank String providerBaseUrl,
        @NotBlank String apiKey,
        @NotBlank @Size(min = 32, message = "must be at least 32 characters (set MOBILE_MONEY_WEBHOOK_SECRET)")
        String webhookSecret,
        /* Where the provider sends webhooks (our public URL). */
        @NotBlank String callbackUrl,
        /* Webhooks with a timestamp further from "now" than this are refused (replay protection). */
        @DefaultValue("5m") Duration timestampTolerance,
        @DefaultValue("ETB") String currency,
        /* Name of the Fineract payment type used for these repayments. */
        @DefaultValue("Mobile Money") String paymentTypeName,
        /* Background jobs on/off (off in tests, which call them directly). */
        @DefaultValue("true") boolean workersEnabled,
        @Valid @NotNull Posting posting) {

    /** Retry with exponential backoff: baseDelay, x2 each failure, capped at maxDelay, maxAttempts in total. */
    public record Posting(
            @DefaultValue("8") @Min(1) int maxAttempts,
            @DefaultValue("10s") Duration baseDelay,
            @DefaultValue("2m") Duration maxDelay) {
    }

    @Override
    public String toString() {
        return "MobileMoneyProperties[providerBaseUrl=%s, apiKey=***, webhookSecret=***, callbackUrl=%s]"
                .formatted(providerBaseUrl, callbackUrl);
    }
}
