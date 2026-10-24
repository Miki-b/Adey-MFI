package et.adey.mfi.mobilemoney;

import et.adey.mfi.ApiTestBase;
import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.Verification;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.VerifiedStatus;
import et.adey.mfi.mobilemoney.PostingWorkerTest.MovableClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PaymentVerifier with a fake "chapa" provider and the REAL inbox and database: nothing is ever
 * accepted without a verify answer, and a verified payment goes through the normal inbox path.
 */
class PaymentVerifierTest extends ApiTestBase {

    private static final String REF = "AMP-CHAPA0000001";

    @Autowired
    private WebhookInbox inbox;
    @Autowired
    private AuditService audit;
    @Autowired
    private MobileMoneyProperties props;

    private MobileMoneyProvider chapa;
    private MovableClock clock;
    private PaymentVerifier verifier;

    @BeforeEach
    void setUp() {
        chapa = mock(MobileMoneyProvider.class);
        when(chapa.name()).thenReturn("chapa");
        when(chapa.needsVerification()).thenReturn(true);
        clock = new MovableClock();
        // 3 checks, 5 s base: quick to reach "give up".
        MobileMoneyProperties small = new MobileMoneyProperties("chapa", null, null, null, null,
                props.timestampTolerance(), "ETB", "Mobile Money", false, props.posting(),
                new MobileMoneyProperties.Verify(true, Duration.ofSeconds(10), 3, Duration.ofSeconds(5),
                        Duration.ofMinutes(2)));
        verifier = new PaymentVerifier(chapa, paymentIntents, inbox, small, audit, clock);
    }

