package et.adey.mfi.client;

import et.adey.mfi.common.PageResponse;
import et.adey.mfi.idempotency.IdempotencyKeyHeader;
import et.adey.mfi.idempotency.IdempotencyService;
import et.adey.mfi.loan.LoanController;
import et.adey.mfi.security.Officer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/** @Validated enables the @Min/@Max checks on query parameters below. */
@Tag(name = "Clients")
@Validated
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;
    private final IdempotencyService idempotency;

    public ClientController(ClientService clientService, IdempotencyService idempotency) {
        this.clientService = clientService;
        this.idempotency = idempotency;
    }

    @Operation(operationId = "listClients", summary = "Search clients (paged): an officer sees their own clients, a manager their whole office")
    @GetMapping
    public PageResponse<ClientSummary> search(
            @AuthenticationPrincipal Officer officer,
            @Parameter(description = "Part of the client's name. Case-sensitive (Fineract on PostgreSQL).")
            @RequestParam(required = false) String search,
            @Parameter(description = "0-based page number")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return clientService.search(officer, search, page, size);
    }

    @Operation(operationId = "createClient", summary = "Create a client in your office (officer: assigned to you; manager: choose staffId)")
    @PostMapping
    public ResponseEntity<ClientDetails> create(@AuthenticationPrincipal Officer officer,
                                                @IdempotencyKeyHeader @RequestHeader(name = IdempotencyService.HEADER, required = false) String idempotencyKey,
                                                @Valid @RequestBody CreateClientRequest request) {
        var outcome = idempotency.execute(officer, idempotencyKey, "POST /api/clients", request,
                ClientDetails.class, () -> clientService.create(officer, request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(outcome.body().id()).toUri();
        return LoanController.withReplayHeader(ResponseEntity.created(location), outcome.replayed())
                .body(outcome.body());
    }

    @Operation(operationId = "updateClient", summary = "Edit a client's basic details (only the fields you send are changed)")
    @PutMapping("/{clientId}")
    public ClientDetails update(@AuthenticationPrincipal Officer officer, @PathVariable long clientId,
                                @Valid @RequestBody UpdateClientRequest request) {
        return clientService.update(officer, clientId, request);
    }

    @Operation(operationId = "getClient", summary = "Client details (403 if the client is not yours / not in your office)")
    @GetMapping("/{clientId}")
    public ClientDetails get(@AuthenticationPrincipal Officer officer, @PathVariable long clientId) {
        return clientService.getClient(officer, clientId);
    }
}
