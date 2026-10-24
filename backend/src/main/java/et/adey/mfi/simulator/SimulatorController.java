package et.adey.mfi.simulator;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * HTTP API of the simulated provider, under /sim (local profile only, hidden from our OpenAPI spec).
 *
 *   POST /sim/charges                       our backend asks for a charge (needs the provider API key)
 *   GET  /sim/charges/{id}                  the charge, incl. every webhook delivery attempt
 *   GET  /sim/charges?reference=AMP-...     charges with OUR reference (verification; needs the API key)
 *   POST /sim/charges/{id}/approve          "customer pressed Pay"; chaos flags:
 *        ?duplicate=true                    send the webhook twice (same eventId)
 *        ?delaySeconds=20                   wait before sending
 *        ?wrongSecret=true                  sign with a wrong secret
 *        ?drop=true                         never send the webhook
 *        ?reportAmount=1000                 provider reports (and settles) a different amount
 *        ?staleSeconds=600                  sign with a timestamp this many seconds in the past
 *   POST /sim/charges/{id}/decline          "customer pressed Decline"
 *   GET  /sim/settlements?date=YYYY-MM-DD   successful payments that day (for reconciliation)
 */
@Hidden
@Profile("local")
@RestController
@RequestMapping("/sim")
public class SimulatorController {

    private final TelePaySimulator sim;

    public SimulatorController(TelePaySimulator sim) {
        this.sim = sim;
    }

    public record ChargeRequest(String reference, String phone, BigDecimal amount, String currency, String callbackUrl) {
    }

    @PostMapping("/charges")
    public ResponseEntity<Map<String, Object>> create(@RequestHeader(name = "Authorization", required = false) String auth,
                                                      @RequestBody ChargeRequest req) {
        if (!sim.apiKeyValid(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid API key"));
        }
        if (req.reference() == null || req.amount() == null || req.amount().signum() <= 0 || req.callbackUrl() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "reference, amount > 0 and callbackUrl are required"));
        }
        TelePaySimulator.Charge c = sim.create(req.reference(), req.phone(), req.amount(),
                req.currency() == null ? "ETB" : req.currency(), req.callbackUrl());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("chargeId", c.chargeId, "reference", c.reference, "status", c.status));
    }

    @GetMapping("/charges")
    public ResponseEntity<Object> byReference(@RequestHeader(name = "Authorization", required = false) String auth,
                                              @RequestParam String reference) {
        if (!sim.apiKeyValid(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid API key"));
        }
        return ResponseEntity.ok(sim.findByReference(reference).stream().map(TelePaySimulator.Charge::view).toList());
    }

    @GetMapping("/charges/{id}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable String id) {
        TelePaySimulator.Charge c = sim.find(id);
        return c == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(c.view());
    }

    @PostMapping("/charges/{id}/approve")
    public ResponseEntity<Map<String, Object>> approve(@PathVariable String id,
                                                       @RequestParam(defaultValue = "false") boolean duplicate,
                                                       @RequestParam(defaultValue = "0") int delaySeconds,
                                                       @RequestParam(defaultValue = "false") boolean wrongSecret,
                                                       @RequestParam(defaultValue = "false") boolean drop,
                                                       @RequestParam(required = false) BigDecimal reportAmount,
                                                       @RequestParam(defaultValue = "0") int staleSeconds) {
        return act(id, true, new TelePaySimulator.Chaos(duplicate, delaySeconds, wrongSecret, drop, reportAmount, staleSeconds));
    }

    @PostMapping("/charges/{id}/decline")
    public ResponseEntity<Map<String, Object>> decline(@PathVariable String id) {
        return act(id, false, new TelePaySimulator.Chaos(false, 0, false, false, null, 0));
    }

    @GetMapping("/settlements")
    public Object settlements(@RequestHeader(name = "Authorization", required = false) String auth,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (!sim.apiKeyValid(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid API key"));
        }
        return sim.settlements(date);
    }

    private ResponseEntity<Map<String, Object>> act(String id, boolean approve, TelePaySimulator.Chaos chaos) {
        TelePaySimulator.Charge c = sim.find(id);
        if (c == null) {
            return ResponseEntity.notFound().build();
        }
        if (!"PENDING".equals(c.status)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "charge is already " + c.status));
        }
        Map<String, Object> plan = approve ? sim.approve(c, chaos) : sim.decline(c, chaos);
        Map<String, Object> body = new java.util.LinkedHashMap<>(c.view());
        body.putAll(plan);
        return ResponseEntity.ok(body);
    }
}