    @Test
    void verifiedSuccessGoesThroughTheInboxAndIsScheduledForPosting() {
        PaymentIntent intent = pending("CHbXq2Mvg8E9M");
        answer(VerifiedStatus.SUCCEEDED, "success", "CHbXq2Mvg8E9M", "1833.60");

        assertThat(verifier.verifyDue()).isEqualTo(1);

        PaymentIntent i = reload(intent);
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.RECEIVED);
        assertThat(i.getProviderTransactionId()).isEqualTo("CHbXq2Mvg8E9M"); // future Fineract receipt number
        assertThat(i.getNextAttemptAt()).isNotNull();                        // the posting worker takes over
        assertThat(i.getNextVerifyAt()).isNull();
        assertThat(webhookEvents.findAll()).singleElement()
                .satisfies(e -> assertThat(e.getEventId()).isEqualTo("chapa:" + REF + ":SUCCEEDED"));
        verify(fineract, never()).repayLoan(anyLong(), any());
    }

    @Test
    void webhookTriggersVerifyAndARepeatChangesNothing() {
        PaymentIntent intent = pending("CHbXq2Mvg8E9M");
        answer(VerifiedStatus.SUCCEEDED, "success", "CHbXq2Mvg8E9M", "1833.60");

        assertThat(verifier.onWebhook(REF, "charge.success", "{\"raw\":1}")).isEqualTo(PaymentVerifier.Result.RECEIVED);
        assertThat(verifier.onWebhook(REF, "charge.success", "{\"raw\":1}")).isEqualTo(PaymentVerifier.Result.IGNORED);
        assertThat(verifier.verifyDue()).isZero(); // decided: no more polling

        verify(chapa, times(1)).verifyCharge(REF);
        assertThat(reload(intent).getStatus()).isEqualTo(PaymentStatus.RECEIVED);
        assertThat(webhookEvents.count()).isEqualTo(1);
    }

    @Test
    void verifyAnswerWinsOverTheWebhookClaim() {
        // The (authentic) webhook says success, Chapa's verify says still pending: we wait.
        PaymentIntent intent = pending("CHbXq2Mvg8E9M");
        answer(VerifiedStatus.PENDING, "pending", null, null);

        assertThat(verifier.onWebhook(REF, "charge.success", "{}")).isEqualTo(PaymentVerifier.Result.STILL_PENDING);
        assertThat(reload(intent).getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(webhookEvents.count()).isZero();
    }

    @Test
    void wrongAmountIsMismatchAndNotPosted() {
        PaymentIntent intent = pending("CHbXq2Mvg8E9M");
        answer(VerifiedStatus.SUCCEEDED, "success", "CHbXq2Mvg8E9M", "1000.00");

        assertThat(verifier.verifyOne(intent.getId(), "poll", null)).isEqualTo(PaymentVerifier.Result.MISMATCH);
        assertThat(reload(intent).getStatus()).isEqualTo(PaymentStatus.MISMATCH);
    }

    @Test
    void differentChapaReferenceIsMismatch() {
        PaymentIntent intent = pending("CHbXq2Mvg8E9M");
        answer(VerifiedStatus.SUCCEEDED, "success", "CHsomethingElse", "1833.60");

        assertThat(verifier.verifyOne(intent.getId(), "poll", null)).isEqualTo(PaymentVerifier.Result.MISMATCH);
        assertThat(reload(intent).getStatus()).isEqualTo(PaymentStatus.MISMATCH);
        assertThat(webhookEvents.count()).isZero();
    }

    @Test
    void failedCancelledIsDeclined() {
        PaymentIntent intent = pending(null); // charge was refused: no Chapa ref from the charge call
        answer(VerifiedStatus.DECLINED, "failed/cancelled", "CH85gCrlwXJPs", "10");

        assertThat(verifier.verifyOne(intent.getId(), "poll", null)).isEqualTo(PaymentVerifier.Result.DECLINED);
        PaymentIntent i = reload(intent);
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.DECLINED);
        assertThat(i.getProviderChargeId()).isEqualTo("CH85gCrlwXJPs"); // learnt from verify
    }

    @Test
    void unknownStatusNeedsAttentionNeverPosted() {
        PaymentIntent intent = pending("CHbXq2Mvg8E9M");
        answer(VerifiedStatus.UNKNOWN, "reversed", "CHbXq2Mvg8E9M", "1833.60");

        assertThat(verifier.verifyOne(intent.getId(), "poll", null)).isEqualTo(PaymentVerifier.Result.NEEDS_ATTENTION);
        assertThat(reload(intent).getStatus()).isEqualTo(PaymentStatus.NEEDS_ATTENTION);
        assertThat(auditLog.findAll()).anySatisfy(r ->
                assertThat(r.getAction()).isEqualTo(AuditAction.MOBILE_PAYMENT_NEEDS_ATTENTION));
    }

    @Test
    void pendingIsCheckedAgainWithBackoffThenNeedsAttention() {
        PaymentIntent intent = pending("CHbXq2Mvg8E9M");
        answer(VerifiedStatus.PENDING, "pending", null, null);
        Instant t0 = clock.now;

        verifier.verifyDue();
        assertThat(reload(intent).getNextVerifyAt()).isEqualTo(t0.plusSeconds(5));
        assertThat(verifier.verifyDue()).isZero(); // not due yet

        clock.now = t0.plusSeconds(5);
        verifier.verifyDue();
        assertThat(reload(intent).getNextVerifyAt()).isEqualTo(t0.plusSeconds(15)); // 5 s later x2
        clock.now = t0.plusSeconds(15);
        verifier.verifyDue(); // third check: give up

        PaymentIntent i = reload(intent);
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.NEEDS_ATTENTION);
        assertThat(i.getVerifyAttempts()).isEqualTo(3);
    }

    @Test
    void providerDownIsRetriedNotFailed() {
        PaymentIntent intent = pending("CHbXq2Mvg8E9M");
        when(chapa.verifyCharge(REF)).thenThrow(new ProviderUnavailableException("Chapa not reachable"));

        assertThat(verifier.verifyOne(intent.getId(), "poll", null)).isEqualTo(PaymentVerifier.Result.PROVIDER_UNAVAILABLE);
        assertThat(reload(intent).getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(reload(intent).getNextVerifyAt()).isAfter(clock.now);
    }

    @Test
    void neverInitiatedAndUnknownToChapaIsChargeFailed() {
        PaymentIntent intent = pending(null);
        answer(VerifiedStatus.NOT_FOUND, null, null, null);

        assertThat(verifier.verifyOne(intent.getId(), "poll", null)).isEqualTo(PaymentVerifier.Result.CHARGE_FAILED);
        assertThat(reload(intent).getStatus()).isEqualTo(PaymentStatus.CHARGE_FAILED);
    }

    @Test
    void webhookForAReferenceWeDontHaveIsIgnored() {
        assertThat(verifier.onWebhook("AMP-NOBODY", "charge.success", "{}"))
                .isEqualTo(PaymentVerifier.Result.UNKNOWN_REFERENCE);
        verify(chapa, never()).verifyCharge(any());
    }

    // ---- Helpers ---------------------------------------------------------------------------

    private PaymentIntent pending(String chapaRef) {
        PaymentIntent i = new PaymentIntent(REF, 1, 1, 2, 1L, "samuel", "0900123456", new BigDecimal("1833.60"), "ETB",
                "chapa", PaymentMethod.TELEBIRR, clock.now);
        if (chapaRef != null) {
            i.chargeCreated(chapaRef);
        }
        i.scheduleVerification(clock.now);
        return paymentIntents.save(i);
    }

    private void answer(VerifiedStatus status, String raw, String chapaRef, String amount) {
        when(chapa.verifyCharge(REF)).thenReturn(new Verification(status, raw, REF, chapaRef,
                amount == null ? null : new BigDecimal(amount), "ETB", clock.now, "{\"status\":\"" + raw + "\"}"));
    }

    private PaymentIntent reload(PaymentIntent i) {
        return paymentIntents.findById(i.getId()).orElseThrow();
    }
}
