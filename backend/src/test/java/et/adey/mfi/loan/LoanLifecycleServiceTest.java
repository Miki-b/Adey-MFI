package et.adey.mfi.loan;

import et.adey.mfi.TestJson;
import et.adey.mfi.common.ConflictException;
import et.adey.mfi.common.FineractDates;
import et.adey.mfi.config.FineractConfig;
import et.adey.mfi.config.FineractProperties;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * LoanLifecycleService + the real FineractClient against a fake Fineract (MockRestServiceServer).
 * Each test lists the exact calls Fineract should receive; an unexpected call fails the test.
 */
class LoanLifecycleServiceTest {

    private static final String BASE = "https://fineract.test/fineract-provider/api/v1";

    private MockRestServiceServer server;
    private LoanLifecycleService service;

    @BeforeEach
    void setUp() {
        FineractProperties props = new FineractProperties(BASE, "default", "mifos", "password",
                Duration.ofSeconds(5), Duration.ofSeconds(30), false);
        RestClient.Builder builder = FineractConfig.applyDefaults(RestClient.builder(), props);
        server = MockRestServiceServer.bindTo(builder).build();
        service = new LoanLifecycleService(new FineractClient(builder.build(), TestJson.mapper()));
    }

    // ---- Business rule: one loan in progress per client ----------------------------------

    @Test
    void submitIsRefusedWhenClientHasActiveLoan() {
        // Real GET /clients/1/accounts while loan 000000001 is Active.
        server.expect(requestTo(BASE + "/clients/1/accounts"))
                .andRespond(json("fineract/client-1-accounts-active-loan.json"));
        // No POST /loans is expected: the rule must stop us before Fineract is asked.

        assertThatThrownBy(() -> service.submit(application(1L)))
                .isInstanceOfSatisfying(ConflictException.class, ex -> {
                    assertThat(ex.getCode()).isEqualTo("OPEN_LOAN_EXISTS");
                    assertThat(ex.getMessage()).contains("000000001").contains("Active");
                });
        server.verify();
    }

    @Test
    void pendingAndApprovedLoansAlsoBlockANewApplication() {
        for (int statusId : new int[]{100, 200}) {
            server.reset();
            server.expect(requestTo(BASE + "/clients/1/accounts"))
                    .andRespond(withSuccess("""
                            {"loanAccounts": [{"id": 9, "accountNo": "000000009", "status": {"id": %d, "value": "x"}}]}
                            """.formatted(statusId), MediaType.APPLICATION_JSON));

            assertThatThrownBy(() -> service.submit(application(1L))).isInstanceOf(ConflictException.class);
            server.verify();
        }
    }

