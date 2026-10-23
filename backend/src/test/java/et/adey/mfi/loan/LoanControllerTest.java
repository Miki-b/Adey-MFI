package et.adey.mfi.loan;

import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractUnavailableException;
import et.adey.mfi.fineract.model.FineractError;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @WebMvcTest starts only the web layer (controllers + GlobalExceptionHandler), with the
 * service replaced by a Mockito mock. Checks HTTP status codes and the ProblemDetail JSON.
 */
@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LoanService loanService;

    @Test
    void invalidBodyGives400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/loans/schedule-preview")
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
        when(loanService.previewSchedule(any())).thenThrow(new FineractApiException(400, error));

        mvc.perform(post("/api/loans/schedule-preview")
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
    void fineractNotFoundGives404() throws Exception {
        FineractError error = new FineractError(null, "The requested resource is not available.", null, List.of(
                new FineractError.FieldError("id", "Client not found with valuer 999.", null, null)));
        when(loanService.getClientLoans(anyLong())).thenThrow(new FineractApiException(404, error));

        mvc.perform(get("/api/clients/999/loans"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not found"));
    }

    @Test
    void fineractDownGives503() throws Exception {
        when(loanService.getClientLoans(anyLong()))
                .thenThrow(new FineractUnavailableException("Connection refused", null));

        mvc.perform(get("/api/clients/1/loans"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Fineract unavailable"));
    }
}
