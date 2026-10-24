package et.adey.mfi.mobilemoney;

import java.time.Duration;
import java.time.Instant;

/**
 * RETRY WITH EXPONENTIAL BACKOFF. When Fineract is down we try again later, waiting longer after
 * each failure: base, 2x base, 4x base, ... capped at maxDelay. Retrying at once in a tight loop
 * would hammer a system that is already struggling, and an outage of a few minutes needs only a
 * handful of attempts. After maxAttempts failures we stop and ask a person (NEEDS_ATTENTION).
 *
 * With the defaults (10 s base, 2 min cap, 8 attempts): 10s, 20s, 40s, 80s, 2m, 2m, 2m, then give up,
 * about 8.5 minutes after the first failure.
 * (Real systems often add random "jitter" so many clients don't retry at the same instant; with
 * one worker we keep it deterministic and easy to test.)
 */
public record RetryPolicy(int maxAttempts, Duration baseDelay, Duration maxDelay) {

    public static RetryPolicy from(MobileMoneyProperties.Posting p) {
        return new RetryPolicy(p.maxAttempts(), p.baseDelay(), p.maxDelay());
    }

    public static RetryPolicy from(MobileMoneyProperties.Verify v) {
        return new RetryPolicy(v.maxAttempts(), v.baseDelay(), v.maxDelay());
    }

    /** Delay before the next try after this many failed attempts (1 = the first failure). */
    public Duration delayAfter(int failedAttempts) {
        if (failedAttempts < 1) {
            return Duration.ZERO;
        }
        int doublings = Math.min(failedAttempts - 1, 30); // avoid overflow
        Duration delay = baseDelay.multipliedBy(1L << doublings);
        return delay.compareTo(maxDelay) > 0 ? maxDelay : delay;
    }

    public boolean shouldGiveUp(int failedAttempts) {
        return failedAttempts >= maxAttempts;
    }

    public Instant nextAttemptAt(Instant now, int failedAttempts) {
        return now.plus(delayAfter(failedAttempts));
    }
}
