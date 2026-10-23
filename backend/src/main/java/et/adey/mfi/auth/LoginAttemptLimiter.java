package et.adey.mfi.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Slows down password guessing: after maxFailures failed logins within the window (default 5 in
 * 15 minutes), for the same USERNAME or from the same IP ADDRESS, further attempts are refused with
 * 429 until the oldest failure leaves the window. Both keys matter: per username stops guessing one
 * officer's password; per IP stops one device trying many usernames.
 *
 * IN-MEMORY: counts live in this JVM only. They reset on restart and are not shared between
 * several backend instances. Good enough for one server; use a shared store (DB/Redis) to scale out.
 */
@Component
public class LoginAttemptLimiter {

    private final int maxFailures;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(@Value("${app.security.login.max-failures:5}") int maxFailures,
                               @Value("${app.security.login.window:15m}") Duration window,
                               Clock clock) {
        this.maxFailures = maxFailures;
        this.window = window;
        this.clock = clock;
    }

    /** Throws TooManyLoginAttemptsException if the username or the IP is currently locked. */
    public void checkAllowed(String username, String ip) {
        Instant now = clock.instant();
        long retryAfter = Math.max(secondsUntilFree(userKey(username), now), secondsUntilFree(ipKey(ip), now));
        if (retryAfter > 0) {
            throw new TooManyLoginAttemptsException(retryAfter);
        }
    }

    public void recordFailure(String username, String ip) {
        Instant now = clock.instant();
        add(userKey(username), now);
        add(ipKey(ip), now);
    }

    /** A correct login clears that username's failures (the IP's stay: it may be guessing other names). */
    public void recordSuccess(String username) {
        failures.remove(userKey(username));
    }

    /** Forget all failures (tests; or an admin action after a false alarm). */
    public void reset() {
        failures.clear();
    }

    /** Housekeeping so the map does not grow forever. */
    @Scheduled(fixedRate = 15 * 60 * 1000, initialDelay = 15 * 60 * 1000)
    public void cleanUp() {
        Instant now = clock.instant();
        failures.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                prune(e.getValue(), now);
                return e.getValue().isEmpty();
            }
        });
    }

    private long secondsUntilFree(String key, Instant now) {
        Deque<Instant> times = failures.get(key);
        if (times == null) {
            return 0;
        }
        synchronized (times) {
            prune(times, now);
            if (times.size() < maxFailures) {
                return 0;
            }
            // Free again when the oldest failure that still counts leaves the window.
            Instant oldestCounting = times.stream().skip(times.size() - maxFailures).findFirst().orElseThrow();
            long seconds = Duration.between(now, oldestCounting.plus(window)).toSeconds();
            return Math.max(1, seconds + 1);
        }
    }

    private void add(String key, Instant now) {
        Deque<Instant> times = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (times) {
            prune(times, now);
            times.addLast(now);
        }
    }

    private void prune(Deque<Instant> times, Instant now) {
        Instant cutoff = now.minus(window);
        while (!times.isEmpty() && !times.peekFirst().isAfter(cutoff)) {
            times.pollFirst();
        }
    }

    private static String userKey(String username) {
        return "user:" + (username == null ? "" : username.trim().toLowerCase(Locale.ROOT));
    }

    private static String ipKey(String ip) {
        return "ip:" + ip;
    }
}
