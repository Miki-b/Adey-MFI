package et.adey.mfi.loan;

import et.adey.mfi.common.ConflictException;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractUnavailableException;
import et.adey.mfi.fineract.model.FineractError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static et.adey.mfi.TestOfficers.SAMUEL;
import static et.adey.mfi.TestOfficers.TADESSE;
import static et.adey.mfi.TestOfficers.as;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP layer of the loan endpoints with the REAL security filter chain, and the services replaced
 * by Mockito mocks. Checks status codes and ProblemDetail JSON. Requests are sent "as" an officer
 * with TestOfficers.as(...); security itself is tested in SecurityIntegrationTest.
 * (A @WebMvcTest slice would not load our SecurityConfig, so this is a @SpringBootTest.)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LoanControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private LoanLifecycleService lifecycle;

    @Test
    void invalidBodyGives400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/loans/schedule-preview").with(as(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId": 1, "productId": 1, "principal": -5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[?(@.field == 'principal')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'numberOfRepayments')]").exists());
    }

    @Test
    void fineractValidationErrorIsPassedThroughAs400() throws Exception {
        FineractError error = new FineractError(null, "Validation errors exist.", null, List.of(
                new FineractError.FieldError("principal",
                        "The parameter `principal` amount 90000.0 must be between 5000.00 and 50000.00 .", null, null)));
        when(loanService.previewSchedule(any(), any())).thenThrow(new FineractApiException(400, error));

        mvc.perform(post("/api/loans/schedule-preview").with(as(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId": 1, "productId": 1, "principal": 90000, "numberOfRepayments": 12}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(
                        "The parameter `principal` amount 90000.0 must be between 5000.00 and 50000.00 ."))
                .andExpect(jsonPath("$.errors[0].field").value("principal"));
    }

    @Test
    void wrongLoanStateGives422WithOurMessage() throws Exception {
        // Real Fineract answer to "disburse a pending loan" (HTTP 400 validation error).
        FineractError error = new FineractError(null, "Validation errors exist.",
                "validation.msg.validation.errors.exist", List.of(new FineractError.FieldError("id",
                "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state.", null,
                "error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state")));
        when(lifecycle.disburse(any(), anyLong(), any())).thenThrow(new FineractApiException(400, error));

        mvc.perform(post("/api/loans/2/disburse").with(as(TADESSE)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("LOAN_WRONG_STATE"))
                .andExpect(jsonPath("$.detail").value(
                        "This loan cannot be disbursed: only an approved loan that has not been disbursed yet can be paid out."));
    }

    @Test
    void fineractNotFoundGives404() throws Exception {
        FineractError error = new FineractError(null, "The requested resource is not available.", null, List.of(
                new FineractError.FieldError("id", "Client not found with valuer 999.", null, null)));
        when(loanService.getClientLoans(any(), anyLong())).thenThrow(new FineractApiException(404, error));

        mvc.perform(get("/api/clients/999/loans").with(as(SAMUEL)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"));
    }

    @Test
    void fineractDownGives503() throws Exception {
        when(loanService.getClientLoans(any(), anyLong()))
                .thenThrow(new FineractUnavailableException("Connection refused", null));

        mvc.perform(get("/api/clients/1/loans").with(as(SAMUEL)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Fineract unavailable"));
    }

    @Test
    void submitReturns201WithLocation() throws Exception {
        when(lifecycle.submit(any(), any())).thenReturn(new LoanStatusResponse(7L, "000000007",
                LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, "Submitted and pending approval"));

        mvc.perform(post("/api/loans").with(as(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId": 1, "productId": 1, "principal": 20000, "numberOfRepayments": 12}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/loans/7"))
                .andExpect(jsonPath("$.status").value("SUBMITTED_AND_PENDING_APPROVAL"));
    }

    @Test
    void openLoanRuleGives409WithCode() throws Exception {
        when(lifecycle.submit(any(), any())).thenThrow(new ConflictException("OPEN_LOAN_EXISTS",
                "Client 1 already has loan 000000001 (Active)."));

        mvc.perform(post("/api/loans").with(as(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId": 1, "productId": 1, "principal": 20000, "numberOfRepayments": 12}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPEN_LOAN_EXISTS"))
                .andExpect(jsonPath("$.detail").value("Client 1 already has loan 000000001 (Active)."));
    }

    @Test
    void repaymentAmountMustBePositive() throws Exception {
        mvc.perform(post("/api/loans/1/repayments").with(as(SAMUEL))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("amount"));
    }
}
