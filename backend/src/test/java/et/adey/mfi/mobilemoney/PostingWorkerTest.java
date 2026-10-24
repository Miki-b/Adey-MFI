package et.adey.mfi.mobilemoney;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.TestJson;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.config.FineractConfig;
import et.adey.mfi.config.FineractProperties;
import et.adey.mfi.fineract.FineractClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The posting worker against a fake Fineract (MockRestServiceServer) and a clock we move by hand:
 * exactly which calls it makes, that it never posts twice, and when it retries.
 */
class PostingWorkerTest extends ApiTestBase {

    private static final String BASE = "https://fineract.test/fineract-provider/api/v1";
    private static final String LOAN_WITH_TX = BASE + "/loans/1?associations=repaymentSchedule";

    @Autowired
    private AuditService audit;
    @Autowired
    private MobileMoneyProperties props;

    private MockRestServiceServer server;
    private MovableClock clock;
    private MobileMoneyPostingService worker;

    static final class MovableClock extends Clock {
        Instant now = Instant.parse("2026-10-24T08:00:00Z");

        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @BeforeEach
    void setUp() {
        FineractProperties fp = new FineractProperties(BASE, "default", "mifos", "password",
                Duration.ofSeconds(5), Duration.ofSeconds(30), false);
        RestClient.Builder builder = FineractConfig.applyDefaults(RestClient.builder(), fp);
        server = MockRestServiceServer.bindTo(builder).build();
        clock = new MovableClock();
        // 3 attempts, 10 s base, so the give-up case is quick to reach.
        MobileMoneyProperties small = new MobileMoneyProperties(props.providerBaseUrl(), props.apiKey(),
                props.webhookSecret(), props.callbackUrl(), props.timestampTolerance(), "ETB", "Mobile Money", false,
                new MobileMoneyProperties.Posting(3, Duration.ofSeconds(10), Duration.ofMinutes(2)));
        worker = new MobileMoneyPostingService(new FineractClient(builder.build(), TestJson.mapper()), paymentIntents,
                small, audit, clock);
    }

