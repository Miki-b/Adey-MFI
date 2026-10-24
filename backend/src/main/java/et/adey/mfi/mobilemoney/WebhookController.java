package et.adey.mfi.mobilemoney;

import et.adey.mfi.common.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.util.Map;

/**
 * The provider calls this URL; no JWT (it isn't one of our users). Instead every call must carry a
 * valid HMAC signature and a fresh timestamp. The body is read as a raw String, because the
 * signature covers the exact bytes: parsing and re-serialising JSON could change spacing or field
 * order and break it.
 */
@Tag(name = "Mobile money")
@RestController
public class WebhookController {

    private final MobileMoneyProperties props;
    private final WebhookInbox inbox;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public WebhookController(MobileMoneyProperties props, WebhookInbox inbox, ObjectMapper objectMapper, Clock clock) {
        this.props = props;
        this.inbox = inbox;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Operation(summary = "Webhook from the mobile money provider (HMAC-signed, no JWT)")
    @SecurityRequirements
    @PostMapping(value = "/api/webhooks/mobile-money", consumes = "application/json")
    public Map<String, Object> receive(
            @RequestHeader(name = WebhookSignature.TIMESTAMP_HEADER, required = false) String timestamp,
            @RequestHeader(name = WebhookSignature.SIGNATURE_HEADER, required = false) String signature,
            @RequestBody String rawBody,
            HttpServletRequest http) {
        WebhookSignature.Result check = WebhookSignature.verify(props.webhookSecret(), timestamp, signature, rawBody,
                clock.instant(), props.timestampTolerance());
        switch (check) {
            case MISSING, BAD_SIGNATURE -> {
                inbox.rejected("signature " + check, http.getRemoteAddr());
                throw new ApiException(HttpStatus.UNAUTHORIZED, "WEBHOOK_SIGNATURE_INVALID",
                        "Webhook signature missing or invalid.");
            }
            case STALE_TIMESTAMP -> {
                inbox.rejected("timestamp " + timestamp + " outside " + props.timestampTolerance(), http.getRemoteAddr());
                throw new ApiException(HttpStatus.UNAUTHORIZED, "WEBHOOK_TIMESTAMP_INVALID",
                        "Webhook timestamp is too old or too far in the future.");
            }
            case VALID -> { }
        }
        TelePayWebhook webhook;
        try {
            webhook = objectMapper.readValue(rawBody, TelePayWebhook.class);
        } catch (JacksonException e) {
            inbox.rejected("signed but unreadable body", http.getRemoteAddr());
            throw new ApiException(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Webhook body is not valid JSON.");
        }
        if (webhook.eventId() == null || webhook.eventId().isBlank()) {
            inbox.rejected("signed but no eventId", http.getRemoteAddr());
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Webhook has no eventId.");
        }
        // 2xx for everything we have stored (even duplicates/mismatches): the provider must stop re-sending.
        WebhookInbox.Result result = inbox.receive(webhook, rawBody);
        return Map.of("received", true, "eventId", webhook.eventId(), "result", result);
    }
}
