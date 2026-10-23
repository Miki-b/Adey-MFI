package et.adey.mfi.loan;

import et.adey.mfi.TestJson;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditLogEntry;
import et.adey.mfi.audit.AuditLogRepository;
import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.common.BadRequestException;
import et.adey.mfi.common.ConflictException;
import et.adey.mfi.common.FineractDates;
import et.adey.mfi.config.FineractConfig;
import et.adey.mfi.config.FineractProperties;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.FineractErrorCodes;
import et.adey.mfi.paymenttype.PaymentTypeService;
import et.adey.mfi.security.AccessPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;

import static et.adey.mfi.TestOfficers.HANA;
import static et.adey.mfi.TestOfficers.SAMUEL;
import static et.adey.mfi.TestOfficers.TADESSE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * LoanLifecycleService + the real FineractClient against a fake Fineract (MockRestServiceServer).
 * Each test lists the exact calls Fineract should receive, in order; an unexpected call fails the
 * test. That is how we prove e.g. that a refused officer never reaches Fineract.
 * The audit repository is a Mockito mock so we can check which row was written.
 */
class LoanLifecycleServiceTest {

    private static final String BASE = "https://fineract.test/fineract-provider/api/v1";

    private MockRestServiceServer server;
    private LoanLifecycleService service;
    private AuditLogRepository auditRepository;

    @BeforeEach
    void setUp() {
        FineractProperties props = new FineractProperties(BASE, "default", "mifos", "password",
                Duration.ofSeconds(5), Duration.ofSeconds(30), false);
        RestClient.Builder builder = FineractConfig.applyDefaults(RestClient.builder(), props);
        server = MockRestServiceServer.bindTo(builder).build();
        FineractClient client = new FineractClient(builder.build(), TestJson.mapper());
        AccessPolicy access = new AccessPolicy();
        auditRepository = mock(AuditLogRepository.class);
        service = new LoanLifecycleService(client, new PaymentTypeService(client), new LoanGuard(client, access),
                access, new AuditService(auditRepository, Clock.systemUTC()));
    }

    // ---- Business rule: one loan in progress per client ----------------------------------

