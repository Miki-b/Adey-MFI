package et.adey.mfi.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * The demo data, as plain values. DemoSeeder makes Fineract match this plan.
 * Every record has a stable key (externalId / username / name) so a second run finds it
 * instead of creating a duplicate.
 *
 * Dates: Bishoftu Branch opened 2026-10-03, so its loans are recent. Adama Branch "opened"
 * 2025-01-01, so its loans can be back-dated months into the past; that is how we get
 * on-time histories, a fully repaid loan and loans in arrears without moving Fineract's clock.
 */
final class SeedPlan {

    private SeedPlan() {
    }

    static final String CASH = "Cash";
    static final String OFFICER_ROLE = "Loan Officer";
    static final String MANAGER_ROLE = "Branch Manager";

    /** Local demo passwords. They satisfy Fineract's "strong" policy (12+ chars, mixed, digit, symbol). */
    static final String OFFICER_PASSWORD = "Adey#Loan2026";
    static final String MANAGER_PASSWORD = "Adey#Lead2026";

    static final String BISHOFTU = "Bishoftu Branch";
    static final String ADAMA = "Adama Branch";
    static final LocalDate ADAMA_OPENED = LocalDate.of(2025, 1, 1);

    // ---- Roles (minimum Fineract permissions) --------------------------------------------

    static final List<String> OFFICER_PERMISSIONS = List.of(
            "READ_OFFICE", "READ_STAFF", "READ_LOANPRODUCT", "READ_PAYMENTTYPE", "READ_CODE", "READ_CODEVALUE",
            "READ_CLIENT", "CREATE_CLIENT", "ACTIVATE_CLIENT",
            "READ_LOAN", "CREATE_LOAN", "REPAYMENT_LOAN");

    static final List<String> MANAGER_EXTRA_PERMISSIONS = List.of(
            "APPROVE_LOAN", "REJECT_LOAN", "APPROVALUNDO_LOAN", "DISBURSE_LOAN");

    // ---- Staff and their login users ------------------------------------------------------

    enum Kind { OFFICER, MANAGER }

    /** externalId null = an existing staff member found by name (Samuel Derese). */
    record Staff(String key, String externalId, String office, String firstname, String lastname,
                 Kind kind, String username, String mobileNo, LocalDate joiningDate) {
    }

    static final List<Staff> STAFF = List.of(
            new Staff("samuel", null, BISHOFTU, "Samuel", "Derese", Kind.OFFICER, "samuel",
                    null, LocalDate.of(2026, 10, 3)),
            new Staff("hana", "SEED-STAFF-BSH-O2", BISHOFTU, "Hana", "Girma", Kind.OFFICER, "hana",
                    "0911000102", LocalDate.of(2026, 10, 3)),
            new Staff("tadesse", "SEED-STAFF-BSH-M", BISHOFTU, "Tadesse", "Bekele", Kind.MANAGER, "tadesse",
                    "0911000103", LocalDate.of(2026, 10, 3)),
            new Staff("meron", "SEED-STAFF-ADA-O1", ADAMA, "Meron", "Alemu", Kind.OFFICER, "meron",
                    "0911000201", ADAMA_OPENED),
            new Staff("dawit", "SEED-STAFF-ADA-O2", ADAMA, "Dawit", "Haile", Kind.OFFICER, "dawit",
                    "0911000202", ADAMA_OPENED),
            new Staff("selamawit", "SEED-STAFF-ADA-M", ADAMA, "Selamawit", "Tesfaye", Kind.MANAGER, "selamawit",
                    "0911000203", ADAMA_OPENED));

    // ---- Clients and their loans ----------------------------------------------------------

    /** Where the client's loan should end up. */
    enum Scenario {
        NO_LOAN,
        PENDING,
        APPROVED,
        REJECTED,
        /** Disbursed recently, nothing due yet. */
        ACTIVE_NEW,
        /** Every installment due so far was paid on its due date. */
        ACTIVE_ON_TIME,
        /** Some installments paid, then a part payment, then nothing: in arrears. */
        ACTIVE_PARTLY_PAID,
        /** Disbursed months ago, never paid: in arrears. */
        ACTIVE_NO_PAYMENTS,
        /** Every installment paid on its due date: closed (obligations met). */
        CLOSED
    }

    record Loan(String externalId, BigDecimal principal, int repayments, LocalDate submittedOn,
                LocalDate approvedOn, LocalDate disbursedOn, Scenario scenario,
                int installmentsPaidInFull, BigDecimal partialPayment) {
    }

    record Client(String externalId, String officerKey, String firstname, String lastname, String mobileNo,
                  LocalDate dateOfBirth, LocalDate activatedOn, Loan loan) {
    }

    private static final LocalDate BSH_ACTIVATED = LocalDate.of(2026, 10, 5);
    private static final LocalDate ADA_ACTIVATED = LocalDate.of(2025, 1, 15);

