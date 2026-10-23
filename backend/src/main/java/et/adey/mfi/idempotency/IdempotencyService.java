package et.adey.mfi.idempotency;

import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.security.Officer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * Makes a POST safe to retry. Why: a field officer records a repayment on a weak mobile connection;
 * Fineract books it but the answer never reaches the phone. The app shows an error, the officer taps
 * again, and without protection the client is charged twice. With an Idempotency-Key (a random id
 * the app creates ONCE per payment and sends again on every retry) the retry gets the first answer.
 *
 *   new key                         -> run the action, store its response (24 h)
 *   same key + same body, finished  -> return the stored response, Fineract is NOT called again
 *   same key + different body       -> 422 IDEMPOTENCY_KEY_REUSED
 *   same key while the first runs   -> 409 IDEMPOTENCY_IN_PROGRESS (try again in a moment)
 *   first attempt refused (403/409/422/400): key released, a retry runs normally (nothing happened)
 *   first attempt failed after possibly reaching Fineract (timeout, 5xx): key kept as OUTCOME_UNKNOWN
 *     -> 409 IDEMPOTENCY_OUTCOME_UNKNOWN; check the loan before retrying with a NEW key. We choose
 *        "maybe tell the officer to check" over "maybe charge twice".
 *
 * Keys are per user (two officers may use the same key string) and expire after 24 hours.
 */
@Slf4j
@Service
public class IdempotencyService {

    public static final String HEADER = "Idempotency-Key";
    static final Duration TTL = Duration.ofHours(24);
    private static final Pattern KEY_FORMAT = Pattern.compile("^[A-Za-z0-9_-]{8,100}$");

    private final IdempotencyRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public IdempotencyService(IdempotencyRepository repository, ObjectMapper objectMapper, Clock clock) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    /** The response, and whether it was replayed from storage (then the action did not run). */
    public record Outcome<T>(T body, boolean replayed) {
    }

    public <T> Outcome<T> execute(Officer officer, String key, String endpoint, Object request,
                                  Class<T> responseType, Supplier<T> action) {
        if (key == null) {
            return new Outcome<>(action.get(), false); // header not sent: behave as before
        }
        if (!KEY_FORMAT.matcher(key).matches()) {
            throw new IdempotencyException(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_INVALID",
                    "Idempotency-Key must be 8-100 characters: letters, digits, '-' or '_' (a UUID is ideal).");
        }
        String hash = fingerprint(endpoint, request);
        Instant now = clock.instant();

        Optional<IdempotencyRecord> existing = repository.findByUsernameAndKey(officer.username(), key);
        if (existing.isPresent() && existing.get().isExpired(now)) {
            repository.delete(existing.get());
            existing = Optional.empty();
        }
        if (existing.isPresent()) {
            return replay(existing.get(), hash, responseType);
        }

        IdempotencyRecord record;
        try {
            record = repository.saveAndFlush(new IdempotencyRecord(officer.username(), key, endpoint, hash,
                    now, now.plus(TTL)));
        } catch (DataIntegrityViolationException race) {
            // A parallel retry with the same key inserted first: answer as a replay of that one.
            return replay(repository.findByUsernameAndKey(officer.username(), key).orElseThrow(), hash, responseType);
        }

        try {
            T response = action.get();
            record.complete(objectMapper.writeValueAsString(response));
            repository.save(record);
            return new Outcome<>(response, false);
        } catch (RuntimeException e) {
            AuditOutcome kind = AuditService.outcomeOf(e);
            if (kind == AuditOutcome.DENIED || kind == AuditOutcome.REJECTED) {
                repository.delete(record); // refused before anything happened: retrying is safe
            } else {
                record.markOutcomeUnknown();
                repository.save(record);
                log.warn("Idempotency-Key {} of {} ({}): outcome unknown after {}", key, officer.username(),
                        endpoint, e.toString());
            }
            throw e;
        }
    }

    private <T> Outcome<T> replay(IdempotencyRecord record, String hash, Class<T> responseType) {
        if (!record.getRequestHash().equals(hash)) {
            throw new IdempotencyException(HttpStatus.UNPROCESSABLE_CONTENT, "IDEMPOTENCY_KEY_REUSED",
                    "This Idempotency-Key was already used for a different request. Use a new key for a new request.");
        }
        return switch (record.getState()) {
            case COMPLETED -> {
                log.info("Idempotent replay of {} for {}", record.getEndpoint(), record.getUsername());
                yield new Outcome<>(objectMapper.readValue(record.getResponseBody(), responseType), true);
            }
            case IN_PROGRESS -> throw new IdempotencyException(HttpStatus.CONFLICT, "IDEMPOTENCY_IN_PROGRESS",
                    "The first request with this Idempotency-Key is still being processed. Try again in a moment.");
            case OUTCOME_UNKNOWN -> throw new IdempotencyException(HttpStatus.CONFLICT, "IDEMPOTENCY_OUTCOME_UNKNOWN",
                    "The first request with this Idempotency-Key failed and may or may not have been recorded. "
                            + "Check the loan/client before retrying with a NEW key.");
        };
    }

    /** SHA-256 of the endpoint and the JSON of the request body (records serialize in a fixed order). */
    String fingerprint(String endpoint, Object request) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(request);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(endpoint.getBytes(StandardCharsets.UTF_8));
            sha.update((byte) '\n');
            return HexFormat.of().formatHex(sha.digest(json));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Housekeeping: drop keys older than 24 h, every hour. */
    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 60 * 1000)
    public void deleteExpired() {
        int removed = repository.deleteExpired(clock.instant());
        if (removed > 0) {
            log.info("Removed {} expired idempotency keys", removed);
        }
    }
}
