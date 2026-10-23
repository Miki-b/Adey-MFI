package et.adey.mfi.client;

import et.adey.mfi.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** @Validated enables the @Min/@Max checks on query parameters below. */
@Tag(name = "Clients")
@Validated
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(summary = "Search clients (paged)")
    @GetMapping
    public PageResponse<ClientSummary> search(
            @Parameter(description = "Part of the client's name. Case-sensitive (Fineract on PostgreSQL).")
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long officeId,
            @Parameter(description = "Loan officer (staff) id")
            @RequestParam(required = false) Long staffId,
            @Parameter(description = "0-based page number")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return clientService.search(search, officeId, staffId, page, size);
    }

    @Operation(summary = "Client details")
    @GetMapping("/{clientId}")
    public ClientDetails get(@PathVariable long clientId) {
        return clientService.getClient(clientId);
    }
}
