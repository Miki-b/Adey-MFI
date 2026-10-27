package et.adey.mfi.mobilemoney;

import et.adey.mfi.idempotency.IdempotencyKeyHeader;
import et.adey.mfi.idempotency.IdempotencyService;
import et.adey.mfi.loan.LoanController;
import et.adey.mfi.security.Officer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

/** Officer endpoints for mobile money repayments. The provider's webhook is in WebhookController. */
@Tag(name = "Mobile money")
@RestController
public class MobilePaymentController {

    private final MobilePaymentService service;
    private final IdempotencyService idempotency;

    public MobilePaymentController(MobilePaymentService service, IdempotencyService idempotency) {
        this.service = service;
        this.idempotency = idempotency;
    }

    @Operation(operationId = "requestMobilePayment", summary = "Ask the client to pay a repayment from their mobile wallet (client confirms on the phone)")
    @PostMapping("/api/loans/{loanId}/mobile-payments")
    public ResponseEntity<MobilePaymentResponse> request(@AuthenticationPrincipal Officer officer,
                                                         @PathVariable long loanId,
                                                         @IdempotencyKeyHeader @RequestHeader(name = IdempotencyService.HEADER, required = false) String idempotencyKey,
                                                         @Valid @RequestBody MobilePaymentRequest request) {
        var outcome = idempotency.execute(officer, idempotencyKey, "POST /api/loans/" + loanId + "/mobile-payments",
                request, MobilePaymentResponse.class, () -> service.request(officer, loanId, request));
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/mobile-payments/{id}").buildAndExpand(outcome.body().id()).toUri();
        return LoanController.withReplayHeader(ResponseEntity.created(location), outcome.replayed())
                .body(outcome.body());
    }

    @Operation(operationId = "getMobilePayment", summary = "One mobile payment with its status history (poll this until finalState is true)")
    @GetMapping("/api/mobile-payments/{id}")
    public MobilePaymentResponse get(@AuthenticationPrincipal Officer officer, @PathVariable long id) {
        return service.get(officer, id);
    }

    @Operation(operationId = "listLoanMobilePayments", summary = "All mobile payments of a loan, newest first")
    @GetMapping("/api/loans/{loanId}/mobile-payments")
    public List<MobilePaymentResponse> list(@AuthenticationPrincipal Officer officer, @PathVariable long loanId) {
        return service.listForLoan(officer, loanId);
    }
}
