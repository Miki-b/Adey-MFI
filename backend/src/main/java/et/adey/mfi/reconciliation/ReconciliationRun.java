package et.adey.mfi.reconciliation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** One execution of the reconciliation for one business day (table reconciliation_runs). */
@Entity
@Table(name = "reconciliation_runs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReconciliationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;
    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "finished_at")
    private Instant finishedAt;
    @Column(name = "provider_count")
    private Integer providerCount;
    @Column(name = "provider_total")
    private BigDecimal providerTotal;
    @Column(name = "fineract_count")
    private Integer fineractCount;
    @Column(name = "fineract_total")
    private BigDecimal fineractTotal;
    @Column(nullable = false)
    private String status;
    private String error;

    ReconciliationRun(LocalDate businessDate, String triggeredBy, Instant startedAt) {
        this.businessDate = businessDate;
        this.triggeredBy = triggeredBy;
        this.startedAt = startedAt;
        this.status = "RUNNING";
    }

    void finished(Instant at, int providerCount, BigDecimal providerTotal, int fineractCount, BigDecimal fineractTotal) {
        this.finishedAt = at;
        this.providerCount = providerCount;
        this.providerTotal = providerTotal;
        this.fineractCount = fineractCount;
        this.fineractTotal = fineractTotal;
        this.status = "COMPLETED";
    }

    void failed(Instant at, String error) {
        this.finishedAt = at;
        this.status = "FAILED";
        this.error = error == null ? null : error.substring(0, Math.min(1000, error.length()));
    }
}
