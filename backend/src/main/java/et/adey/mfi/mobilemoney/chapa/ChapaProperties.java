package et.adey.mfi.mobilemoney.chapa;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * "app.chapa.*", used only when app.mobile-money.provider=chapa.
 * secretKey (CHAPA_SECRET_KEY) must be a TEST key; ChapaProvider refuses to start otherwise.
 * webhookSecret (CHAPA_WEBHOOK_SECRET) is the "secret hash" also entered in Chapa's dashboard;
 * without it every webhook is refused, and payments still complete through verify polling.
 * Chapa's encryption key is NOT needed: it only encrypts card data and OTPs, and telebirr/M-Pesa
 * are confirmed by USSD on the client's phone.
 */
@ConfigurationProperties(prefix = "app.chapa")
public record ChapaProperties(
        @DefaultValue("https://api.chapa.co/v1") String baseUrl,
        String secretKey,
        String webhookSecret,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("20s") Duration readTimeout) {

    @Override
    public String toString() {
        return "ChapaProperties[baseUrl=%s, secretKey=***, webhookSecret=***]".formatted(baseUrl);
    }
}
