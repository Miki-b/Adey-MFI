package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** @Valid on the body runs the Bean Validation annotations on SchedulePreviewRequest. */
@Tag(name = "Loans")
@RestController
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @Operation(summary = "A client's loans with progress and next due installment")
    @GetMapping("/api/clients/{clientId}/loans")
    public List<LoanSummary> clientLoans(@PathVariable long clientId) {
        return loanService.getClientLoans(clientId);
    }

    @Operation(summary = "Preview a repayment schedule without creating a loan")
    @PostMapping("/api/loans/schedule-preview")
    public SchedulePreviewResponse previewSchedule(@Valid @RequestBody SchedulePreviewRequest request) {
        return loanService.previewSchedule(request);
    }
}
