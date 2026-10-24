package et.adey.mfi.mobilemoney;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The allowed state changes (PaymentStatus) and the history they leave (PaymentIntent). */
class PaymentStateMachineTest {

    private static final Instant T0 = Instant.parse("2026-10-24T08:00:00Z");

    private PaymentIntent intent() {
        return new PaymentIntent("AMP-TEST", 1, 1, 2, 1L, "samuel", "0922100001", new BigDecimal("1833.60"), "ETB", T0);
    }

    @Test
    void happyPathAndRetryPathAreAllowed() {
        PaymentIntent i = intent();
        i.moveTo(PaymentStatus.RECEIVED, T0.plusSeconds(5), "paid");
        i.moveTo(PaymentStatus.RETRYING, T0.plusSeconds(10), "attempt 1 failed");
        i.moveTo(PaymentStatus.RETRYING, T0.plusSeconds(20), "attempt 2 failed");
        i.moveTo(PaymentStatus.POSTED, T0.plusSeconds(40), "posted");

        assertThat(i.getStatus()).isEqualTo(PaymentStatus.POSTED);
        assertThat(i.getTransitions()).extracting(PaymentIntentTransition::getToStatus).containsExactly(
                PaymentStatus.PENDING, PaymentStatus.RECEIVED, PaymentStatus.RETRYING, PaymentStatus.RETRYING,
                PaymentStatus.POSTED);
        assertThat(i.getTransitions().get(1).getFromStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(i.getTransitions().get(4).getOccurredAt()).isEqualTo(T0.plusSeconds(40));
        assertThat(i.getUpdatedAt()).isEqualTo(T0.plusSeconds(40));
    }

    @Test
    void finalStatesCannotMove() {
        for (PaymentStatus end : new PaymentStatus[]{PaymentStatus.POSTED, PaymentStatus.DECLINED,
                PaymentStatus.CHARGE_FAILED, PaymentStatus.MISMATCH, PaymentStatus.NEEDS_ATTENTION}) {
            for (PaymentStatus next : PaymentStatus.values()) {
                assertThat(end.canMoveTo(next)).as(end + " -> " + next).isFalse();
            }
        }
    }

    @Test
    void forbiddenMovesAreRefused() {
        PaymentIntent i = intent();
        // Not paid yet: cannot be posted or retried.
        assertThatThrownBy(() -> i.moveTo(PaymentStatus.POSTED, T0, "x")).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> i.moveTo(PaymentStatus.RETRYING, T0, "x")).isInstanceOf(IllegalStateException.class);
        i.moveTo(PaymentStatus.RECEIVED, T0, "paid");
        // Once received, the provider can no longer "decline" it.
        assertThatThrownBy(() -> i.moveTo(PaymentStatus.DECLINED, T0, "x")).isInstanceOf(IllegalStateException.class);
        assertThat(i.getStatus()).isEqualTo(PaymentStatus.RECEIVED);
        assertThat(i.getTransitions()).hasSize(2); // refused moves leave no history
    }

    @Test
    void openAndAwaitingPostingFlags() {
        assertThat(PaymentStatus.PENDING.isOpen()).isTrue();
        assertThat(PaymentStatus.RETRYING.isOpen()).isTrue();
        assertThat(PaymentStatus.POSTED.isOpen()).isFalse();
        assertThat(PaymentStatus.RECEIVED.isAwaitingPosting()).isTrue();
        assertThat(PaymentStatus.PENDING.isAwaitingPosting()).isFalse();
    }

    @Test
    void retryPolicyDoublesUpToTheCapAndGivesUp() {
        RetryPolicy p = new RetryPolicy(8, Duration.ofSeconds(10), Duration.ofMinutes(2));
        assertThat(p.delayAfter(1)).isEqualTo(Duration.ofSeconds(10));
        assertThat(p.delayAfter(2)).isEqualTo(Duration.ofSeconds(20));
        assertThat(p.delayAfter(3)).isEqualTo(Duration.ofSeconds(40));
        assertThat(p.delayAfter(4)).isEqualTo(Duration.ofSeconds(80));
        assertThat(p.delayAfter(5)).isEqualTo(Duration.ofMinutes(2)); // 160 s capped at 120 s
        assertThat(p.delayAfter(40)).isEqualTo(Duration.ofMinutes(2)); // no overflow
        assertThat(p.shouldGiveUp(7)).isFalse();
        assertThat(p.shouldGiveUp(8)).isTrue();
        assertThat(p.nextAttemptAt(T0, 3)).isEqualTo(T0.plusSeconds(40));
    }
}
