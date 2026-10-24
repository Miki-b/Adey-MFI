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

import java.time.Instant;

/** One state change of a payment intent (table payment_intent_transitions). */
@Entity
@Table(name = "payment_intent_transitions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentIntentTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private PaymentStatus toStatus;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    private String note;

    PaymentIntentTransition(PaymentStatus fromStatus, PaymentStatus toStatus, Instant occurredAt, String note) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.occurredAt = occurredAt;
        this.note = note == null ? null : note.substring(0, Math.min(1000, note.length()));
    }
}
