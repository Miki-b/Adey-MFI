package et.adey.mfi.mobilemoney;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ADAPTER for our TelePay simulator (app.mobile-money.provider=simulator, the default).
 *
 * TelePay's webhooks are trustworthy on their own: signed over timestamp + body, refused when older
 * than 5 minutes, and carrying a unique eventId. So they go straight into the inbox (Trusted), and
 * reconciliation uses TelePay's settlement listing. Behaviour is exactly as before Phase 5b.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.mobile-money", name = "provider", havingValue = "simulator", matchIfMissing = true)
public class SimulatorProvider implements MobileMoneyProvider {

    private final TelePayClient telePay;
    private final MobileMoneyProperties props;
    private final ObjectMapper objectMapper;

    public SimulatorProvider(TelePayClient telePay, MobileMoneyProperties props, ObjectMapper objectMapper) {
        // These settings only matter for TelePay, so they are checked here instead of on the properties.
        require(props.providerBaseUrl(), "app.mobile-money.provider-base-url (TELEPAY_BASE_URL)");
        require(props.apiKey(), "app.mobile-money.api-key (TELEPAY_API_KEY)");
        require(props.callbackUrl(), "app.mobile-money.callback-url (MOBILE_MONEY_CALLBACK_URL)");
        if (props.webhookSecret() == null || props.webhookSecret().length() < 32) {
            throw new IllegalStateException("app.mobile-money.webhook-secret (MOBILE_MONEY_WEBHOOK_SECRET) must be at "
                    + "least 32 characters for the simulator provider");
        }
        this.telePay = telePay;
        this.props = props;
        this.objectMapper = objectMapper;
        log.info("Mobile money provider: simulator (TelePay at {})", props.providerBaseUrl());
    }

    @Override
    public String name() {
        return "simulator";
    }

    @Override
    public ChargeResult createCharge(ChargeRequest request) {
        TelePayClient.Charge charge = telePay.createCharge(new TelePayClient.ChargeRequest(request.reference(),
                request.phone(), request.amount(), request.currency(), props.callbackUrl()));
        return ChargeResult.accepted(charge.chargeId());
    }

    @Override
    public Verification verifyCharge(String reference) {
        return telePay.findCharge(reference)
                .map(c -> new Verification(switch (c.status() == null ? "" : c.status()) {
                    case "SUCCEEDED" -> VerifiedStatus.SUCCEEDED;
                    case "DECLINED" -> VerifiedStatus.DECLINED;
                    case "PENDING" -> VerifiedStatus.PENDING;
                    default -> VerifiedStatus.UNKNOWN;
                }, c.status(), c.reference(), c.providerTransactionId(), c.settledAmount(), c.currency(),
                        c.settledAt(), null))
                .orElse(new Verification(VerifiedStatus.NOT_FOUND, null, reference, null, null, null, null, null));
    }

    @Override
    public WebhookCheck parseAndVerifyWebhook(HttpHeaders headers, String rawBody, Instant now) {
        String timestamp = headers.getFirst(WebhookSignature.TIMESTAMP_HEADER);
        WebhookSignature.Result check = WebhookSignature.verify(props.webhookSecret(), timestamp,
                headers.getFirst(WebhookSignature.SIGNATURE_HEADER), rawBody, now, props.timestampTolerance());
        switch (check) {
            case MISSING, BAD_SIGNATURE -> {
                return new WebhookCheck.Rejected(401, "WEBHOOK_SIGNATURE_INVALID",
                        "Webhook signature missing or invalid.", "signature " + check);
            }
            case STALE_TIMESTAMP -> {
                return new WebhookCheck.Rejected(401, "WEBHOOK_TIMESTAMP_INVALID",
                        "Webhook timestamp is too old or too far in the future.",
                        "timestamp " + timestamp + " outside " + props.timestampTolerance());
            }
            case VALID -> { }
        }
        ProviderPaymentEvent event;
        try {
            event = objectMapper.readValue(rawBody, ProviderPaymentEvent.class);
        } catch (JacksonException e) {
            return new WebhookCheck.Rejected(400, "MALFORMED_REQUEST", "Webhook body is not valid JSON.",
                    "signed but unreadable body");
        }
        if (!StringUtils.hasText(event.eventId())) {
            return new WebhookCheck.Rejected(400, "VALIDATION_FAILED", "Webhook has no eventId.", "signed but no eventId");
        }
        return new WebhookCheck.Trusted(event);
    }

    @Override
    public Optional<List<Settlement>> listSettlements(LocalDate date) {
        return Optional.of(telePay.settlements(date).stream()
                .map(s -> new Settlement(s.chargeId(), s.reference(), s.providerTransactionId(), s.amount(),
                        s.currency(), s.settledAt()))
                .toList());
    }

    @Override
    public boolean needsVerification() {
        return false;
    }

    private static void require(String value, String what) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(what + " must be set for the simulator provider");
        }
    }
}
