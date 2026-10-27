package et.adey.mfi.portfolio;

import et.adey.mfi.TestJson;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanSchedule;
import et.adey.mfi.fineract.model.FineractPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static et.adey.mfi.TestOfficers.MERON;
import static et.adey.mfi.TestOfficers.SELAMAWIT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Portfolio numbers from REAL Fineract data (Adama Branch, captured 2026-10-23, see
 * src/test/resources/fineract/portfolio) with "today" pinned to that date, plus small hand-made
 * cases for the edges (due today, 1 vs 2 days late, 30 vs 31 days late).
 */
class PortfolioCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 23);
    private final ObjectMapper json = TestJson.mapper();

    private Map<Long, FineractClientData> adamaClients;
    private List<LoanView> adamaLoans;

    @BeforeEach
    void loadAdama() throws IOException {
        FineractPage<FineractClientData> clients = read("fineract/portfolio/clients-adama.json", new TypeReference<>() { });
        adamaClients = new LinkedHashMap<>();
        clients.pageItems().forEach(c -> adamaClients.put(c.id(), c));
        FineractPage<FineractLoan> loans = read("fineract/portfolio/loans-all.json", new TypeReference<>() { });
        adamaLoans = loans.pageItems().stream()
                .filter(l -> adamaClients.containsKey(l.clientId()))
                .map(l -> new LoanView(l, schedule(l.id()), adamaClients.get(l.clientId())))
                .toList();
    }

    @Test
    void adamaManagerPortfolioMatchesTheHandCalculation() {
        Portfolio p = PortfolioCalculator.portfolio(SELAMAWIT, adamaClients, adamaLoans, TODAY);

        assertThat(p.counts().clients()).isEqualTo(7);
        assertThat(p.counts().activeLoans()).isEqualTo(4);
        assertThat(p.counts().pendingApplications()).isEqualTo(1);   // Hirut
        assertThat(p.counts().loansInArrears()).isEqualTo(2);        // Getachew, Solomon
        assertThat(p.amounts().principalOutstanding()).isEqualByComparingTo("56736.16");
        assertThat(p.amounts().totalOverdue()).isEqualByComparingTo("13302.08");

        // PAR30 = (24745.16 Getachew + 12000.00 Solomon) / 56736.16 x 100 = 64.76 %
        assertThat(p.par().par30Principal()).isEqualByComparingTo("36745.16");
        assertThat(p.par().par30()).isEqualByComparingTo("64.76");
        assertThat(p.par().par1()).isEqualByComparingTo("64.76"); // nobody is 2-30 days late

        // NOT the overdue amount: (13302.08 / 56736.16) would be only 23.45 %.
        assertThat(p.par().par30()).isNotEqualByComparingTo("23.45");

        assertThat(p.officers()).extracting(Portfolio.OfficerSummary::officerName)
                .containsExactly("Dawit Haile", "Meron Alemu");
        assertThat(p.officers().get(1).par30()).isEqualByComparingTo("67.16"); // 24745.16 / 36843.65
        assertThat(p.officers().get(0).par30()).isEqualByComparingTo("60.32"); // 12000.00 / 19892.51

        assertThat(p.overdueCount()).isEqualTo(7);
        assertThat(p.overdue().get(0)).satisfies(o -> {
            assertThat(o.clientName()).isEqualTo("Getachew Ayele");
            assertThat(o.installment()).isEqualTo(3);
            assertThat(o.dueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
            assertThat(o.amount()).isEqualByComparingTo("1750.40");
            assertThat(o.daysOverdue()).isEqualTo(105);
        });
    }

    @Test
    void officerPortfolioHasNoOfficerBreakdown() {
        Map<Long, FineractClientData> meronClients = new LinkedHashMap<>();
        adamaClients.values().stream().filter(c -> c.staffId() == 4L).forEach(c -> meronClients.put(c.id(), c));
        List<LoanView> meronLoans = adamaLoans.stream().filter(v -> meronClients.containsKey(v.loan().clientId())).toList();

        Portfolio p = PortfolioCalculator.portfolio(MERON, meronClients, meronLoans, TODAY);

        assertThat(p.scope().role()).isEqualTo(et.adey.mfi.security.Role.OFFICER);
        assertThat(p.scope().staffId()).isEqualTo(4L);
        assertThat(p.officers()).isNull();
        assertThat(p.counts().clients()).isEqualTo(3);
        assertThat(p.par().par30()).isEqualByComparingTo("67.16");
    }

    @Test
    void arrearsListIsSortedByDaysOverdueDescending() {
        List<LoanListItem> arrears = PortfolioCalculator.list(adamaLoans, null, true, null, TODAY);

        assertThat(arrears).extracting(LoanListItem::loanId).containsExactly(7L, 9L);
        assertThat(arrears).extracting(LoanListItem::daysOverdue).containsExactly(105L, 83L);
        assertThat(arrears.get(0).nextDueDate()).isEqualTo(LocalDate.of(2026, 7, 10));
        assertThat(arrears.get(0).nextDueAmount()).isEqualByComparingTo("1750.40");
        assertThat(arrears.get(0).loanOfficerName()).isEqualTo("Meron Alemu");
    }

    @Test
    void statusAndClientFilters() {
        assertThat(PortfolioCalculator.list(adamaLoans, LoanListStatus.PENDING, null, null, TODAY))
                .extracting(LoanListItem::clientName).containsExactly("Hirut Desta");
        assertThat(PortfolioCalculator.list(adamaLoans, LoanListStatus.CLOSED, null, null, TODAY))
                .extracting(LoanListItem::clientName).containsExactly("Mulu Gebre");
        assertThat(PortfolioCalculator.list(adamaLoans, LoanListStatus.ACTIVE, false, null, TODAY))
                .extracting(LoanListItem::clientName).containsExactly("Meseret Bekele", "Tigist Haile");
        assertThat(PortfolioCalculator.list(adamaLoans, null, null, 11L, TODAY))
                .singleElement().satisfies(l -> assertThat(l.loanId()).isEqualTo(9L));
    }

    // ---- Edges, hand-made ----------------------------------------------------------------

    @Test
    void dueTodayIsNotOverdueAndYesterdayIsOneDay() {
        assertThat(view(1, "10000", "10000", TODAY).daysOverdue(TODAY)).isZero();
        assertThat(view(2, "10000", "10000", TODAY.minusDays(1)).daysOverdue(TODAY)).isEqualTo(1);
    }

    @Test
    void par1CountsMoreThanOneDayAndPar30MoreThanThirtyDays() {
        List<LoanView> loans = List.of(
                view(1, "10000", "8000", TODAY.minusDays(1)),   // 1 day: in neither
                view(2, "10000", "6000", TODAY.minusDays(2)),   // 2 days: PAR1 only
                view(3, "10000", "4000", TODAY.minusDays(30)),  // 30 days: PAR1 only
                view(4, "10000", "2000", TODAY.minusDays(31)),  // 31 days: PAR1 and PAR30
                view(5, "10000", "5000", TODAY.plusDays(10)));  // not yet due

        Portfolio.Par par = PortfolioCalculator.par(loans, TODAY);

        // total outstanding 25000; PAR1 = 6000+4000+2000 = 12000 -> 48.00 %; PAR30 = 2000 -> 8.00 %
        assertThat(par.par1Principal()).isEqualByComparingTo("12000");
        assertThat(par.par1()).isEqualByComparingTo("48.00");
        assertThat(par.par30Principal()).isEqualByComparingTo("2000");
        assertThat(par.par30()).isEqualByComparingTo("8.00");
    }

    @Test
    void parIsZeroWhenNothingIsOutstandingAndRoundsHalfUp() {
        assertThat(PortfolioCalculator.par(List.of(), TODAY).par30()).isEqualByComparingTo("0.00");
        assertThat(PortfolioCalculator.percent(new BigDecimal("1"), new BigDecimal("3"))).isEqualByComparingTo("33.33");
        assertThat(PortfolioCalculator.percent(new BigDecimal("2"), new BigDecimal("3"))).isEqualByComparingTo("66.67");
    }

    @Test
    void dueTodayListShowsInstallmentsDueToday() {
        LoanView v = view(1, "10000", "10000", TODAY);
        Portfolio p = PortfolioCalculator.portfolio(MERON, Map.of(1L, v.client()), List.of(v), TODAY);

        assertThat(p.dueTodayCount()).isEqualTo(1);
        assertThat(p.dueToday().get(0).amount()).isEqualByComparingTo("1000.00");
        assertThat(p.overdueCount()).isZero();
    }

    // ---- Helpers -------------------------------------------------------------------------

    /** An active loan whose first unpaid installment (1000.00 still owed) falls due on firstUnpaidDue. */
    private LoanView view(long id, String principal, String outstanding, LocalDate firstUnpaidDue) {
        String due = "[%d, %d, %d]".formatted(firstUnpaidDue.getYear(), firstUnpaidDue.getMonthValue(),
                firstUnpaidDue.getDayOfMonth());
        FineractLoan loan = json.readValue("""
                {"id": %d, "accountNo": "%09d", "clientId": %d, "loanProductName": "Small Business Loan",
                 "status": {"id": 300, "value": "Active"}, "principal": %s,
                 "summary": {"principalOutstanding": %s, "totalOverdue": 0}}
                """.formatted(id, id, id, principal, outstanding), FineractLoan.class);
        FineractLoanSchedule schedule = json.readValue("""
                {"periods": [
                  {"period": 1, "dueDate": %s, "totalDueForPeriod": 1000.0, "totalOutstandingForPeriod": 1000.0, "complete": false}
                ]}
                """.formatted(due), FineractLoanSchedule.class);
        FineractClientData client = json.readValue("""
                {"id": %d, "displayName": "Client %d", "officeId": 3, "staffId": 4, "staffName": "Alemu, Meron"}
                """.formatted(id, id), FineractClientData.class);
        return new LoanView(loan, schedule, client);
    }

    private FineractLoanSchedule schedule(Long loanId) {
        ClassPathResource r = new ClassPathResource("fineract/portfolio/loan-" + loanId + "-schedule.json");
        if (!r.exists()) {
            return null; // not active: no schedule fetched, like PortfolioReader
        }
        try (InputStream in = r.getInputStream()) {
            return json.readValue(in, FineractLoan.class).repaymentSchedule();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private <T> T read(String path, TypeReference<T> type) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return json.readValue(in, type);
        }
    }
}
