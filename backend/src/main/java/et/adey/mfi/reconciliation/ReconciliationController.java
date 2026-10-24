package et.adey.mfi.reconciliation;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.security.Officer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Mobile money reconciliation (branch managers only). */
@Tag(name = "Reconciliation")
@RestController
public class ReconciliationController {

    private final ReconciliationService service;

    public ReconciliationController(ReconciliationService service) {
        this.service = service;
    }

    @Operation(summary = "Run reconciliation for a date now (default today). Manager only")
    @PostMapping("/api/reconciliation/run")
    public ReconciliationReport run(@AuthenticationPrincipal Officer officer,
                                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                    LocalDate date) {
        return service.runAsManager(officer, date != null ? date : FineractDates.today());
    }

    @Operation(summary = "Latest reconciliation for a date: summary + findings of your office. Manager only")
    @GetMapping("/api/reconciliation/{date}")
    public ReconciliationReport get(@AuthenticationPrincipal Officer officer,
                                    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return service.view(officer, date);
    }

    @Operation(summary = "Resolve a finding: PAID_NOT_POSTED is posted to Fineract; others need a note. Manager only")
    @PostMapping("/api/reconciliation/findings/{id}/resolve")
    public ReconciliationReport.FindingResponse resolve(@AuthenticationPrincipal Officer officer, @PathVariable long id,
                                                        @Valid @RequestBody(required = false) ResolveRequest request) {
        return service.resolve(officer, id, request);
    }
}
