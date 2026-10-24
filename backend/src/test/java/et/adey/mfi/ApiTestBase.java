package et.adey.mfi;

import et.adey.mfi.audit.AuditLogRepository;
import et.adey.mfi.auth.LoginAttemptLimiter;
import et.adey.mfi.auth.RefreshTokenRepository;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.idempotency.IdempotencyRepository;
import et.adey.mfi.mobilemoney.PaymentIntentRepository;
import et.adey.mfi.mobilemoney.TelePayClient;
import et.adey.mfi.mobilemoney.WebhookEventRepository;
import et.adey.mfi.reconciliation.ReconciliationFindingRepository;
import et.adey.mfi.reconciliation.ReconciliationRunRepository;
import et.adey.mfi.security.Officer;
import et.adey.mfi.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.when;

/**
 * Shared set-up for API tests: the whole application (real security, H2 database) with Fineract
 * mocked. All subclasses share one Spring context. State is wiped before each test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ApiTestBase {

    @Autowired
    protected MockMvc mvc;
    @Autowired
    protected TokenService tokens;
    @Autowired
    protected AuditLogRepository auditLog;
    @Autowired
    protected RefreshTokenRepository refreshTokens;
    @Autowired
    protected IdempotencyRepository idempotencyKeys;
    @Autowired
    protected LoginAttemptLimiter loginLimiter;

    @MockitoBean
    protected FineractClient fineract;
    /** The mobile money provider, mocked like Fineract (tests never make real HTTP calls). */
    @MockitoBean
    protected TelePayClient telePay;

    @Autowired
    protected PaymentIntentRepository paymentIntents;
    @Autowired
    protected WebhookEventRepository webhookEvents;
    @Autowired
    protected ReconciliationFindingRepository reconciliationFindings;
    @Autowired
    protected ReconciliationRunRepository reconciliationRuns;

    protected final ObjectMapper json = TestJson.mapper();

    @BeforeEach
    void resetState() {
        auditLog.deleteAll();
        refreshTokens.deleteAll();
        idempotencyKeys.deleteAll();
        loginLimiter.reset();
        reconciliationFindings.deleteAll();
        reconciliationRuns.deleteAll();
        webhookEvents.deleteAll();
        paymentIntents.deleteAll();
    }

    protected String bearer(Officer officer) {
        return "Bearer " + tokens.issue(officer).value();
    }

    protected FineractClientData client(long id, long officeId, long staffId) {
        return json.readValue("""
                {"id": %d, "officeId": %d, "staffId": %d, "displayName": "Client %d", "mobileNo": "09110000%02d",
                 "active": true, "gender": {"id": 24, "name": "Female"}}
                """.formatted(id, officeId, staffId, id, id), FineractClientData.class);
    }

    /** A loan for LoanGuard / status read-backs: id, client, status. */
    protected void stubLoan(long loanId, long clientId, int statusId) {
        when(fineract.getLoanWithSchedule(loanId)).thenReturn(json.readValue("""
                {"id": %d, "accountNo": "00000000%d", "clientId": %d, "approvedPrincipal": 15000.0,
                 "status": {"id": %d, "value": "status %d"}}
                """.formatted(loanId, loanId, clientId, statusId, statusId), FineractLoan.class));
    }

    protected <T> T read(String path, Class<T> type) {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return json.readValue(in, type);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
