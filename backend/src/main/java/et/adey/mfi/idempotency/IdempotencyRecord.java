package et.adey.mfi.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** A row of idempotency_keys (Flyway V2). */
@Entity
@Table(name = "idempotency_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyRecord {

    public enum State { IN_PROGRESS, COMPLETED, OUTCOME_UNKNOWN }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "idem_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String endpoint;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private State state;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    IdempotencyRecord(String username, String key, String endpoint, String requestHash,
                      Instant createdAt, Instant expiresAt) {
        this.username = username;
        this.key = key;
        this.endpoint = endpoint;
        this.requestHash = requestHash;
        this.state = State.IN_PROGRESS;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    void complete(String responseBody) {
        this.state = State.COMPLETED;
        this.responseBody = responseBody;
    }

    void markOutcomeUnknown() {
        this.state = State.OUTCOME_UNKNOWN;
    }

    boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
