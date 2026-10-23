package et.adey.mfi.loan;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.common.Money;
import et.adey.mfi.common.Rates;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanSchedule;
import et.adey.mfi.fineract.model.FineractLoanSchedule.Period;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Pure functions: Fineract loan shapes -> our DTOs. No Spring, no HTTP, so they are easy to
 * unit test against saved Fineract responses.
 */
public final class LoanMapper {

    private LoanMapper() {
    }

    public static SchedulePreviewResponse toPreview(FineractLoanSchedule schedule) {
        List<SchedulePeriod> periods = schedule.installments().stream().map(LoanMapper::toPeriod).toList();

        // The disbursement row (no period number) carries the disbursement date.
        LocalDate disbursementDate = schedule.periods() == null ? null : schedule.periods().stream()
                .filter(p -> !p.isInstallment())
                .findFirst()
                .map(p -> FineractDates.fromArray(p.dueDate()))
                .orElse(null);

        return new SchedulePreviewResponse(
                schedule.currency() == null ? null : schedule.currency().code(),
                Money.of(schedule.totalPrincipalDisbursed()),
                periods.size(),
                disbursementDate,
                periods.isEmpty() ? null : periods.get(0).total(),
                periods.isEmpty() ? null : periods.get(periods.size() - 1).total(),
                Money.orZero(schedule.totalInterestCharged()),
                Money.orZero(schedule.totalRepaymentExpected()),
                periods);
    }

    static SchedulePeriod toPeriod(Period p) {
        return new SchedulePeriod(
                p.period(),
                FineractDates.fromArray(p.dueDate()),
                Money.orZero(p.principalDue()),
                Money.orZero(p.interestDue()),
                Money.orZero(p.totalDueForPeriod()),
                Money.orZero(p.principalLoanBalanceOutstanding()));
    }

    public static LoanSummary toSummary(FineractLoan loan) {
        LoanStatus status = LoanStatus.fromFineractId(loan.status() == null ? null : loan.status().id());
        List<Period> installments = loan.repaymentSchedule() == null
                ? List.of()
                : loan.repaymentSchedule().installments();

        int paid = (int) installments.stream().filter(p -> Boolean.TRUE.equals(p.complete())).count();
        int total = !installments.isEmpty() ? installments.size()
                : loan.numberOfRepayments() == null ? 0 : loan.numberOfRepayments();

        // Only a disbursed (active) loan has a real "next due" installment.
        Optional<Period> next = status == LoanStatus.ACTIVE
                ? installments.stream().filter(p -> !Boolean.TRUE.equals(p.complete())).findFirst()
                : Optional.empty();

        FineractLoan.Summary s = loan.summary();
        BigDecimal principalOutstanding = s == null ? null : s.principalOutstanding();
        BigDecimal totalOutstanding = s == null ? null : s.totalOutstanding();

        return new LoanSummary(
                loan.id(),
                loan.accountNo(),
                loan.loanProductName(),
                status,
                Money.of(loan.principal()),
                Rates.percent(loan.annualInterestRate()),
                total,
                paid,
                Money.orZero(principalOutstanding),
                Money.orZero(totalOutstanding),
                next.map(p -> FineractDates.fromArray(p.dueDate())).orElse(null),
                next.map(p -> Money.orZero(p.totalOutstandingForPeriod())).orElse(null),
                s != null && Boolean.TRUE.equals(s.inArrears()));
    }
}
