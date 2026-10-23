package et.adey.mfi.portfolio;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.common.Money;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanSchedule;
import et.adey.mfi.fineract.model.FineractLoanSchedule.Period;
import et.adey.mfi.loan.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * One loan as the portfolio code sees it: the loan from Fineract's list (with summary), its
 * repayment schedule (only fetched for ACTIVE loans, else null) and its client.
 * All derived numbers (days overdue, next due, ...) are computed here, relative to "today".
 */
public record LoanView(FineractLoan loan, FineractLoanSchedule schedule, FineractClientData client) {

    public LoanStatus status() {
        return LoanStatus.fromFineractId(loan.status() == null ? null : loan.status().id());
    }

    public boolean isActive() {
        return status() == LoanStatus.ACTIVE;
    }

    public BigDecimal principalOutstanding() {
        return isActive() && loan.summary() != null ? Money.orZero(loan.summary().principalOutstanding())
                : Money.orZero(null);
    }

    public BigDecimal totalOverdue() {
        return isActive() && loan.summary() != null ? Money.orZero(loan.summary().totalOverdue())
                : Money.orZero(null);
    }

    /** Installments not yet fully paid, oldest first. */
    public List<Period> unpaidInstallments() {
        return schedule == null ? List.of() : schedule.installments().stream()
                .filter(p -> !Boolean.TRUE.equals(p.complete()))
                .toList();
    }

    /**
     * Days since the oldest unpaid installment's due date; 0 if nothing is past due.
     * An installment due TODAY is not overdue yet (0 days); one due yesterday is 1 day overdue.
     * From the schedule when we have it, else from Fineract's summary.overdueSinceDate (same date).
     */
    public long daysOverdue(LocalDate today) {
        if (!isActive()) {
            return 0;
        }
        LocalDate oldestUnpaidDue = schedule != null
                ? unpaidInstallments().stream().map(p -> FineractDates.fromArray(p.dueDate()))
                        .filter(d -> d.isBefore(today)).findFirst().orElse(null)
                : (loan.summary() == null ? null : FineractDates.fromArray(loan.summary().overdueSinceDate()));
        return oldestUnpaidDue == null ? 0 : Math.max(0, ChronoUnit.DAYS.between(oldestUnpaidDue, today));
    }

    /** The oldest installment still (partly) unpaid: what the client should pay next. */
    public Optional<Period> nextDue() {
        return isActive() ? unpaidInstallments().stream().findFirst() : Optional.empty();
    }

    public String clientName() {
        return client != null && client.displayName() != null ? client.displayName() : loan.clientName();
    }

    public Long officerStaffId() {
        return client == null ? null : client.staffId();
    }
}
