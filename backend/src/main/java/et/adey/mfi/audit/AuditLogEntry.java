package et.adey.mfi.audit;

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
 * One row of the audit_log table (created by Flyway V1). @Entity = JPA maps this class to a
 * table: each field is a column. Rows are only ever inserted, never updated.
 */
@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA needs a no-args constructor
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    private String username;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "staff_id")
    private Long staffId;
    @Column(name = "office_id")
    private Long officeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "client_id")
    private Long clientId;
    @Column(name = "loan_id")
    private Long loanId;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditOutcome outcome;

    private String detail;

    public AuditLogEntry(Instant occurredAt, String username, Long userId, Long staffId, Long officeId,
                         AuditAction action, Long clientId, Long loanId, BigDecimal amount,
                         AuditOutcome outcome, String detail) {
        this.occurredAt = occurredAt;
        this.username = username;
        this.userId = userId;
        this.staffId = staffId;
        this.officeId = officeId;
        this.action = action;
        this.clientId = clientId;
        this.loanId = loanId;
        this.amount = amount;
        this.outcome = outcome;
        this.detail = detail;
    }
}
