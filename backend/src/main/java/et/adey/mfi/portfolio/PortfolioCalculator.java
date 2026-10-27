package et.adey.mfi.portfolio;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.common.Money;
import et.adey.mfi.common.Names;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractLoanSchedule.Period;
import et.adey.mfi.loan.LoanStatus;
import et.adey.mfi.security.Officer;

import et.adey.mfi.security.Role;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Pure arithmetic on already-loaded loans: no Fineract calls, no Spring. "today" is a parameter,
 * so tests can pin the date.
 */
public final class PortfolioCalculator {

    /** Collection lists are for a field officer's phone: keep them short. */
    static final int MAX_COLLECTION_ITEMS = 50;

    private PortfolioCalculator() {
    }

    // ---- Loan list -----------------------------------------------------------------------

    /**
     * Filters and sorts for GET /api/loans:
     * - inArrears=true: most days overdue first (the collection list);
     * - status=PENDING: oldest application first (the approval queue, first come first served);
     * - otherwise: by client name.
     */
    public static List<LoanListItem> list(List<LoanView> loans, LoanListStatus status, Boolean inArrears,
                                          Long clientId, LocalDate today) {
        Predicate<LoanView> filter = v -> true;
        if (status != null) {
            filter = filter.and(v -> status.matches(v.status()));
        }
        if (inArrears != null) {
            filter = filter.and(v -> (v.daysOverdue(today) > 0) == inArrears);
        }
        if (clientId != null) {
            filter = filter.and(v -> Objects.equals(v.loan().clientId(), clientId));
        }
        Comparator<LoanListItem> order;
        if (Boolean.TRUE.equals(inArrears)) {
            order = Comparator.comparingLong(LoanListItem::daysOverdue).reversed()
                    .thenComparing(LoanListItem::principalOutstanding, Comparator.reverseOrder());
        } else if (status == LoanListStatus.PENDING) {
            order = Comparator.comparing(LoanListItem::submittedOn, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(LoanListItem::loanId);
        } else {
            order = Comparator.comparing(LoanListItem::clientName, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(LoanListItem::loanId);
        }
        return loans.stream().filter(filter).map(v -> toItem(v, today)).sorted(order).toList();
    }

    static LoanListItem toItem(LoanView v, LocalDate today) {
        var next = v.nextDue();
        long days = v.daysOverdue(today);
        return new LoanListItem(
                v.loan().id(),
                v.loan().accountNo(),
                v.loan().clientId(),
                v.clientName(),
                v.client() == null ? null : v.client().mobileNo(),
                v.loan().loanProductName(),
                v.client() == null ? null : Names.readable(v.client().staffName()),
                v.status(),
                v.loan().status() == null ? null : v.loan().status().value(),
                Money.of(v.loan().principal()),
                v.principalOutstanding(),
                v.totalOverdue(),
                days,
                days > 0,
                next.map(p -> FineractDates.fromArray(p.dueDate())).orElse(null),
                next.map(p -> Money.orZero(p.totalOutstandingForPeriod())).orElse(null),
                v.loan().timeline() == null ? null : FineractDates.fromArray(v.loan().timeline().submittedOnDate()));
    }

    // ---- Portfolio -----------------------------------------------------------------------

    public static Portfolio portfolio(Officer officer, Map<Long, FineractClientData> clients, List<LoanView> loans,
                                      LocalDate today) {
        List<LoanView> active = loans.stream().filter(LoanView::isActive).toList();

        Portfolio.Counts counts = new Portfolio.Counts(
                clients.size(),
                active.size(),
                (int) loans.stream().filter(v -> v.status() == LoanStatus.SUBMITTED_AND_PENDING_APPROVAL).count(),
                (int) loans.stream().filter(v -> v.status() == LoanStatus.APPROVED).count(),
                (int) active.stream().filter(v -> v.daysOverdue(today) > 0).count());

        Portfolio.Amounts amounts = new Portfolio.Amounts("ETB",
                sum(active, LoanView::principalOutstanding), sum(active, LoanView::totalOverdue));

        List<Portfolio.CollectionItem> dueToday = new ArrayList<>();
        List<Portfolio.CollectionItem> overdue = new ArrayList<>();
        for (LoanView v : active) {
            for (Period p : v.unpaidInstallments()) {
                LocalDate due = FineractDates.fromArray(p.dueDate());
                if (due.isEqual(today)) {
                    dueToday.add(collectionItem(v, p, 0));
                } else if (due.isBefore(today)) {
                    overdue.add(collectionItem(v, p, ChronoUnit.DAYS.between(due, today)));
                }
            }
        }
        dueToday.sort(Comparator.comparing(Portfolio.CollectionItem::clientName,
                Comparator.nullsLast(Comparator.naturalOrder())));
        overdue.sort(Comparator.comparing(Portfolio.CollectionItem::dueDate)
                .thenComparing(Portfolio.CollectionItem::loanId));

        List<Portfolio.OfficerSummary> officers = officer.isManager() ? officerBreakdown(clients, active, today) : null;

        return new Portfolio(today,
                new Portfolio.Scope(officer.isManager() ? Role.MANAGER : Role.OFFICER, officer.officeId(),
                        officer.officeName(), officer.isManager() ? null : officer.staffId(),
                        officer.isManager() ? null : officer.displayName()),
                counts, amounts, par(active, today),
                dueToday.size(), dueToday.stream().limit(MAX_COLLECTION_ITEMS).toList(),
                overdue.size(), overdue.stream().limit(MAX_COLLECTION_ITEMS).toList(),
                officers);
    }

    /**
     * PORTFOLIO AT RISK (PAR) - the MFI industry's main loan-quality number.
     *
     *   PAR(n) = principal OUTSTANDING of all loans with an installment more than n days late
     *            -----------------------------------------------------------------------------  x 100
     *                         principal outstanding of all active loans
     *
     * It deliberately counts the WHOLE remaining principal of a late loan, not just the overdue
     * installment: once a client stops paying, the entire balance is at risk of never coming back.
     * (Using only the overdue amount would make a bad portfolio look healthy.)
     * PAR1 = more than 1 day late, PAR30 = more than 30 days late. 0.00 when nothing is outstanding.
     */
    static Portfolio.Par par(List<LoanView> active, LocalDate today) {
        BigDecimal total = sum(active, LoanView::principalOutstanding);
        BigDecimal par1Principal = sum(active.stream().filter(v -> v.daysOverdue(today) > 1).toList(),
                LoanView::principalOutstanding);
        BigDecimal par30Principal = sum(active.stream().filter(v -> v.daysOverdue(today) > 30).toList(),
                LoanView::principalOutstanding);
        return new Portfolio.Par(percent(par1Principal, total), percent(par30Principal, total),
                par1Principal, par30Principal);
    }

    static BigDecimal percent(BigDecimal part, BigDecimal whole) {
        if (whole.signum() == 0) {
            return new BigDecimal("0.00");
        }
        return part.multiply(BigDecimal.valueOf(100)).divide(whole, 2, RoundingMode.HALF_UP);
    }

    /** Managers: one line per loan officer of the office (officers with clients but no active loans included). */
    private static List<Portfolio.OfficerSummary> officerBreakdown(Map<Long, FineractClientData> clients,
                                                                  List<LoanView> active, LocalDate today) {
        Map<Long, String> officerNames = new LinkedHashMap<>();
        Map<Long, Integer> clientCounts = new LinkedHashMap<>();
        for (FineractClientData c : clients.values()) {
            if (c.staffId() == null) continue;
            officerNames.putIfAbsent(c.staffId(), Names.readable(c.staffName()));
            clientCounts.merge(c.staffId(), 1, Integer::sum);
        }
        List<Portfolio.OfficerSummary> result = new ArrayList<>();
        for (Map.Entry<Long, String> o : officerNames.entrySet()) {
            List<LoanView> theirs = active.stream().filter(v -> Objects.equals(v.officerStaffId(), o.getKey())).toList();
            result.add(new Portfolio.OfficerSummary(o.getKey(), o.getValue(), clientCounts.get(o.getKey()),
                    theirs.size(), sum(theirs, LoanView::principalOutstanding), sum(theirs, LoanView::totalOverdue),
                    par(theirs, today).par30()));
        }
        result.sort(Comparator.comparing(Portfolio.OfficerSummary::officerName, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    private static Portfolio.CollectionItem collectionItem(LoanView v, Period p, long daysOverdue) {
        return new Portfolio.CollectionItem(v.loan().clientId(), v.clientName(),
                v.client() == null ? null : v.client().mobileNo(), v.loan().id(), v.loan().accountNo(),
                p.period(), FineractDates.fromArray(p.dueDate()), Money.orZero(p.totalOutstandingForPeriod()),
                daysOverdue);
    }

    private static BigDecimal sum(List<LoanView> loans, Function<LoanView, BigDecimal> amount) {
        return Money.orZero(loans.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
