package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Loan endpoints. Reads go to LoanService, lifecycle steps to LoanLifecycleService.
 * @Valid runs the Bean Validation annotations on request bodies. Lifecycle bodies are
 * optional (required = false) because every field has a default (dates default to today).
 */
@Tag(name = "Loans")
@RestController
public class LoanController {

    private final LoanService loanService;
    private final LoanLifecycleService lifecycle;

    public LoanController(LoanService loanService, LoanLifecycleService lifecycle) {
        this.loanService = loanService;
        this.lifecycle = lifecycle;
    }

    // ---- Reads ---------------------------------------------------------------------------

    @Operation(summary = "A client's loans with progress and next due installment")
    @GetMapping("/api/clients/{clientId}/loans")
    public List<LoanSummary> clientLoans(@PathVariable long clientId) {
        return loanService.getClientLoans(clientId);
    }

    @Operation(summary = "Loan details: totals, full schedule and transactions")
    @GetMapping("/api/loans/{loanId}")
    public LoanDetails get(@PathVariable long loanId) {
        return loanService.getLoan(loanId);
    }

    @Operation(summary = "Preview a repayment schedule without creating a loan")
    @PostMapping("/api/loans/schedule-preview")
    public SchedulePreviewResponse previewSchedule(@Valid @RequestBody SchedulePreviewRequest request) {
        return loanService.previewSchedule(request);
    }

    // ---- Lifecycle -----------------------------------------------------------------------

    @Operation(summary = "Submit a loan application (409 if the client already has a loan in progress)")
    @PostMapping("/api/loans")
    public ResponseEntity<LoanStatusResponse> submit(@Valid @RequestBody LoanApplicationRequest request) {
        LoanStatusResponse created = lifecycle.submit(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.loanId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Approve a submitted application")
    @PostMapping("/api/loans/{loanId}/approve")
    public LoanStatusResponse approve(@PathVariable long loanId,
                                      @Valid @RequestBody(required = false) ApproveLoanRequest request) {
        return lifecycle.approve(loanId, request);
    }

    @Operation(summary = "Undo an approval (back to pending, only before disbursement)")
    @PostMapping("/api/loans/{loanId}/undo-approval")
    public LoanStatusResponse undoApproval(@PathVariable long loanId,
                                           @Valid @RequestBody(required = false) UndoApprovalRequest request) {
        return lifecycle.undoApproval(loanId, request);
    }

    @Operation(summary = "Reject a submitted application")
    @PostMapping("/api/loans/{loanId}/reject")
    public LoanStatusResponse reject(@PathVariable long loanId,
                                     @Valid @RequestBody(required = false) RejectLoanRequest request) {
        return lifecycle.reject(loanId, request);
    }

    @Operation(summary = "Disburse (pay out) an approved loan; it becomes Active")
    @PostMapping("/api/loans/{loanId}/disburse")
    public LoanStatusResponse disburse(@PathVariable long loanId,
                                       @Valid @RequestBody(required = false) DisburseLoanRequest request) {
        return lifecycle.disburse(loanId, request);
    }

    @Operation(summary = "Record a repayment; returns how it was split over principal/interest/fees/penalties")
    @PostMapping("/api/loans/{loanId}/repayments")
    public LoanTransactionResponse repay(@PathVariable long loanId,
                                         @Valid @RequestBody RepaymentRequest request) {
        return lifecycle.repay(loanId, request);
    }
}