    private static Loan loan(String id, String principal, int repayments, String submitted, String approved,
                             String disbursed, Scenario scenario) {
        return loan(id, principal, repayments, submitted, approved, disbursed, scenario, 0, null);
    }

    private static Loan loan(String id, String principal, int repayments, String submitted, String approved,
                             String disbursed, Scenario scenario, int paidInFull, String partial) {
        return new Loan(id, new BigDecimal(principal), repayments, LocalDate.parse(submitted),
                approved == null ? null : LocalDate.parse(approved),
                disbursed == null ? null : LocalDate.parse(disbursed),
                scenario, paidInFull, partial == null ? null : new BigDecimal(partial));
    }

    static final List<Client> CLIENTS = List.of(
            // --- Bishoftu, Samuel ---
            new Client("SEED-C01", "samuel", "Abebe", "Kebede", "0922100001", LocalDate.of(1988, 3, 12),
                    BSH_ACTIVATED, null),
            new Client("SEED-C02", "samuel", "Almaz", "Tadesse", "0922100002", LocalDate.of(1992, 7, 4),
                    BSH_ACTIVATED, loan("SEED-L02", "15000", 12, "2026-10-20", null, null, Scenario.PENDING)),
            // --- Bishoftu, Hana ---
            new Client("SEED-C03", "hana", "Kidist", "Wolde", "0922100003", LocalDate.of(1990, 11, 23),
                    BSH_ACTIVATED, loan("SEED-L03", "25000", 18, "2026-10-12", "2026-10-15", null, Scenario.APPROVED)),
            new Client("SEED-C04", "hana", "Yonas", "Mekonnen", "0922100004", LocalDate.of(1985, 1, 30),
                    BSH_ACTIVATED, loan("SEED-L04", "50000", 24, "2026-10-10", null, null, Scenario.REJECTED)),
            new Client("SEED-C05", "hana", "Bethlehem", "Assefa", "0922100005", LocalDate.of(1995, 5, 17),
                    BSH_ACTIVATED, loan("SEED-L05", "10000", 6, "2026-10-06", "2026-10-07", "2026-10-08",
                    Scenario.ACTIVE_NEW)),
            new Client("SEED-C13", "hana", "Fikirte", "Mengistu", "0922100013", LocalDate.of(1993, 9, 2),
                    BSH_ACTIVATED, null),
            // --- Adama, Meron ---
            new Client("SEED-C06", "meron", "Tigist", "Haile", "0922200006", LocalDate.of(1987, 2, 14),
                    ADA_ACTIVATED, loan("SEED-L06", "20000", 12, "2026-05-10", "2026-05-12", "2026-05-15",
                    Scenario.ACTIVE_ON_TIME)),
            new Client("SEED-C07", "meron", "Getachew", "Ayele", "0922200007", LocalDate.of(1983, 8, 9),
                    ADA_ACTIVATED, loan("SEED-L07", "30000", 12, "2026-04-05", "2026-04-08", "2026-04-10",
                    Scenario.ACTIVE_PARTLY_PAID, 2, "1000.00")),
            new Client("SEED-C08", "meron", "Mulu", "Gebre", "0922200008", LocalDate.of(1979, 12, 1),
                    ADA_ACTIVATED, loan("SEED-L08", "8000", 6, "2025-02-20", "2025-02-25", "2025-03-01",
                    Scenario.CLOSED)),
            // --- Adama, Dawit ---
            new Client("SEED-C09", "dawit", "Solomon", "Tefera", "0922200009", LocalDate.of(1991, 4, 21),
                    ADA_ACTIVATED, loan("SEED-L09", "12000", 12, "2026-06-25", "2026-06-28", "2026-07-01",
                    Scenario.ACTIVE_NO_PAYMENTS)),
            new Client("SEED-C10", "dawit", "Hirut", "Desta", "0922200010", LocalDate.of(1996, 6, 6),
                    ADA_ACTIVATED, loan("SEED-L10", "40000", 24, "2026-10-21", null, null, Scenario.PENDING)),
            new Client("SEED-C11", "dawit", "Ermias", "Girma", "0922200011", LocalDate.of(1989, 10, 18),
                    ADA_ACTIVATED, null),
            new Client("SEED-C12", "dawit", "Meseret", "Bekele", "0922200012", LocalDate.of(1994, 3, 27),
                    ADA_ACTIVATED, loan("SEED-L12", "18000", 12, "2026-03-01", "2026-03-03", "2026-03-05",
                    Scenario.ACTIVE_ON_TIME)));

    /** Payment types to ensure exist (name -> isCashPayment). Existing ones are left alone. */
    static final Map<String, Boolean> PAYMENT_TYPES = Map.of(CASH, true);
}
