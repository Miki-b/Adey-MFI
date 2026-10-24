package et.adey.mfi.reconciliation;

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

/** One line of a reconciliation run (table reconciliation_findings). */
@Entity
@Table(name = "reconciliation_findings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReconciliationFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "run_id", nullable = false)
    private Long runId;
    @Enumerated(EnumType.STRING)
    @Column(name = "finding_type", nullable = false)
    private FindingType type;
    @Column(name = "office_id")
    private Long officeId;
    @Column(name = "loan_id")
    private Long loanId;
    @Column(name = "intent_id")
    private Long intentId;
    private String reference;
    @Column(name = "receipt_number")
    private String receiptNumber;
    @Column(name = "provider_amount")
    private BigDecimal providerAmount;
    @Column(name = "provider_paid_at")
    private Instant providerPaidAt;
    @Column(name = "intent_amount")
    private BigDecimal intentAmount;
    @Column(name = "fineract_amount")
    private BigDecimal fineractAmount;
    @Column(name = "fineract_transaction_id")
    private Long fineractTransactionId;
    private String detail;
    @Column(name = "resolved_at")
    private Instant resolvedAt;
    @Column(name = "resolved_by")
    private String resolvedBy;
    @Column(name = "resolution_note")
    private String resolutionNote;

    ReconciliationFinding(long runId, ReconciliationMatcher.Finding f) {
        this.runId = runId;
        this.type = f.type();
        this.officeId = f.officeId();
        this.loanId = f.loanId();
        this.intentId = f.intentId();
        this.reference = f.reference();
        this.receiptNumber = f.receiptNumber();
        this.providerAmount = f.providerAmount();
        this.providerPaidAt = f.providerPaidAt();
        this.intentAmount = f.intentAmount();
        this.fineractAmount = f.fineractAmount();
        this.fineractTransactionId = f.fineractTransactionId();
        this.detail = f.detail();
    }

    public boolean isResolved() {
        return resolvedAt != null;
    }

    void resolve(String by, Instant at, String note) {
        this.resolvedBy = by;
        this.resolvedAt = at;
        this.resolutionNote = note == null ? null : note.substring(0, Math.min(1000, note.length()));
    }
}