    @Test
    void postsOnceWithReceiptAndMobileMoneyTypeAndNeverAgain() {
        PaymentIntent intent = received("TPX1");
        expectLoanTransactions("[]");
        server.expect(requestTo(BASE + "/paymenttypes")).andRespond(withSuccess(
                "[{\"id\": 4, \"name\": \"Cash\"}, {\"id\": 5, \"name\": \"Mobile Money\"}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1/transactions?command=repayment"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.receiptNumber").value("TPX1"))
                .andExpect(jsonPath("$.paymentTypeId").value(5))
                .andExpect(jsonPath("$.transactionAmount").value(1833.6))
                .andExpect(jsonPath("$.transactionDate").value("24 October 2026"))
                .andRespond(withSuccess("{\"loanId\": 1, \"resourceId\": 50}", MediaType.APPLICATION_JSON));

        assertThat(worker.postDue()).isEqualTo(1);
        // Second run: our DB says POSTED, so no Fineract call at all (the mock fails on any extra call).
        assertThat(worker.postDue()).isZero();
        assertThat(worker.postOne(intent.getId()).getStatus()).isEqualTo(PaymentStatus.POSTED);

        server.verify();
        PaymentIntent done = paymentIntents.findById(intent.getId()).orElseThrow();
        assertThat(done.getStatus()).isEqualTo(PaymentStatus.POSTED);
        assertThat(done.getFineractTransactionId()).isEqualTo(50L);
        assertThat(auditLog.findAll()).filteredOn(r -> r.getAction() == AuditAction.MOBILE_PAYMENT_POSTED).hasSize(1);
    }

    @Test
    void receiptAlreadyInFineractIsNotPostedAgain() {
        PaymentIntent intent = received("TPX2");
        expectLoanTransactions("""
                [{"id": 77, "type": {"code": "loanTransactionType.repayment"}, "amount": 1833.6,
                  "paymentDetailData": {"paymentType": {"id": 5, "name": "Mobile Money"}, "receiptNumber": "TPX2"}}]""");
        // No POST expected.

        PaymentIntent after = worker.postOne(intent.getId());

        server.verify();
        assertThat(after.getStatus()).isEqualTo(PaymentStatus.POSTED);
        assertThat(after.getFineractTransactionId()).isEqualTo(77L);
        assertThat(after.getTransitions().getLast().getNote()).contains("receipt found");
    }

    @Test
    void outageRetriesWithBackoffThenGivesUp() {
        PaymentIntent intent = received("TPX3");
        // Exactly three Fineract calls are allowed, all failing (the mock refuses any extra call).
        server.expect(requestTo(startsWith(LOAN_WITH_TX))).andRespond(withException(new IOException("Connection refused")));
        server.expect(requestTo(startsWith(LOAN_WITH_TX))).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        server.expect(requestTo(startsWith(LOAN_WITH_TX))).andRespond(withException(new IOException("Connection refused")));

        worker.postDue();
        PaymentIntent i = paymentIntents.findById(intent.getId()).orElseThrow();
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.RETRYING);
        assertThat(i.getPostingAttempts()).isEqualTo(1);
        assertThat(i.getNextAttemptAt()).isEqualTo(clock.now.plusSeconds(10));

        // Not due yet: nothing happens, no Fineract call.
        clock.now = clock.now.plusSeconds(9);
        assertThat(worker.postDue()).isZero();

        // Due: 2nd failure, next wait doubles to 20 s.
        clock.now = clock.now.plusSeconds(1);
        worker.postDue();
        i = paymentIntents.findById(intent.getId()).orElseThrow();
        assertThat(i.getPostingAttempts()).isEqualTo(2);
        assertThat(i.getNextAttemptAt()).isEqualTo(clock.now.plusSeconds(20));

        // 3rd failure = maxAttempts: a person has to look at it.
        clock.now = clock.now.plusSeconds(20);
        worker.postDue();

        server.verify();
        i = paymentIntents.findById(intent.getId()).orElseThrow();
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.NEEDS_ATTENTION);
        assertThat(i.getPostingAttempts()).isEqualTo(3);
        assertThat(i.getTransitions()).extracting(PaymentIntentTransition::getToStatus).containsExactly(
                PaymentStatus.PENDING, PaymentStatus.RECEIVED, PaymentStatus.RETRYING, PaymentStatus.RETRYING,
                PaymentStatus.NEEDS_ATTENTION);
        assertThat(auditLog.findAll()).extracting(r -> r.getAction()).containsExactlyInAnyOrder(
                AuditAction.MOBILE_PAYMENT_RETRY, AuditAction.MOBILE_PAYMENT_RETRY, AuditAction.MOBILE_PAYMENT_NEEDS_ATTENTION);
    }

    @Test
    void fineractRefusalIsNotRetried() {
        PaymentIntent intent = received("TPX4");
        expectLoanTransactions("[]");
        server.expect(requestTo(BASE + "/paymenttypes")).andRespond(withSuccess(
                "[{\"id\": 5, \"name\": \"Mobile Money\"}]", MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/loans/1/transactions?command=repayment"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body("""
                        {"defaultUserMessage": "Loan must be Active, Fully Paid or Overpaid", "errors": []}"""));

        PaymentIntent after = worker.postOne(intent.getId());

        server.verify();
        assertThat(after.getStatus()).isEqualTo(PaymentStatus.NEEDS_ATTENTION);
        assertThat(after.getLastError()).contains("HTTP 400");
    }

    private PaymentIntent received(String providerTxnId) {
        PaymentIntent i = new PaymentIntent("AMP-" + providerTxnId, 1, 1, 2, 1L, "samuel", "0922100001",
                new BigDecimal("1833.60"), "ETB", clock.now);
        i.chargeCreated("ch_" + providerTxnId);
        i.paid(providerTxnId, new BigDecimal("1833.60"), Instant.parse("2026-10-24T07:30:00Z"));
        i.moveTo(PaymentStatus.RECEIVED, clock.now, "paid");
        i.scheduleAttempt(clock.now);
        return paymentIntents.save(i);
    }

    private void expectLoanTransactions(String transactionsJson) {
        server.expect(requestTo(startsWith(LOAN_WITH_TX))).andRespond(withSuccess(
                "{\"id\": 1, \"clientId\": 1, \"status\": {\"id\": 300}, \"transactions\": " + transactionsJson + "}",
                MediaType.APPLICATION_JSON));
    }
}
