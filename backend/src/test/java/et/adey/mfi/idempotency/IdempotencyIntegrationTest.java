package et.adey.mfi.idempotency;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractUnavailableException;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractError;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import et.adey.mfi.fineract.model.FineractLoanTransaction;
import et.adey.mfi.fineract.model.FineractPaymentType;
import et.adey.mfi.fineract.model.FineractClientAccounts;
import et.adey.mfi.loan.RepaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static et.adey.mfi.TestOfficers.SAMUEL;
import static et.adey.mfi.TestOfficers.TADESSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Idempotency-Key on repayments and loan applications, end to end through the HTTP API. */
class IdempotencyIntegrationTest extends ApiTestBase {

    private static final String KEY = "3f0a9c5e-6f1b-4b8e-9d2a-7c1e0b5a4d33";
    private static final String PAY = "{\"amount\": 1000, \"paymentTypeId\": 4}";

    @Autowired
    private IdempotencyService service;

    @BeforeEach
    void stubRepayment() {
        stubLoan(1, 1, 300);
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));
        when(fineract.getPaymentTypes()).thenReturn(List.of(new FineractPaymentType(4L, "Cash", "Cash", true, 2, false)));
        when(fineract.repayLoan(eq(1L), any())).thenReturn(new FineractCommandResult(2L, 1L, 1L, 38L, Map.of()));
        when(fineract.getLoanTransaction(1L, 38L)).thenReturn(
                read("fineract/loan-1-transaction-4-partial-repayment.json", FineractLoanTransaction.class));
    }

    @Test
    void sameKeySameBodyIsRecordedOnlyOnce() throws Exception {
        repay(SAMUEL, KEY, PAY).andExpect(status().isOk())
                .andExpect(header().doesNotExist("Idempotent-Replayed"))
                .andExpect(jsonPath("$.interestPortion").value(277.0));
        repay(SAMUEL, KEY, PAY).andExpect(status().isOk())
                .andExpect(header().string("Idempotent-Replayed", "true"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.interestPortion").value(277.0));

        verify(fineract, times(1)).repayLoan(eq(1L), any());
        assertThat(auditLog.findAll()).filteredOn(r -> r.getAction() == AuditAction.LOAN_REPAY).hasSize(1);
    }

    @Test
    void sameKeyDifferentBodyIsRefused() throws Exception {
        repay(SAMUEL, KEY, PAY).andExpect(status().isOk());
        repay(SAMUEL, KEY, "{\"amount\": 1001, \"paymentTypeId\": 4}")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
        verify(fineract, times(1)).repayLoan(eq(1L), any());
    }

    @Test
    void withoutAKeyEveryRequestRuns() throws Exception {
        repay(SAMUEL, null, PAY).andExpect(status().isOk());
        repay(SAMUEL, null, PAY).andExpect(status().isOk());
        verify(fineract, times(2)).repayLoan(eq(1L), any());
    }

    @Test
    void refusedFirstAttemptReleasesTheKey() throws Exception {
        when(fineract.repayLoan(eq(1L), any()))
                .thenThrow(new FineractApiException(400, new FineractError(null, "Validation errors exist.", null,
                        List.of(new FineractError.FieldError("id", "Loan must be Active", null,
                                "error.msg.loan.must.be.active.fully.paid.or.overpaid")))))
                .thenReturn(new FineractCommandResult(2L, 1L, 1L, 38L, Map.of()));

        repay(SAMUEL, KEY, PAY).andExpect(status().isUnprocessableContent());
        // Nothing happened the first time, so the same key may run again.
        repay(SAMUEL, KEY, PAY).andExpect(status().isOk()).andExpect(header().doesNotExist("Idempotent-Replayed"));
        verify(fineract, times(2)).repayLoan(eq(1L), any());
    }

    @Test
    void unknownOutcomeBlocksTheKey() throws Exception {
        when(fineract.repayLoan(eq(1L), any())).thenThrow(new FineractUnavailableException("Read timed out", null));

        repay(SAMUEL, KEY, PAY).andExpect(status().isServiceUnavailable());
        // Fineract may have booked it: refuse to run again with this key.
        repay(SAMUEL, KEY, PAY).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_OUTCOME_UNKNOWN"));
        verify(fineract, times(1)).repayLoan(eq(1L), any());
    }

    @Test
    void requestStillRunningGives409() throws Exception {
        String hash = service.fingerprint("POST /api/loans/1/repayments",
                new RepaymentRequest(null, new BigDecimal("1000"), 4L, null));
        Instant now = Instant.now();
        idempotencyKeys.save(new IdempotencyRecord("samuel", KEY, "POST /api/loans/1/repayments", hash,
                now, now.plusSeconds(3600)));

        repay(SAMUEL, KEY, PAY).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_IN_PROGRESS"));
        verify(fineract, never()).repayLoan(eq(1L), any());
    }

    @Test
    void expiredKeysAreIgnored() throws Exception {
        Instant longAgo = Instant.now().minusSeconds(25 * 3600);
        idempotencyKeys.save(new IdempotencyRecord("samuel", KEY, "POST /api/loans/1/repayments", "old-hash",
                longAgo, longAgo.plus(IdempotencyService.TTL)));

        repay(SAMUEL, KEY, PAY).andExpect(status().isOk());
        verify(fineract, times(1)).repayLoan(eq(1L), any());
        assertThat(idempotencyKeys.deleteExpired(Instant.now().plus(IdempotencyService.TTL).plusSeconds(1)))
                .isEqualTo(1);
    }

    @Test
    void keysArePerUserAndMustBeWellFormed() throws Exception {
        repay(SAMUEL, KEY, PAY).andExpect(status().isOk());
        repay(TADESSE, KEY, PAY).andExpect(status().isOk()).andExpect(header().doesNotExist("Idempotent-Replayed"));
        verify(fineract, times(2)).repayLoan(eq(1L), any());

        repay(SAMUEL, "short", PAY).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_INVALID"));
    }

    @Test
    void loanApplicationIsIdempotentToo() throws Exception {
        when(fineract.getClient(2L)).thenReturn(client(2, 2, 1));
        when(fineract.getClientAccounts(2L)).thenReturn(new FineractClientAccounts(null));
        when(fineract.getLoanProduct(1L)).thenReturn(read("fineract/loanproduct-1-sbl.json", FineractLoanProduct.class));
        when(fineract.submitLoanApplication(any())).thenReturn(new FineractCommandResult(2L, 2L, 7L, 7L, Map.of()));
        stubLoan(7, 2, 100);
        String body = "{\"clientId\": 2, \"productId\": 1, \"principal\": 15000, \"numberOfRepayments\": 12}";

        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/loans").header("Authorization", bearer(SAMUEL)).header("Idempotency-Key", KEY)
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "http://localhost/api/loans/7"))
                    .andExpect(jsonPath("$.loanId").value(7));
        }
        verify(fineract, times(1)).submitLoanApplication(any());
    }

    private ResultActions repay(et.adey.mfi.security.Officer officer, String key, String body) throws Exception {
        var request = post("/api/loans/1/repayments").header("Authorization", bearer(officer))
                .contentType(MediaType.APPLICATION_JSON).content(body);
        if (key != null) {
            request.header("Idempotency-Key", key);
        }
        return mvc.perform(request);
    }
}
