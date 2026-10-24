package et.adey.mfi.mobilemoney;

import et.adey.mfi.common.ApiException;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.WebhookCheck;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.util.Map;

/**
 * The provider calls this URL; no JWT (it isn't one of our users). The active provider adapter
 * decides whether the call is authentic (TelePay: HMAC over timestamp + body; Chapa: HMAC over
 * the body). The body is read as a raw String, because signatures cover the exact bytes: parsing
 * and re-serialising JSON could change spacing or field order and break them.
 *
 * Then: a TRUSTED event (simulator) goes straight into the inbox; a HINT (Chapa) only triggers
 * a verify call to the provider (PaymentVerifier). Either way we answer 2xx once the call is
 * authentic, so the provider stops re-sending.
 */
@Tag(name = "Mobile money")
@RestController
public class WebhookController {

    private final MobileMoneyProvider provider;
    private final WebhookInbox inbox;
    private final PaymentVerifier verifier;
    private final Clock clock;

    public WebhookController(MobileMoneyProvider provider, WebhookInbox inbox, PaymentVerifier verifier, Clock clock) {
        this.provider = provider;
        this.inbox = inbox;
        this.verifier = verifier;
        this.clock = clock;
    }

    @Operation(summary = "Webhook from the mobile money provider (signed, no JWT)")
    @SecurityRequirements
    @PostMapping("/api/webhooks/mobile-money")
    public Map<String, Object> receive(@RequestHeader HttpHeaders headers, @RequestBody String rawBody,
                                       HttpServletRequest http) {
        WebhookCheck check = provider.parseAndVerifyWebhook(headers, rawBody, clock.instant());
        return switch (check) {
            case WebhookCheck.Rejected r -> {
                inbox.rejected(r.auditReason(), http.getRemoteAddr());
                throw new ApiException(HttpStatus.valueOf(r.httpStatus()), r.code(), r.message());
            }
            case WebhookCheck.Trusted t -> {
                WebhookInbox.Result result = inbox.receive(t.event(), rawBody);
                yield Map.of("received", true, "eventId", t.event().eventId(), "result", result);
            }
            case WebhookCheck.Hint h -> {
                PaymentVerifier.Result result = verifier.onWebhook(h.reference(), h.event(), rawBody);
                yield Map.of("received", true, "reference", h.reference(), "result", result);
            }
        };
    }
}
