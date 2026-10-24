package et.adey.mfi.mobilemoney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Plain HTTP client for the TelePay provider (our simulator at /sim on this same app, but called
 * exactly like a remote service: over HTTP, with an API key). It only speaks TelePay's API;
 * SimulatorProvider is the adapter that turns it into our MobileMoneyProvider interface.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mobile-money", name = "provider", havingValue = "simulator", matchIfMissing = true)
public class TelePayClient {

    private final RestClient rest;

    public TelePayClient(RestClient.Builder builder, MobileMoneyProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        this.rest = builder.clone()
                .baseUrl(props.providerBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Authorization", "Bearer " + props.apiKey())
                .build();
    }

    public record ChargeRequest(String reference, String phone, BigDecimal amount, String currency, String callbackUrl) {
    }

    public record Charge(String chargeId, String reference, String status) {
    }

    /** A successful payment in the provider's books for one day. */
    public record Settlement(String chargeId, String reference, String providerTransactionId, BigDecimal amount,
                             String currency, Instant settledAt) {
    }

    /** Ask the provider to charge the client's wallet. The client then confirms on their phone. */
    public Charge createCharge(ChargeRequest request) {
        try {
            return rest.post().uri("/charges").contentType(MediaType.APPLICATION_JSON).body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new ProviderUnavailableException("Provider answered HTTP " + res.getStatusCode().value());
                    })
                    .body(Charge.class);
        } catch (RestClientException e) {
            log.warn("TelePay charge request failed: {}", e.getMessage());
            throw new ProviderUnavailableException("Mobile money provider not reachable: " + e.getMessage());
        }
    }

    /** One charge as the provider sees it (for verification). */
    public record ChargeStatus(String chargeId, String reference, String status, String providerTransactionId,
                               BigDecimal settledAmount, String currency, Instant settledAt) {
    }

    /** Look a charge up by OUR reference; empty if the provider has none. */
    public Optional<ChargeStatus> findCharge(String reference) {
        try {
            List<ChargeStatus> found = rest.get().uri("/charges?reference={reference}", reference)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new ProviderUnavailableException("Provider answered HTTP " + res.getStatusCode().value());
                    })
                    .body(new ParameterizedTypeReference<List<ChargeStatus>>() { });
            return found == null ? Optional.empty() : found.stream().findFirst();
        } catch (RestClientException e) {
            throw new ProviderUnavailableException("Mobile money provider not reachable: " + e.getMessage());
        }
    }

    /** The provider's list of successful payments on a day (for reconciliation). */
    public List<Settlement> settlements(LocalDate date) {
        try {
            return rest.get().uri("/settlements?date={date}", date)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new ProviderUnavailableException("Provider answered HTTP " + res.getStatusCode().value());
                    })
                    .body(new ParameterizedTypeReference<List<Settlement>>() { });
        } catch (RestClientException e) {
            throw new ProviderUnavailableException("Mobile money provider not reachable: " + e.getMessage());
        }
    }
}
