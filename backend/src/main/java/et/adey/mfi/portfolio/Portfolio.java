package et.adey.mfi.portfolio;

import org.jspecify.annotations.Nullable;

import et.adey.mfi.security.Role;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * GET /api/me/portfolio: the dashboard of an officer (their clients) or a manager (their office).
 * Money in ETB with 2 decimals; PAR values are percentages with 2 decimals (e.g. 12.34 = 12.34 %).
 */
public record Portfolio(
        LocalDate asOf,
        Scope scope,
        Counts counts,
        Amounts amounts,
        Par par,
        int dueTodayCount,
        List<CollectionItem> dueToday,
        int overdueCount,
        List<CollectionItem> overdue,
        /* Managers only (null for officers). */
        @Nullable List<OfficerSummary> officers) {

    /** role: whose numbers these are (an officer's own clients, or a manager's whole office). */
    public record Scope(Role role, long officeId, String officeName, @Nullable Long staffId, @Nullable String officerName) {
    }

    public record Counts(int clients, int activeLoans, int pendingApplications, int approvedNotDisbursed,
                         int loansInArrears) {
    }

    public record Amounts(String currency, BigDecimal principalOutstanding, BigDecimal totalOverdue) {
    }

    /**
     * Portfolio at risk. par1 = principal OUTSTANDING of loans more than 1 day late / all principal
     * outstanding x 100; par30 the same for more than 30 days. The *Principal fields are the numerators.
     */
    public record Par(BigDecimal par1, BigDecimal par30, BigDecimal par1Principal, BigDecimal par30Principal) {
    }

    /** One installment to collect: who, which loan, how much is still owed, since when. */
    public record CollectionItem(Long clientId, String clientName, @Nullable String clientMobileNo, Long loanId,
                                 String accountNo, int installment, LocalDate dueDate, BigDecimal amount,
                                 long daysOverdue) {
    }

    public record OfficerSummary(@Nullable Long staffId, @Nullable String officerName, int clients, int activeLoans,
                                 BigDecimal principalOutstanding, BigDecimal totalOverdue, BigDecimal par30) {
    }
}
