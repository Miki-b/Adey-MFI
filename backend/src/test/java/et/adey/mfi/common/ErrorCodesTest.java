package et.adey.mfi.common;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractUnavailableException;
import et.adey.mfi.fineract.model.FineractError;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static et.adey.mfi.TestOfficers.SAMUEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Every kind of error response carries a stable "code" the app can switch on. */
class ErrorCodesTest extends ApiTestBase {

    @Test
    void springBuiltInErrorsHaveCodes() throws Exception {
        String auth = bearer(SAMUEL);
        mvc.perform(post("/api/loans/schedule-preview").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
        mvc.perform(post("/api/loans/schedule-preview").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mvc.perform(get("/api/loans?status=SOMETIMES").header("Authorization", auth))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        mvc.perform(get("/api/clients?size=1000").header("Authorization", auth))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("size"));
        mvc.perform(get("/api/no-such-thing").header("Authorization", auth))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));
        mvc.perform(delete("/api/offices").header("Authorization", auth))
                .andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
        mvc.perform(post("/api/loans/schedule-preview").header("Authorization", auth)
                        .contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    void fineractAndUnexpectedErrorsHaveCodes() throws Exception {
        String auth = bearer(SAMUEL);
        when(fineract.getOffices()).thenThrow(new FineractUnavailableException("Connection refused", null));
        mvc.perform(get("/api/offices").header("Authorization", auth))
                .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("FINERACT_UNAVAILABLE"));

        when(fineract.getClient(99L)).thenThrow(new FineractApiException(404,
                new FineractError(null, "The requested resource is not available.", null, List.of())));
        mvc.perform(get("/api/clients/99").header("Authorization", auth))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("NOT_FOUND"));

        when(fineract.getLoanProducts()).thenThrow(new FineractApiException(500,
                new FineractError("NPE somewhere", "Internal error", null, List.of())));
        mvc.perform(get("/api/loan-products").header("Authorization", auth))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.code").value("FINERACT_ERROR"));

        when(fineract.getPaymentTypes()).thenThrow(new IllegalStateException("bug"));
        mvc.perform(get("/api/payment-types").header("Authorization", auth))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));

        when(fineract.searchClients(any(), any(), any(), anyInt(), anyInt())).thenThrow(new FineractApiException(400,
                new FineractError(null, "Validation errors exist.", null,
                        List.of(new FineractError.FieldError("displayName", "Bad value", null, "validation.msg.x")))));
        mvc.perform(get("/api/clients?search=x").header("Authorization", auth))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("FINERACT_VALIDATION"));
    }

    @Test
    void securityErrorsHaveCodes() throws Exception {
        mvc.perform(get("/api/offices"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
        mvc.perform(post("/api/loans/1/approve").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }
}
