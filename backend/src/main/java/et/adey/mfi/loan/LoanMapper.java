package et.adey.mfi.loan;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.common.Money;
import et.adey.mfi.common.Rates;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanSchedule;
import et.adey.mfi.fineract.model.FineractLoanSchedule.Period;
import et.adey.mfi.fineract.model.FineractLoanTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Pure functions: Fineract loan shapes -> our DTOs. No Spring, no HTTP, so they are easy to
 * unit test against saved Fineract responses.
 */
public final class LoanMapper {

    private LoanMapper() {
    }

    // ---- Schedule preview ----------------------------------------------------------------

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

    // ---- Loan list item ------------------------------------------------------------------

    public static LoanSummary toSummary(FineractLoan loan) {
        LoanStatus status = statusOf(loan);
        List<Period> installments = installmentsOf(loan);

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
                statusLabelOf(loan),
                Money.of(loan.principal()),
                Rates.percent(loan.annualInterestRate()),
                total,
                paid,
                Money.orZero(principalOutstanding),
                Money.orZero(totalOutstanding),
                next.map(p -> FineractDates.fromArray(p.dueDate())).orElse(null),
                next.map(p -> Money.orZero(p.totalOutstandingForPeriod())).orElse(null),
                loan.isInArrears());
    }

    // ---- Loan details --------------------------------------------------------------------

    public static LoanDetails toDetails(FineractLoan loan) {
        FineractLoan.Timeline t = loan.timeline();
        LoanDetails.Dates dates = t == null ? null : new LoanDetails.Dates(
                FineractDates.fromArray(t.submittedOnDate()),
                FineractDates.fromArray(t.approvedOnDate()),
                FineractDates.fromArray(t.rejectedOnDate()),
                FineractDates.fromArray(t.expectedDisbursementDate()),
                FineractDates.fromArray(t.actualDisbursementDate()),
                FineractDates.fromArray(t.expectedMaturityDate()),
                FineractDates.fromArray(t.closedOnDate()));

        FineractLoan.Summary s = loan.summary();
        LoanDetails.Summary summary = new LoanDetails.Summary(
                Money.orZero(s == null ? null : s.principalDisbursed()),
                Money.orZero(s == null ? null : s.interestCharged()),
                Money.orZero(s == null ? null : s.principalPaid()),
                Money.orZero(s == null ? null : s.interestPaid()),
                Money.orZero(s == null ? null : s.principalOutstanding()),
                Money.orZero(s == null ? null : s.interestOutstanding()),
                Money.orZero(s == null ? null : s.totalOutstanding()),
                Money.orZero(s == null ? null : s.totalOverdue()),
                s == null ? null : FineractDates.fromArray(s.overdueSinceDate()),
                loan.isInArrears());

        // Fineract also lists accruals (interest booked as income at disbursement). They are
        // accounting entries, not money paid or received, so a loan officer doesn't see them.
        List<LoanTransactionResponse> transactions = loan.transactions() == null ? List.of()
                : loan.transactions().stream()
                        .filter(tx -> tx.type() == null || !tx.type().isAccrualBookkeeping())
                        .map(LoanMapper::toTransaction)
                        .toList();

        return new LoanDetails(
                loan.id(),
                loan.accountNo(),
                loan.clientId(),
                loan.clientName(),
                loan.loanProductId(),
                loan.loanProductName(),
                loan.loanOfficerId(),
                loan.loanOfficerName() == null ? null : loan.loanOfficerName().trim(),
                statusOf(loan),
                statusLabelOf(loan),
                loan.currency() == null ? null : loan.currency().code(),
                Money.of(loan.principal()),
                Money.of(loan.approvedPrincipal()),
                Rates.percent(loan.annualInterestRate()),
                loan.numberOfRepayments() == null ? 0 : loan.numberOfRepayments(),
                dates,
                summary,
                installmentsOf(loan).stream().map(LoanMapper::toInstallment).toList(),
                transactions);
    }

    static LoanDetails.Installment toInstallment(Period p) {
        return new LoanDetails.Installment(
                p.period(),
                FineractDates.fromArray(p.dueDate()),
                Money.orZero(p.principalDue()),
                Money.orZero(p.interestDue()),
                Money.orZero(p.feeChargesDue()),
                Money.orZero(p.penaltyChargesDue()),
                Money.orZero(p.totalDueForPeriod()),
                Money.orZero(p.totalPaidForPeriod()),
                Money.orZero(p.totalOutstandingForPeriod()),
                Money.orZero(p.principalLoanBalanceOutstanding()),
                Boolean.TRUE.equals(p.complete()));
    }

    // ---- Transactions --------------------------------------------------------------------

    public static LoanTransactionResponse toTransaction(FineractLoanTransaction tx) {
        String paymentType = tx.paymentDetailData() == null || tx.paymentDetailData().paymentType() == null
                ? null : tx.paymentDetailData().paymentType().name();
        return new LoanTransactionResponse(
                tx.id(),
                FineractDates.fromArray(tx.date()),
                typeCode(tx.type()),
                tx.type() == null ? null : tx.type().value(),
                Money.orZero(tx.amount()),
                Money.orZero(tx.principalPortion()),
                Money.orZero(tx.interestPortion()),
                Money.orZero(tx.feeChargesPortion()),
                Money.orZero(tx.penaltyChargesPortion()),
                Money.orZero(tx.overpaymentPortion()),
                Money.orZero(tx.outstandingLoanBalance()),
                paymentType,
                Boolean.TRUE.equals(tx.manuallyReversed()));
    }

    /** "loanTransactionType.repaymentAtDisbursement" -> "REPAYMENT_AT_DISBURSEMENT". */
    static String typeCode(FineractLoanTransaction.Type type) {
        if (type == null || type.code() == null) {
            return "UNKNOWN";
        }
        String name = type.code().substring(type.code().lastIndexOf('.') + 1);
        return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
    }

    // ---- Helpers -------------------------------------------------------------------------

    static LoanStatus statusOf(FineractLoan loan) {
        return LoanStatus.fromFineractId(loan.status() == null ? null : loan.status().id());
    }

    static String statusLabelOf(FineractLoan loan) {
        return loan.status() == null ? null : loan.status().value();
    }

    private static List<Period> installmentsOf(FineractLoan loan) {
        return loan.repaymentSchedule() == null ? List.of() : loan.repaymentSchedule().installments();
    }
}
