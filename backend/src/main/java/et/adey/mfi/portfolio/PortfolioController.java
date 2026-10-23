package et.adey.mfi.portfolio;

import et.adey.mfi.security.Officer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Loan lists (approval queue, collection list) and the officer / manager dashboard. */
@Tag(name = "Portfolio")
@RestController
public class PortfolioController {

    private final PortfolioService service;

    public PortfolioController(PortfolioService service) {
        this.service = service;
    }

    @Operation(summary = "Loans you may see, filtered. status=PENDING is the approval queue (oldest first); "
            + "inArrears=true is the collection list (most days overdue first)")
    @GetMapping("/api/loans")
    public List<LoanListItem> list(
            @AuthenticationPrincipal Officer officer,
            @RequestParam(required = false) LoanListStatus status,
            @Parameter(description = "true = only loans with an installment past due, false = only loans without")
            @RequestParam(required = false) Boolean inArrears,
            @RequestParam(required = false) Long clientId) {
        return service.listLoans(officer, status, inArrears, clientId);
    }

    @Operation(summary = "Dashboard: counts, outstanding, PAR1/PAR30, due today, overdue (+ per officer for managers)")
    @GetMapping("/api/me/portfolio")
    public Portfolio portfolio(@AuthenticationPrincipal Officer officer) {
        return service.portfolio(officer);
    }
}
