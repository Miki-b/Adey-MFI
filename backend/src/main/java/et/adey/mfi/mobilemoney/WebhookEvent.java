package et.adey.mfi.mobilemoney;

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

import java.math.BigDecimal;
import java.time.Instant;

/**
 * INBOX row: a webhook that passed the signature and timestamp checks, stored before (and
 * together with) acting on it. event_id is unique, so a re-delivered event is detected.
 */
@Entity
@Table(name = "webhook_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebhookEvent {

    public enum Outcome {
        /** Applied to its intent (paid -> RECEIVED, or declined -> DECLINED). */
        ACCEPTED,
        /** Paid, but amount/currency differ from our intent: held, NOT posted. */
        MISMATCH,
        /** No intent with this reference/charge: nothing to apply. */
        UNKNOWN_REFERENCE,
        /** Valid but no longer relevant (e.g. intent already received through another event). */
        IGNORED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private String eventId;
    @Column(name = "charge_id")
    private String chargeId;
    private String reference;
    private String status;
    private BigDecimal amount;
    private String currency;
    @Column(name = "provider_transaction_id")
    private String providerTransactionId;
    @Column(name = "event_timestamp")
    private Instant eventTimestamp;
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Outcome outcome;
    @Column(name = "outcome_detail")
    private String outcomeDetail;
    @Column(name = "intent_id")
    private Long intentId;
    @Column(name = "raw_body", nullable = false)
    private String rawBody;

    WebhookEvent(TelePayWebhook w, Instant receivedAt, String rawBody) {
        this.eventId = w.eventId();
        this.chargeId = w.chargeId();
        this.reference = w.reference();
        this.status = w.status();
        this.amount = w.amount();
        this.currency = w.currency();
        this.providerTransactionId = w.providerTransactionId();
        this.eventTimestamp = w.timestamp();
        this.receivedAt = receivedAt;
        this.rawBody = rawBody;
        this.outcome = Outcome.IGNORED;
    }

    void outcome(Outcome outcome, Long intentId, String detail) {
        this.outcome = outcome;
        this.intentId = intentId;
        this.outcomeDetail = detail;
    }
}