    @Test
    void submitIsRefusedWhenClientHasActiveLoan() {
        expectClient(1, 1);
        // Real GET /clients/1/accounts while loan 000000001 is Active.
        server.expect(requestTo(BASE + "/clients/1/accounts"))
                .andRespond(json("fineract/client-1-accounts-active-loan.json"));
        // No POST /loans is expected: the rule must stop us before Fineract is asked.

        assertThatThrownBy(() -> service.submit(SAMUEL, application(1L)))
                .isInstanceOfSatisfying(ConflictException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("OPEN_LOAN_EXISTS");
                    assertThat(ex.getMessage()).contains("000000001").contains("Active");
                });
        server.verify();
        assertThat(lastAudit().getOutcome()).isEqualTo(AuditOutcome.REJECTED);
        assertThat(lastAudit().getAction()).isEqualTo(AuditAction.LOAN_APPLY);
    }

    @Test
    void pendingAndApprovedLoansAlsoBlockANewApplication() {
        for (int statusId : new int[]{100, 200}) {
            server.reset();
            expectClient(1, 1);
            server.expect(requestTo(BASE + "/clients/1/accounts"))
                    .andRespond(withSuccess("""
                            {"loanAccounts": [{"id": 9, "accountNo": "000000009", "status": {"id": %d, "value": "x"}}]}
                            """.formatted(statusId), MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> service.submit(SAMUEL, application(1L))).isInstanceOf(ConflictException.class);
            server.verify();
        }
    }

    @Test
    void closedOrRejectedLoansDoNotBlockAndOfficerIsTakenFromTheToken() {
        expectClient(1, 1);
        server.expect(requestTo(BASE + "/clients/1/accounts"))
                .andRespond(withSuccess("""
                        {"loanAccounts": [
                          {"id": 5, "accountNo": "000000005", "status": {"id": 600, "value": "Closed (obligations met)"}},
                          {"id": 6, "accountNo": "000000006", "status": {"id": 500, "value": "Rejected"}}]}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loanproducts/1"))
                .andRespond(json("fineract/loanproduct-1-sbl.json"));
        server.expect(requestTo(BASE + "/loans"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.clientId").value(1))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.loanOfficerId").value(1)) // Samuel's staffId, from the token
                .andExpect(jsonPath("$.principal").value(20000))
                .andExpect(jsonPath("$.numberOfRepayments").value(12))
                .andExpect(jsonPath("$.loanTermFrequency").value(12))
                .andExpect(jsonPath("$.loanTermFrequencyType").value(2))
                .andExpect(jsonPath("$.interestRatePerPeriod").value(18.0))
                .andExpect(jsonPath("$.amortizationType").value(1))
                .andExpect(jsonPath("$.interestType").value(0))
                .andExpect(jsonPath("$.transactionProcessingStrategyCode").value("mifos-standard-strategy"))
                .andExpect(jsonPath("$.expectedDisbursementDate").value("01 November 2026"))
                .andExpect(jsonPath("$.externalId").doesNotExist())
                .andExpect(jsonPath("$.dateFormat").value("dd MMMM yyyy"))
                .andExpect(jsonPath("$.locale").value("en"))
                .andRespond(withSuccess("""
                        {"officeId": 2, "clientId": 1, "loanId": 7, "resourceId": 7}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/7?associations=repaymentSchedule"))
                .andRespond(loanResponse(7, 1, 100, "Submitted and pending approval"));

        LoanStatusResponse result = service.submit(SAMUEL, new LoanApplicationRequest(1L, 1L,
                new BigDecimal("20000"), 12, LocalDate.of(2026, 11, 1)));

        server.verify();
        assertThat(result.loanId()).isEqualTo(7L);
        assertThat(result.status()).isEqualTo(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        AuditLogEntry audit = lastAudit();
        assertThat(audit.getOutcome()).isEqualTo(AuditOutcome.SUCCESS);
        assertThat(audit.getLoanId()).isEqualTo(7L);
        assertThat(audit.getClientId()).isEqualTo(1L);
        assertThat(audit.getAmount()).isEqualByComparingTo("20000");
        assertThat(audit.getUsername()).isEqualTo("samuel");
    }

    @Test
    void officerCannotApplyForAnotherOfficersClient() {
        expectClient(4, 2); // Kidist: Hana's client
        // No accounts / product / POST calls expected.

        assertThatThrownBy(() -> service.submit(SAMUEL, application(4L))).isInstanceOf(AccessDeniedException.class);
        server.verify();
        assertThat(lastAudit().getOutcome()).isEqualTo(AuditOutcome.DENIED);
    }

    // ---- Manager-only commands -----------------------------------------------------------

    @Test
    void officerCannotApproveAndFineractIsNeverAsked() {
        // No expectations at all: the role check comes first.
        assertThatThrownBy(() -> service.approve(SAMUEL, 2L, null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("branch manager");
        server.verify();
        AuditLogEntry audit = lastAudit();
        assertThat(audit.getAction()).isEqualTo(AuditAction.LOAN_APPROVE);
        assertThat(audit.getOutcome()).isEqualTo(AuditOutcome.DENIED);
        assertThat(audit.getLoanId()).isEqualTo(2L);
    }

    @Test
    void managerApproveWithoutBodyApprovesTodayForTheRequestedAmount() {
        String today = FineractDates.format(FineractDates.today());
        expectLoanAndClient(1, 1, 1);
        server.expect(requestTo(BASE + "/loans/1?command=approve"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.approvedOnDate").value(today))
                .andExpect(jsonPath("$.approvedLoanAmount").doesNotExist())
                .andExpect(jsonPath("$.dateFormat").value("dd MMMM yyyy"))
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1?associations=repaymentSchedule"))
                .andRespond(loanResponse(1, 1, 200, "Approved"));

        LoanStatusResponse result = service.approve(TADESSE, 1L, null);

        server.verify();
        assertThat(result.status()).isEqualTo(LoanStatus.APPROVED);
        assertThat(lastAudit().getOutcome()).isEqualTo(AuditOutcome.SUCCESS);
        assertThat(lastAudit().getUsername()).isEqualTo("tadesse");
    }

    @Test
    void rejectAndUndoApprovalSendTheirCommands() {
        expectLoanAndClient(1, 1, 1);
        server.expect(requestTo(BASE + "/loans/1?command=undoApproval"))
                .andExpect(jsonPath("$.note").value("Wrong amount"))
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1?associations=repaymentSchedule"))
                .andRespond(loanResponse(1, 1, 100, "Submitted and pending approval"));
        expectLoanAndClient(1, 1, 1);
        server.expect(requestTo(BASE + "/loans/1?command=reject"))
                .andExpect(jsonPath("$.rejectedOnDate").value("24 October 2026"))
                .andExpect(jsonPath("$.note").value("Insufficient cash flow"))
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1?associations=repaymentSchedule"))
                .andRespond(loanResponse(1, 1, 500, "Rejected"));

        assertThat(service.undoApproval(TADESSE, 1L, new UndoApprovalRequest("Wrong amount")).status())
                .isEqualTo(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        assertThat(service.reject(TADESSE, 1L,
                new RejectLoanRequest(LocalDate.of(2026, 10, 24), "Insufficient cash flow")).status())
                .isEqualTo(LoanStatus.REJECTED);
        server.verify();
    }

    @Test
    void disburseInCashIsAccepted() {
        expectLoanAndClient(1, 1, 1);
        server.expect(requestTo(BASE + "/paymenttypes"))
                .andRespond(json("fineract/paymenttypes-with-cash.json"));
        server.expect(requestTo(BASE + "/loans/1?command=disburse"))
                .andExpect(jsonPath("$.actualDisbursementDate").value("23 October 2026"))
                .andExpect(jsonPath("$.paymentTypeId").value(4))
                .andExpect(jsonPath("$.transactionAmount").doesNotExist())
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1?associations=repaymentSchedule"))
                .andRespond(loanResponse(1, 1, 300, "Active"));

        LoanStatusResponse result = service.disburse(TADESSE, 1L,
                new DisburseLoanRequest(LocalDate.of(2026, 10, 23), 4L, null));

        server.verify();
        assertThat(result.status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(lastAudit().getAction()).isEqualTo(AuditAction.LOAN_DISBURSE);
        assertThat(lastAudit().getAmount()).isEqualByComparingTo("20000"); // the approved principal
    }

    @Test
    void systemDefinedPaymentTypeIsRefusedBeforeCallingFineract() {
        expectLoanAndClient(1, 1, 1);
        server.expect(requestTo(BASE + "/paymenttypes"))
                .andRespond(json("fineract/paymenttypes-with-cash.json"));
        // No disburse call expected: id 2 is Fineract's internal "Repayment Adjustment Chargeback".

        assertThatThrownBy(() -> service.disburse(TADESSE, 1L, new DisburseLoanRequest(null, 2L, null)))
                .isInstanceOfSatisfying(BadRequestException.class,
                        ex -> assertThat(ex.getField()).isEqualTo("paymentTypeId"));
        server.verify();
        assertThat(lastAudit().getOutcome()).isEqualTo(AuditOutcome.REJECTED);
    }

    @Test
    void fineractStateErrorIsPassedThroughAndAuditedAsRejected() {
        expectLoanAndClient(2, 3, 1);
        server.expect(requestTo(BASE + "/loans/2?command=disburse"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                        {"defaultUserMessage": "Validation errors exist.",
                         "errors": [{"parameterName": "id",
                                     "defaultUserMessage": "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state.",
                                     "userMessageGlobalisationCode": "error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state"}]}
                        """));

        assertThatThrownBy(() -> service.disburse(TADESSE, 2L, null))
                .isInstanceOfSatisfying(FineractApiException.class, ex -> {
                    assertThat(ex.getFineractStatus()).isEqualTo(400);
                    assertThat(FineractErrorCodes.wrongStateMessage(ex.getError())).isPresent();
                });
        assertThat(lastAudit().getOutcome()).isEqualTo(AuditOutcome.REJECTED);
        assertThat(lastAudit().getClientId()).isEqualTo(3L);
    }

    @Test
    void unknownErrorCodeIsNotTreatedAsWrongState() {
        expectLoanAndClient(1, 1, 1);
        server.expect(requestTo(BASE + "/loans/1?command=approve"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                        {"defaultUserMessage": "Validation errors exist.",
                         "errors": [{"parameterName": "approvedOnDate", "defaultUserMessage": "The parameter approvedOnDate is mandatory.",
                                     "userMessageGlobalisationCode": "validation.msg.loan.approvedOnDate.cannot.be.blank"}]}
                        """));

        assertThatThrownBy(() -> service.approve(TADESSE, 1L, null))
                .isInstanceOfSatisfying(FineractApiException.class, ex ->
                        assertThat(FineractErrorCodes.wrongStateMessage(ex.getError())).isEmpty());
    }

    // ---- Repayment (officers and managers) -----------------------------------------------

    @Test
    void repaymentReturnsTheStoredTransactionWithItsSplit() {
        expectLoanAndClient(1, 1, 1);
        server.expect(requestTo(BASE + "/paymenttypes"))
                .andRespond(json("fineract/paymenttypes-with-cash.json"));
        server.expect(requestTo(BASE + "/loans/1/transactions?command=repayment"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.transactionAmount").value(1000))
                .andExpect(jsonPath("$.transactionDate").value("23 October 2026"))
                .andExpect(jsonPath("$.paymentTypeId").value(1))
                .andExpect(jsonPath("$.note").value("Part payment"))
                .andExpect(jsonPath("$.locale").value("en"))
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 4}", MediaType.APPLICATION_JSON));
        // Real transaction 4 as stored by Fineract.
        server.expect(requestTo(BASE + "/loans/1/transactions/4"))
                .andRespond(json("fineract/loan-1-transaction-4-partial-repayment.json"));

        LoanTransactionResponse tx = service.repay(SAMUEL, 1L, new RepaymentRequest(LocalDate.of(2026, 10, 23),
                new BigDecimal("1000"), 1L, "Part payment"));

        server.verify();
        assertThat(tx.id()).isEqualTo(4L);
        assertThat(tx.interestPortion()).isEqualTo(new BigDecimal("277.00"));
        assertThat(tx.principalPortion()).isEqualTo(new BigDecimal("723.00"));
        assertThat(tx.outstandingBalanceAfter()).isEqualTo(new BigDecimal("17743.40"));
        AuditLogEntry audit = lastAudit();
        assertThat(audit.getAction()).isEqualTo(AuditAction.LOAN_REPAY);
        assertThat(audit.getAmount()).isEqualByComparingTo("1000");
        assertThat(audit.getOutcome()).isEqualTo(AuditOutcome.SUCCESS);
    }

    @Test
    void officerCannotRepayAnotherOfficersLoan() {
        expectLoanAndClient(1, 1, 1); // Yohannes: Samuel's client
        // No payment types / repayment calls expected.

        assertThatThrownBy(() -> service.repay(HANA, 1L,
                new RepaymentRequest(null, new BigDecimal("100"), null, null)))
                .isInstanceOf(AccessDeniedException.class);
        server.verify();
        assertThat(lastAudit().getOutcome()).isEqualTo(AuditOutcome.DENIED);
        assertThat(lastAudit().getClientId()).isEqualTo(1L); // logged with the client it concerned
    }

    // ---- Helpers -------------------------------------------------------------------------

    private AuditLogEntry lastAudit() {
        ArgumentCaptor<AuditLogEntry> captor = ArgumentCaptor.forClass(AuditLogEntry.class);
        verify(auditRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    private void expectClient(long clientId, long staffId) {
        server.expect(requestTo(BASE + "/clients/" + clientId))
                .andRespond(withSuccess("""
                        {"id": %d, "officeId": 2, "staffId": %d, "displayName": "Test Client"}
                        """.formatted(clientId, staffId), MediaType.APPLICATION_JSON));
    }

    /** What LoanGuard.requireLoan asks for: the loan, then its client. */
    private void expectLoanAndClient(long loanId, long clientId, long staffId) {
        server.expect(requestTo(BASE + "/loans/" + loanId + "?associations=repaymentSchedule"))
                .andRespond(withSuccess("""
                        {"id": %d, "accountNo": "00000000%d", "clientId": %d, "approvedPrincipal": 20000.0,
                         "status": {"id": 200, "value": "Approved"}}
                        """.formatted(loanId, loanId, clientId), MediaType.APPLICATION_JSON));
        expectClient(clientId, staffId);
    }

    private static LoanApplicationRequest application(long clientId) {
        return new LoanApplicationRequest(clientId, 1L, new BigDecimal("20000"), 12, null);
    }

    private static ResponseCreator json(String path) {
        return withSuccess(new ClassPathResource(path), MediaType.APPLICATION_JSON);
    }

    private static ResponseCreator loanResponse(long loanId, long clientId, int statusId, String label) {
        return withSuccess("""
                {"id": %d, "accountNo": "00000000%d", "clientId": %d, "status": {"id": %d, "value": "%s"}}
                """.formatted(loanId, loanId, clientId, statusId, label), MediaType.APPLICATION_JSON);
    }
}
