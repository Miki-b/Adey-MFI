package et.adey.mfi.mobilemoney;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A request to collect a loan repayment from a client's mobile wallet (table payment_intents).
 * The status only changes through {@link #moveTo}, which enforces the state machine and records
 * every change with its time.
 */
@Entity
@Table(name = "payment_intents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    @Column(name = "office_id", nullable = false)
    private Long officeId;
    /** The client's loan officer when the payment was requested (for access checks without Fineract). */
    @Column(name = "staff_id")
    private Long staffId;
    @Column(name = "requested_by", nullable = false)
    private String requestedBy;
    @Column(nullable = false)
    private String phone;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(name = "provider_charge_id")
    private String providerChargeId;
    @Column(name = "provider_transaction_id")
    private String providerTransactionId;
    @Column(name = "provider_amount")
    private BigDecimal providerAmount;
    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "fineract_transaction_id")
    private Long fineractTransactionId;
    @Column(name = "posting_attempts", nullable = false)
    private int postingAttempts;
    @Column(name = "next_attempt_at")
    private Instant nextAttemptAt;
    @Column(name = "last_error")
    private String lastError;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "intent_id", nullable = false)
    @OrderBy("id")
    private List<PaymentIntentTransition> transitions = new ArrayList<>();

    public PaymentIntent(String reference, long loanId, long clientId, long officeId, Long staffId, String requestedBy,
                         String phone, BigDecimal amount, String currency, Instant now) {
        this.reference = reference;
        this.loanId = loanId;
        this.clientId = clientId;
        this.officeId = officeId;
        this.staffId = staffId;
        this.requestedBy = requestedBy;
        this.phone = phone;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
        this.createdAt = now;
        this.updatedAt = now;
        this.transitions.add(new PaymentIntentTransition(null, PaymentStatus.PENDING, now, "payment requested"));
    }

    /** The only way to change the status: checks the state machine and records the change. */
    public void moveTo(PaymentStatus next, Instant now, String note) {
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException("Payment intent %d cannot move from %s to %s".formatted(id, status, next));
        }
        transitions.add(new PaymentIntentTransition(status, next, now, note));
        this.status = next;
        this.updatedAt = now;
    }

    void chargeCreated(String providerChargeId) {
        this.providerChargeId = providerChargeId;
    }

    /** The provider says the money was taken: remember its transaction id (our future receipt number). */
    void paid(String providerTransactionId, BigDecimal providerAmount, Instant paidAt) {
        this.providerTransactionId = providerTransactionId;
        this.providerAmount = providerAmount;
        this.paidAt = paidAt;
    }

    void scheduleAttempt(Instant at) {
        this.nextAttemptAt = at;
    }

    void attemptFailed(String error, Instant next) {
        this.postingAttempts++;
        this.lastError = error == null ? null : error.substring(0, Math.min(1000, error.length()));
        this.nextAttemptAt = next;
    }

    void posted(long fineractTransactionId) {
        this.fineractTransactionId = fineractTransactionId;
        this.nextAttemptAt = null;
    }

    void recordError(String error) {
        this.lastError = error == null ? null : error.substring(0, Math.min(1000, error.length()));
    }
}