    @Test
    void closedOrRejectedLoansDoNotBlockAndApplicationIsSentWithProductTerms() {
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
                .andExpect(jsonPath("$.loanOfficerId").value(1))
                .andExpect(jsonPath("$.principal").value(20000))
                .andExpect(jsonPath("$.numberOfRepayments").value(12))
                .andExpect(jsonPath("$.loanTermFrequency").value(12))
                .andExpect(jsonPath("$.loanTermFrequencyType").value(2))
                .andExpect(jsonPath("$.interestRatePerPeriod").value(18.0))
                .andExpect(jsonPath("$.amortizationType").value(1))
                .andExpect(jsonPath("$.interestType").value(0))
                .andExpect(jsonPath("$.transactionProcessingStrategyCode").value("mifos-standard-strategy"))
                .andExpect(jsonPath("$.expectedDisbursementDate").value("01 November 2026"))
                .andExpect(jsonPath("$.dateFormat").value("dd MMMM yyyy"))
                .andExpect(jsonPath("$.locale").value("en"))
                .andRespond(withSuccess("""
                        {"officeId": 2, "clientId": 1, "loanId": 7, "resourceId": 7}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/7?associations=repaymentSchedule"))
                .andRespond(withSuccess("""
                        {"id": 7, "accountNo": "000000007",
                         "status": {"id": 100, "code": "loanStatusType.submitted.and.pending.approval",
                                    "value": "Submitted and pending approval"}}
                        """, MediaType.APPLICATION_JSON));

        LoanStatusResponse result = service.submit(new LoanApplicationRequest(1L, 1L, new BigDecimal("20000"), 12,
                LocalDate.of(2026, 11, 1), 1L));

        server.verify();
        assertThat(result.loanId()).isEqualTo(7L);
        assertThat(result.status()).isEqualTo(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        assertThat(result.statusLabel()).isEqualTo("Submitted and pending approval");
    }

    // ---- Lifecycle commands --------------------------------------------------------------

    @Test
    void approveWithoutBodyApprovesTodayForTheRequestedAmount() {
        String today = FineractDates.format(FineractDates.today());
        server.expect(requestTo(BASE + "/loans/1?command=approve"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.approvedOnDate").value(today))
                .andExpect(jsonPath("$.approvedLoanAmount").doesNotExist())
                .andExpect(jsonPath("$.dateFormat").value("dd MMMM yyyy"))
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1?associations=repaymentSchedule"))
                .andRespond(statusResponse(200, "Approved"));

        LoanStatusResponse result = service.approve(1L, null);

        server.verify();
        assertThat(result.status()).isEqualTo(LoanStatus.APPROVED);
    }

    @Test
    void rejectAndUndoApprovalSendTheirCommands() {
        server.expect(requestTo(BASE + "/loans/1?command=undoApproval"))
                .andExpect(jsonPath("$.note").value("Wrong amount"))
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1?associations=repaymentSchedule"))
                .andRespond(statusResponse(100, "Submitted and pending approval"));
        server.expect(requestTo(BASE + "/loans/1?command=reject"))
                .andExpect(jsonPath("$.rejectedOnDate").value("24 October 2026"))
                .andExpect(jsonPath("$.note").value("Insufficient cash flow"))
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1?associations=repaymentSchedule"))
                .andRespond(statusResponse(500, "Rejected"));

        assertThat(service.undoApproval(1L, new UndoApprovalRequest("Wrong amount")).status())
                .isEqualTo(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        assertThat(service.reject(1L, new RejectLoanRequest(LocalDate.of(2026, 10, 24), "Insufficient cash flow")).status())
                .isEqualTo(LoanStatus.REJECTED);
        server.verify();
    }

    @Test
    void disburseSendsDateAndPaymentType() {
        server.expect(requestTo(BASE + "/loans/1?command=disburse"))
                .andExpect(jsonPath("$.actualDisbursementDate").value("23 October 2026"))
                .andExpect(jsonPath("$.paymentTypeId").value(1))
                .andExpect(jsonPath("$.transactionAmount").doesNotExist())
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 1}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1?associations=repaymentSchedule"))
                .andRespond(statusResponse(300, "Active"));

        LoanStatusResponse result = service.disburse(1L,
                new DisburseLoanRequest(LocalDate.of(2026, 10, 23), 1L, null));

        server.verify();
        assertThat(result.status()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void fineractStateErrorIsPassedThrough() {
        server.expect(requestTo(BASE + "/loans/1?command=disburse"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                        {"defaultUserMessage": "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state.",
                         "errors": [{"parameterName": "id",
                                     "defaultUserMessage": "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state."}]}
                        """));

        assertThatThrownBy(() -> service.disburse(1L, null))
                .isInstanceOfSatisfying(FineractApiException.class,
                        ex -> assertThat(ex.getFineractStatus()).isEqualTo(400));
    }

    // ---- Repayment -----------------------------------------------------------------------

    @Test
    void repaymentReturnsTheStoredTransactionWithItsSplit() {
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

        LoanTransactionResponse tx = service.repay(1L, new RepaymentRequest(LocalDate.of(2026, 10, 23),
                new BigDecimal("1000"), 1L, "Part payment"));

        server.verify();
        assertThat(tx.id()).isEqualTo(4L);
        assertThat(tx.interestPortion()).isEqualTo(new BigDecimal("277.00"));
        assertThat(tx.principalPortion()).isEqualTo(new BigDecimal("723.00"));
        assertThat(tx.outstandingBalanceAfter()).isEqualTo(new BigDecimal("17743.40"));
    }

    // ---- Helpers -------------------------------------------------------------------------

    private static LoanApplicationRequest application(long clientId) {
        return new LoanApplicationRequest(clientId, 1L, new BigDecimal("20000"), 12, null, null);
    }

    private static org.springframework.test.web.client.ResponseCreator json(String path) {
        return withSuccess(new ClassPathResource(path), MediaType.APPLICATION_JSON);
    }

    private static org.springframework.test.web.client.ResponseCreator statusResponse(int id, String label) {
        return withSuccess("""
                {"id": 1, "accountNo": "000000001", "status": {"id": %d, "value": "%s"}}
                """.formatted(id, label), MediaType.APPLICATION_JSON);
    }
}
