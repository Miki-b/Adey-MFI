package et.adey.mfi.loan;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import et.adey.mfi.idempotency.IdempotencyKeyHeader;
import et.adey.mfi.idempotency.IdempotencyService;
import et.adey.mfi.security.Officer;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Loan endpoints. Reads go to LoanService, lifecycle steps to LoanLifecycleService.
 * @AuthenticationPrincipal Officer = the logged-in officer, built from the verified access token.
 * @Valid runs the Bean Validation annotations on request bodies. Lifecycle bodies are
 * optional (required = false) because every field has a default (dates default to today).
 */
@Tag(name = "Loans")
@RestController
public class LoanController {

    private final LoanService loanService;
    private final LoanLifecycleService lifecycle;
    private final IdempotencyService idempotency;

    public LoanController(LoanService loanService, LoanLifecycleService lifecycle, IdempotencyService idempotency) {
        this.loanService = loanService;
        this.lifecycle = lifecycle;
        this.idempotency = idempotency;
    }

    // ---- Reads ---------------------------------------------------------------------------

    @Operation(operationId = "getClientLoans", summary = "A client's loans with progress and next due installment")
    @GetMapping("/api/clients/{clientId}/loans")
    public List<LoanSummary> clientLoans(@AuthenticationPrincipal Officer officer, @PathVariable long clientId) {
        return loanService.getClientLoans(officer, clientId);
    }

    @Operation(operationId = "getLoan", summary = "Loan details: totals, full schedule and transactions")
    @GetMapping("/api/loans/{loanId}")
    public LoanDetails get(@AuthenticationPrincipal Officer officer, @PathVariable long loanId) {
        return loanService.getLoan(officer, loanId);
    }

    @Operation(operationId = "previewSchedule", summary = "Preview a repayment schedule without creating a loan")
    @PostMapping("/api/loans/schedule-preview")
    public SchedulePreviewResponse previewSchedule(@AuthenticationPrincipal Officer officer,
                                                   @Valid @RequestBody SchedulePreviewRequest request) {
        return loanService.previewSchedule(officer, request);
    }

    // ---- Lifecycle -----------------------------------------------------------------------

    @Operation(operationId = "submitLoanApplication", summary = "Submit a loan application (409 if the client already has a loan in progress)")
    @PostMapping("/api/loans")
    public ResponseEntity<LoanStatusResponse> submit(@AuthenticationPrincipal Officer officer,
                                                     @IdempotencyKeyHeader @RequestHeader(name = IdempotencyService.HEADER, required = false) String idempotencyKey,
                                                     @Valid @RequestBody LoanApplicationRequest request) {
        var outcome = idempotency.execute(officer, idempotencyKey, "POST /api/loans", request,
                LoanStatusResponse.class, () -> lifecycle.submit(officer, request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(outcome.body().loanId()).toUri();
        return withReplayHeader(ResponseEntity.created(location), outcome.replayed()).body(outcome.body());
    }

    @Operation(operationId = "approveLoan", summary = "Approve a submitted application (branch manager only)")
    @PostMapping("/api/loans/{loanId}/approve")
    public LoanStatusResponse approve(@AuthenticationPrincipal Officer officer, @PathVariable long loanId,
                                      @Valid @RequestBody(required = false) ApproveLoanRequest request) {
        return lifecycle.approve(officer, loanId, request);
    }

    @Operation(operationId = "undoLoanApproval", summary = "Undo an approval: back to pending, only before disbursement (branch manager only)")
    @PostMapping("/api/loans/{loanId}/undo-approval")
    public LoanStatusResponse undoApproval(@AuthenticationPrincipal Officer officer, @PathVariable long loanId,
                                           @Valid @RequestBody(required = false) UndoApprovalRequest request) {
        return lifecycle.undoApproval(officer, loanId, request);
    }

    @Operation(operationId = "rejectLoan", summary = "Reject a submitted application (branch manager only)")
    @PostMapping("/api/loans/{loanId}/reject")
    public LoanStatusResponse reject(@AuthenticationPrincipal Officer officer, @PathVariable long loanId,
                                     @Valid @RequestBody(required = false) RejectLoanRequest request) {
        return lifecycle.reject(officer, loanId, request);
    }

    @Operation(operationId = "disburseLoan", summary = "Disburse (pay out) an approved loan; it becomes Active (branch manager only)")
    @PostMapping("/api/loans/{loanId}/disburse")
    public LoanStatusResponse disburse(@AuthenticationPrincipal Officer officer, @PathVariable long loanId,
                                       @Valid @RequestBody(required = false) DisburseLoanRequest request) {
        return lifecycle.disburse(officer, loanId, request);
    }

    @Operation(operationId = "recordRepayment", summary = "Record a repayment; returns how it was split over principal/interest/fees/penalties")
    @PostMapping("/api/loans/{loanId}/repayments")
    public ResponseEntity<LoanTransactionResponse> repay(@AuthenticationPrincipal Officer officer,
                                                         @PathVariable long loanId,
                                                         @IdempotencyKeyHeader @RequestHeader(name = IdempotencyService.HEADER, required = false) String idempotencyKey,
                                                         @Valid @RequestBody RepaymentRequest request) {
        var outcome = idempotency.execute(officer, idempotencyKey, "POST /api/loans/" + loanId + "/repayments",
                request, LoanTransactionResponse.class, () -> lifecycle.repay(officer, loanId, request));
        return withReplayHeader(ResponseEntity.ok(), outcome.replayed()).body(outcome.body());
    }

    /** "Idempotent-Replayed: true" tells the app this answer is the stored one from an earlier attempt. */
    public static ResponseEntity.BodyBuilder withReplayHeader(ResponseEntity.BodyBuilder builder, boolean replayed) {
        return replayed ? builder.header("Idempotent-Replayed", "true") : builder;
    }
}
