package et.adey.mfi.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import et.adey.mfi.TestJson;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditLogEntry;
import et.adey.mfi.audit.AuditLogRepository;
import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.auth.RefreshTokenRepository;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractAuthentication;
import et.adey.mfi.fineract.model.FineractClientAccounts;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractError;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import et.adey.mfi.fineract.model.FineractLoanTransaction;
import et.adey.mfi.fineract.model.FineractPage;
import et.adey.mfi.fineract.model.FineractPaymentType;
import et.adey.mfi.fineract.model.FineractRole;
import et.adey.mfi.fineract.model.FineractUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static et.adey.mfi.TestOfficers.HANA;
import static et.adey.mfi.TestOfficers.SAMUEL;
import static et.adey.mfi.TestOfficers.TADESSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The whole application with the real security chain (JWT verification, access rules) and the
 * real audit/refresh-token tables (H2), with only Fineract mocked.
 * Requests carry REAL signed tokens (TokenService), so signature/expiry checks are exercised.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private SecurityProperties props;
    @Autowired
    private AuditLogRepository auditLog;
    @Autowired
    private RefreshTokenRepository refreshTokens;

    @MockitoBean
    private FineractClient fineract;

    private final ObjectMapper json = TestJson.mapper();

    @Autowired
    private et.adey.mfi.auth.LoginAttemptLimiter loginLimiter;

    @BeforeEach
    void cleanDatabase() {
        auditLog.deleteAll();
        refreshTokens.deleteAll();
        loginLimiter.reset(); // the context is shared: earlier failed logins must not lock this class out
    }

    // ---- Tokens --------------------------------------------------------------------------

    @Test
    void noTokenGives401() throws Exception {
        mvc.perform(get("/api/offices"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    void validTokenIsAccepted() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("samuel"))
                .andExpect(jsonPath("$.staffId").value(1))
                .andExpect(jsonPath("$.officeId").value(2))
                .andExpect(jsonPath("$.roles[0]").value("OFFICER"));
    }

    @Test
    void expiredTokenGives401() throws Exception {
        // Issued 1 hour ago with a 15-minute lifetime.
        Clock anHourAgo = Clock.fixed(Instant.now().minus(Duration.ofHours(1)), ZoneOffset.UTC);
        TokenService oldTokens = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(key(props.jwt().secret()))),
                props, anHourAgo);
        String expired = oldTokens.issue(SAMUEL).value();

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithAnotherKeyGives401() throws Exception {
        JwtEncoder attacker = new NimbusJwtEncoder(new ImmutableSecret<>(key("attacker-secret-0123456789abcdef0123456789")));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.jwt().issuer()).subject("samuel")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(600))
                .claim("uid", 4).claim("staffId", 1).claim("officeId", 2).claim("roles", List.of("MANAGER"))
                .build();
        String forged = attacker.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + forged))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedTokenGives401() throws Exception {
        // Take a real officer token and promote it to MANAGER by editing the payload.
        String[] parts = tokenService.issue(SAMUEL).value().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8)
                .replace("\"OFFICER\"", "\"MANAGER\"");
        String tampered = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    // ---- Data scoping --------------------------------------------------------------------

    @Test
    void officerReadingAnotherOfficersClientGets403() throws Exception {
        when(fineract.getClient(4L)).thenReturn(client(4, 2, 2)); // Kidist: Hana's client

        mvc.perform(get("/api/clients/4").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Client 4 is not assigned to you."));
    }

    @Test
    void managerReadsAnyClientOfTheirOfficeButNotOtherOffices() throws Exception {
        when(fineract.getClient(4L)).thenReturn(client(4, 2, 2));
        when(fineract.getClient(8L)).thenReturn(client(8, 3, 4)); // Adama Branch

        mvc.perform(get("/api/clients/4").header("Authorization", bearer(TADESSE))).andExpect(status().isOk());
        mvc.perform(get("/api/clients/8").header("Authorization", bearer(TADESSE))).andExpect(status().isForbidden());
    }

    @Test
    void queryParametersCannotWidenTheScope() throws Exception {
        when(fineract.searchClients(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new FineractPage<>(0, List.of()));

        // officeId/staffId are not parameters any more; whatever is sent, the token decides.
        mvc.perform(get("/api/clients?officeId=3&staffId=4").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isOk());
        verify(fineract).searchClients(isNull(), eq(2L), eq(1L), eq(0), eq(20));

        mvc.perform(get("/api/clients?staffId=4").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isOk());
        verify(fineract).searchClients(isNull(), eq(2L), isNull(), eq(0), eq(20));
    }

    // ---- Roles ---------------------------------------------------------------------------

    @Test
    void officerTryingToApproveGets403AndIsAudited() throws Exception {
        mvc.perform(post("/api/loans/2/approve").header("Authorization", bearer(SAMUEL)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Only a branch manager can do this."));

        verify(fineract, never()).loanCommand(anyLong(), anyString(), any());
        assertThat(auditLog.findAll()).singleElement().satisfies(row -> {
            assertThat(row.getAction()).isEqualTo(AuditAction.LOAN_APPROVE);
            assertThat(row.getOutcome()).isEqualTo(AuditOutcome.DENIED);
            assertThat(row.getUsername()).isEqualTo("samuel");
            assertThat(row.getLoanId()).isEqualTo(2L);
        });
    }

    @Test
    void managerApprovingGets200() throws Exception {
        stubLoan(2, 3, 100);
        when(fineract.getClient(3L)).thenReturn(client(3, 2, 1));
        when(fineract.loanCommand(eq(2L), eq("approve"), any())).thenReturn(ok(2));
        stubLoan(2, 3, 200); // read back after the command

        mvc.perform(post("/api/loans/2/approve").header("Authorization", bearer(TADESSE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        assertThat(auditLog.findAll()).singleElement().satisfies(row -> {
            assertThat(row.getOutcome()).isEqualTo(AuditOutcome.SUCCESS);
            assertThat(row.getUsername()).isEqualTo("tadesse");
            assertThat(row.getClientId()).isEqualTo(3L);
        });
    }

    // ---- Login, refresh, logout ----------------------------------------------------------

    @Test
    void loginIssuesTokensWithOurClaims() throws Exception {
        stubFineractLogin();

        JsonNode body = login();

        String accessToken = body.path("accessToken").asString();
        JsonNode claims = json.readTree(Base64.getUrlDecoder().decode(accessToken.split("\\.")[1]));
        assertThat(claims.path("sub").asString()).isEqualTo("samuel");
        assertThat(claims.path("staffId").asLong()).isEqualTo(1);
        assertThat(claims.path("officeId").asLong()).isEqualTo(2);
        assertThat(claims.path("name").asString()).isEqualTo("Samuel Derese");
        assertThat(claims.path("roles").get(0).asString()).isEqualTo("OFFICER");
        assertThat(claims.path("exp").asLong() - claims.path("iat").asLong()).isEqualTo(15 * 60);
        // The refresh token is stored only as a hash.
        assertThat(refreshTokens.findAll()).singleElement()
                .satisfies(t -> assertThat(t.getTokenHash()).isNotEqualTo(body.path("refreshToken").asString()));
    }

    @Test
    void refreshRotatesAndReuseOfAnOldTokenRevokesTheFamily() throws Exception {
        stubFineractLogin();
        when(fineract.getUser(4L)).thenReturn(fineractUser());
        String first = login().path("refreshToken").asString();

        // 1st refresh: works, returns a NEW refresh token.
        String second = refresh(first).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String secondToken = json.readTree(second).path("refreshToken").asString();
        assertThat(secondToken).isNotEqualTo(first);

        // The old one is now used up: rejected...
        refresh(first).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
        // ...and because it was REUSED, the newer one of the same family is revoked too.
        refresh(secondToken).andExpect(status().isUnauthorized());
    }

    @Test
    void logoutRevokesTheRefreshToken() throws Exception {
        stubFineractLogin();
        String refreshToken = login().path("refreshToken").asString();

        mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\": \"" + refreshToken + "\"}"))
                .andExpect(status().isNoContent());

        refresh(refreshToken).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordGives401AndAdminAccountIsRefused() throws Exception {
        when(fineract.authenticate("samuel", "wrong"))
                .thenThrow(new FineractApiException(401, new FineractError(null, "Unauthenticated. Please login.", null, List.of())));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"samuel\", \"password\": \"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.detail").value("Invalid username or password."));

        // "mifos": correct password, but no staff link and no Adey role.
        when(fineract.authenticate("mifos", "password")).thenReturn(new FineractAuthentication("mifos", 1L, true,
                1L, "Head Office", null, null, List.of(new FineractRole(1L, "Super user", false)), false));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"mifos\", \"password\": \"password\"}"))
                .andExpect(status().isForbidden());
    }

    // ---- Audit ---------------------------------------------------------------------------

    @Test
    void everyStateChangingActionWritesOneAuditRow() throws Exception {
        // Apply (officer) -> approve, undo approval, reject (manager) on loan 7 of client 2;
        // disburse (manager) and repay (officer) on loan 1 of client 1; plus one refused application.
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));
        when(fineract.getClient(2L)).thenReturn(client(2, 2, 1));
        when(fineract.getClientAccounts(2L)).thenReturn(new FineractClientAccounts(null));
        when(fineract.getClientAccounts(1L)).thenReturn(read("fineract/client-1-accounts-active-loan.json",
                FineractClientAccounts.class));
        when(fineract.getLoanProduct(1L)).thenReturn(read("fineract/loanproduct-1-sbl.json", FineractLoanProduct.class));
        when(fineract.submitLoanApplication(any())).thenReturn(ok(7));
        when(fineract.loanCommand(anyLong(), anyString(), any())).thenReturn(ok(7));
        when(fineract.getPaymentTypes()).thenReturn(List.of(new FineractPaymentType(4L, "Cash", "Cash", true, 2, false)));
        when(fineract.repayLoan(eq(1L), any())).thenReturn(new FineractCommandResult(2L, 1L, 1L, 38L, Map.of()));
        when(fineract.getLoanTransaction(1L, 38L)).thenReturn(
                read("fineract/loan-1-transaction-4-partial-repayment.json", FineractLoanTransaction.class));
        stubLoan(7, 2, 100);
        stubLoan(1, 1, 200);

        String body = """
                {"clientId": %d, "productId": 1, "principal": 15000, "numberOfRepayments": 12}""";
        mvc.perform(post("/api/loans").header("Authorization", bearer(SAMUEL))
                .contentType(MediaType.APPLICATION_JSON).content(body.formatted(2))).andExpect(status().isCreated());
        mvc.perform(post("/api/loans").header("Authorization", bearer(SAMUEL))
                .contentType(MediaType.APPLICATION_JSON).content(body.formatted(1))).andExpect(status().isConflict());
        mvc.perform(post("/api/loans/7/approve").header("Authorization", bearer(TADESSE))).andExpect(status().isOk());
        mvc.perform(post("/api/loans/7/undo-approval").header("Authorization", bearer(TADESSE))).andExpect(status().isOk());
        mvc.perform(post("/api/loans/7/reject").header("Authorization", bearer(TADESSE))).andExpect(status().isOk());
        mvc.perform(post("/api/loans/1/disburse").header("Authorization", bearer(TADESSE))
                .contentType(MediaType.APPLICATION_JSON).content("{\"paymentTypeId\": 4}")).andExpect(status().isOk());
        mvc.perform(post("/api/loans/1/repayments").header("Authorization", bearer(SAMUEL))
                .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 1000, \"paymentTypeId\": 4}"))
                .andExpect(status().isOk());

        List<AuditLogEntry> rows = auditLog.findAll(Sort.by("id"));
        assertThat(rows).extracting(AuditLogEntry::getAction).containsExactly(
                AuditAction.LOAN_APPLY, AuditAction.LOAN_APPLY, AuditAction.LOAN_APPROVE,
                AuditAction.LOAN_UNDO_APPROVAL, AuditAction.LOAN_REJECT, AuditAction.LOAN_DISBURSE,
                AuditAction.LOAN_REPAY);
        assertThat(rows).extracting(AuditLogEntry::getOutcome).containsExactly(
                AuditOutcome.SUCCESS, AuditOutcome.REJECTED, AuditOutcome.SUCCESS, AuditOutcome.SUCCESS,
                AuditOutcome.SUCCESS, AuditOutcome.SUCCESS, AuditOutcome.SUCCESS);
        assertThat(rows.get(0).getLoanId()).isEqualTo(7L);
        assertThat(rows.get(1).getDetail()).contains("already has loan");
        assertThat(rows.get(6).getAmount()).isEqualByComparingTo("1000");
        assertThat(rows).allSatisfy(r -> {
            assertThat(r.getOccurredAt()).isNotNull();
            assertThat(r.getOfficeId()).isEqualTo(2L);
        });
        assertThat(rows.get(2).getUsername()).isEqualTo("tadesse");
        assertThat(rows.get(6).getStaffId()).isEqualTo(1L);
    }

    @Test
    void hanaCannotSeeSamuelsClientLoans() throws Exception {
        when(fineract.getClient(1L)).thenReturn(client(1, 2, 1));
        mvc.perform(get("/api/clients/1/loans").header("Authorization", bearer(HANA)))
                .andExpect(status().isForbidden());
        verify(fineract, never()).getClientAccounts(anyLong());
    }

    // ---- CORS ----------------------------------------------------------------------------

    @Test
    void corsAllowsOnlyConfiguredOrigins() throws Exception {
        // application-test.yml allows http://localhost:[*]
        mvc.perform(options("/api/offices")
                        .header("Origin", "http://localhost:5555").header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header()
                        .string("Access-Control-Allow-Origin", "http://localhost:5555"));
        mvc.perform(options("/api/offices")
                        .header("Origin", "https://evil.example").header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    // ---- Helpers -------------------------------------------------------------------------

    private String bearer(Officer officer) {
        return "Bearer " + tokenService.issue(officer).value();
    }

    private static SecretKeySpec key(String secret) {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private FineractClientData client(long id, long officeId, long staffId) {
        return json.readValue("""
                {"id": %d, "officeId": %d, "staffId": %d, "displayName": "Client %d", "active": true}
                """.formatted(id, officeId, staffId, id), FineractClientData.class);
    }

    private void stubLoan(long loanId, long clientId, int statusId) {
        when(fineract.getLoanWithSchedule(loanId)).thenReturn(json.readValue("""
                {"id": %d, "accountNo": "00000000%d", "clientId": %d, "approvedPrincipal": 15000.0,
                 "status": {"id": %d, "value": "status %d"}}
                """.formatted(loanId, loanId, clientId, statusId, statusId), FineractLoan.class));
    }

    private static FineractCommandResult ok(long id) {
        return new FineractCommandResult(2L, 1L, id, id, Map.of());
    }

    private void stubFineractLogin() {
        when(fineract.authenticate("samuel", "Adey#Loan2026")).thenReturn(new FineractAuthentication("samuel", 4L,
                true, 2L, "Bishoftu Branch", 1L, "Derese, Samuel ",
                List.of(new FineractRole(3L, "Loan Officer", false)), false));
    }

    private FineractUser fineractUser() {
        return new FineractUser(4L, "samuel", "Samuel", "Derese", 2L, "Bishoftu Branch",
                new FineractUser.StaffRef(1L, "Derese, Samuel ", 2L), List.of(new FineractRole(3L, "Loan Officer", false)));
    }

    private JsonNode login() throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"samuel\", \"password\": \"Adey#Loan2026\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\": \"" + refreshToken + "\"}"));
    }

    private <T> T read(String path, Class<T> type) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return json.readValue(in, type);
        }
    }

    private static int anyInt() {
        return org.mockito.ArgumentMatchers.anyInt();
    }
}
