package et.adey.mfi.auth;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The limiter's arithmetic with a clock we can move by hand. */
class LoginAttemptLimiterTest {

    /** A clock whose time the test sets. */
    static final class MovableClock extends Clock {
        Instant now = Instant.parse("2026-10-23T10:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MovableClock clock = new MovableClock();
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(5, Duration.ofMinutes(15), clock);

    @Test
    void fiveFailuresLockTheUsernameUntilTheOldestLeavesTheWindow() {
        for (int i = 0; i < 5; i++) {
            assertThatCode(() -> limiter.checkAllowed("dawit", "10.0.0.1")).doesNotThrowAnyException();
            limiter.recordFailure("dawit", "10.0.0.1");
            clock.now = clock.now.plusSeconds(60); // one failure per minute
        }
        // 5 failures at 10:00..10:04; now 10:05 -> locked until 10:15 (+1 s).
        assertThatThrownBy(() -> limiter.checkAllowed("DAWIT", "10.0.0.2")) // usernames ignore case
                .isInstanceOfSatisfying(TooManyLoginAttemptsException.class,
                        e -> assertThat(e.getRetryAfterSeconds()).isEqualTo(601));

        clock.now = Instant.parse("2026-10-23T10:15:01Z");
        assertThatCode(() -> limiter.checkAllowed("dawit", "10.0.0.2")).doesNotThrowAnyException();
    }

    @Test
    void oneIpTryingManyUsernamesIsLockedToo() {
        for (String user : new String[]{"a", "b", "c", "d", "e"}) {
            limiter.recordFailure(user, "10.0.0.9");
        }
        assertThatThrownBy(() -> limiter.checkAllowed("samuel", "10.0.0.9"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
        assertThatCode(() -> limiter.checkAllowed("samuel", "10.0.0.10")).doesNotThrowAnyException();
    }

    @Test
    void successClearsTheUsernameButNotTheIp() {
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("hana", "10.0.0.3");
        }
        limiter.recordSuccess("hana");
        limiter.recordFailure("hana", "10.0.0.4");
        assertThatCode(() -> limiter.checkAllowed("hana", "10.0.0.4")).doesNotThrowAnyException();

        limiter.recordFailure("other", "10.0.0.3"); // 5th failure from 10.0.0.3
        assertThatThrownBy(() -> limiter.checkAllowed("hana", "10.0.0.3"))
                .isInstanceOf(TooManyLoginAttemptsException.class);
    }

    @Test
    void cleanUpForgetsOldFailures() {
        limiter.recordFailure("meron", "10.0.0.5");
        clock.now = clock.now.plus(Duration.ofMinutes(16));
        limiter.cleanUp();
        for (int i = 0; i < 4; i++) {
            limiter.recordFailure("meron", "10.0.0.5");
        }
        assertThatCode(() -> limiter.checkAllowed("meron", "10.0.0.5")).doesNotThrowAnyException();
    }
}
