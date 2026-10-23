package et.adey.mfi.auth;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditLogEntry;
import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.model.FineractAuthentication;
import et.adey.mfi.fineract.model.FineractError;
import et.adey.mfi.fineract.model.FineractRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Lockout after 5 bad logins, through the HTTP API, and the audit rows it leaves. */
class LoginRateLimitIntegrationTest extends ApiTestBase {

    @BeforeEach
    void stubFineractLogin() {
        when(fineract.authenticate(eq("dawit"), anyString()))
                .thenThrow(new FineractApiException(401, new FineractError(null, "Unauthenticated. Please login.", null, List.of())));
        when(fineract.authenticate("samuel", "Adey#Loan2026")).thenReturn(new FineractAuthentication("samuel", 4L,
                true, 2L, "Bishoftu Branch", 1L, "Derese, Samuel ",
                List.of(new FineractRole(3L, "Loan Officer", false)), false));
        when(fineract.authenticate("samuel", "wrong"))
                .thenThrow(new FineractApiException(401, new FineractError(null, "Unauthenticated. Please login.", null, List.of())));
    }

    @Test
    void fiveFailuresThen429WithRetryAfterEvenForTheRightPassword() throws Exception {
        for (int i = 0; i < 5; i++) {
            login("dawit", "wrong", "10.0.0.1").andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
        login("dawit", "Adey#Loan2026", "10.0.0.1")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.code").value("TOO_MANY_LOGIN_ATTEMPTS"))
                .andExpect(jsonPath("$.retryAfterSeconds").isNumber());
        // While locked, Fineract is not even asked.
        verify(fineract, times(5)).authenticate(eq("dawit"), anyString());

        List<AuditLogEntry> rows = auditLog.findAll(Sort.by("id"));
        assertThat(rows).allSatisfy(r -> {
            assertThat(r.getAction()).isEqualTo(AuditAction.LOGIN);
            assertThat(r.getUsername()).isEqualTo("dawit");
            assertThat(r.getDetail()).startsWith("ip=10.0.0.1");
        });
        assertThat(rows).extracting(AuditLogEntry::getOutcome).containsExactly(
                AuditOutcome.DENIED, AuditOutcome.DENIED, AuditOutcome.DENIED, AuditOutcome.DENIED,
                AuditOutcome.DENIED, AuditOutcome.LOCKED);
    }

    @Test
    void successIsAuditedAndClearsTheUsernamesFailures() throws Exception {
        for (int i = 0; i < 4; i++) {
            login("samuel", "wrong", "10.0.0.2").andExpect(status().isUnauthorized());
        }
        login("samuel", "Adey#Loan2026", "10.0.0.2").andExpect(status().isOk());
        login("samuel", "wrong", "10.0.0.3").andExpect(status().isUnauthorized()); // not locked

        AuditLogEntry success = auditLog.findAll().stream()
                .filter(r -> r.getOutcome() == AuditOutcome.SUCCESS).findFirst().orElseThrow();
        assertThat(success.getUserId()).isEqualTo(4L);
        assertThat(success.getStaffId()).isEqualTo(1L);
        assertThat(success.getOfficeId()).isEqualTo(2L);
    }

    private ResultActions login(String username, String password, String ip) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\": \"%s\", \"password\": \"%s\"}".formatted(username, password))
                .with(request -> {
                    request.setRemoteAddr(ip);
                    return request;
                }));
    }
}
