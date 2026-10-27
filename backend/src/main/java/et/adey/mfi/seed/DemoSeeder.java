package et.adey.mfi.seed;

import et.adey.mfi.client.GenderCodeValues;
import et.adey.mfi.common.FineractDates;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientAccounts.LoanAccount;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanCommands;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import et.adey.mfi.fineract.model.FineractLoanSchedule.Period;
import et.adey.mfi.fineract.model.FineractLoanTransaction;
import et.adey.mfi.loan.LoanService;
import et.adey.mfi.loan.LoanStatus;
import et.adey.mfi.seed.SeedPlan.Client;
import et.adey.mfi.seed.SeedPlan.Kind;
import et.adey.mfi.seed.SeedPlan.Loan;
import et.adey.mfi.seed.SeedPlan.Scenario;
import et.adey.mfi.seed.SeedPlan.Staff;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Creates the demo data described in SeedPlan, through the Fineract API.
 * Runs only with the "seed" profile:  mvnw spring-boot:run -Dspring-boot.run.profiles=local,seed
 *
 * Idempotent: every step first looks the record up by its stable key (name, username,
 * externalId) and only creates what is missing. Loans are moved forward step by step
 * (submit -> approve/reject -> disburse -> repayments) until they reach the planned state,
 * so re-running continues where a previous run stopped and otherwise changes nothing.
 *
 * Uses the Fineract service account directly: seeding is administration, not officer work.
 */
@Slf4j
@Component
@Profile("seed")
public class DemoSeeder implements ApplicationRunner {

    private static final long HEAD_OFFICE_ID = 1L;

    private final FineractClient fineract;
    private final ConfigurableApplicationContext context;
    private final GenderCodeValues genders;
    private final Map<String, Long> officeIds = new HashMap<>();
    private final Map<String, Long> staffIds = new HashMap<>();
    private final Map<String, Long> staffOffice = new HashMap<>();
    private int created;

    public DemoSeeder(FineractClient fineract, ConfigurableApplicationContext context, GenderCodeValues genders) {
        this.fineract = fineract;
        this.context = context;
        this.genders = genders;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Seeding demo data into Fineract ===");
        // Foundations that a brand-new Fineract (e.g. in a Codespace) does not have yet.
        ensureCurrency(SeedPlan.CURRENCY);
        long productId = ensureLoanProduct();
        long cashId = ensurePaymentTypes();
        ensureCodeValues("Gender", List.of("Male", "Female"));
        ensureOffices();
        long officerRole = ensureRole(SeedPlan.OFFICER_ROLE, "Adey MFI loan officer: clients, applications, repayments",
                SeedPlan.OFFICER_PERMISSIONS);
        List<String> managerPermissions = new ArrayList<>(SeedPlan.OFFICER_PERMISSIONS);
        managerPermissions.addAll(SeedPlan.MANAGER_EXTRA_PERMISSIONS);
        long managerRole = ensureRole(SeedPlan.MANAGER_ROLE, "Adey MFI branch manager: also approves and disburses",
                managerPermissions);
        for (Staff s : SeedPlan.STAFF) {
            ensureStaff(s);
            ensureUser(s, s.kind() == Kind.OFFICER ? officerRole : managerRole);
        }
        FineractLoanProduct product = fineract.getLoanProduct(productId);
        for (Client c : SeedPlan.CLIENTS) {
            long clientId = ensureClient(c);
            if (c.loan() != null) {
                ensureLoan(clientId, c, product, cashId);
            }
        }
        printSummary();
        log.info("=== Seeding finished: {} records created or changed ===", created);
        // Scheduled jobs (idempotency clean-up, posting worker, ...) keep threads alive, so a
        // web-less app would never stop by itself. Shut down explicitly once seeding is done.
        System.exit(SpringApplication.exit(context, () -> 0));
    }

    // ---- Foundations: currency, loan product ----------------------------------------------

    /** PUT /currencies REPLACES the whole list, so we send every currency already selected plus ours. */
    private void ensureCurrency(String code) {
        JsonNode selected = fineract.getJson("/currencies").path("selectedCurrencyOptions");
        List<String> codes = new ArrayList<>();
        for (JsonNode c : selected) {
            codes.add(c.path("code").asString());
        }
        if (codes.contains(code)) {
            log.info("Currency {} already enabled", code);
            return;
        }
        codes.add(code);
        fineract.putJson("/currencies", Map.of("currencies", codes));
        created++;
        log.info("Currency {} enabled (now {})", code, codes);
    }

    /** The "Small Business Loan" product (Phase 1 settings), found by name or created. */
    private long ensureLoanProduct() {
        Optional<JsonNode> existing = find(fineract.getJson("/loanproducts"), "name", SeedPlan.PRODUCT_NAME);
        if (existing.isPresent()) {
            long id = existing.get().path("id").asLong();
            log.info("Loan product '{}' exists (id {})", SeedPlan.PRODUCT_NAME, id);
            return id;
        }
        long id = fineract.postJson("/loanproducts", SeedPlan.loanProductBody()).path("resourceId").asLong();
        created++;
        log.info("Loan product '{}' created (id {})", SeedPlan.PRODUCT_NAME, id);
        return id;
    }

    // ---- Payment types and offices -------------------------------------------------------

    private long ensurePaymentTypes() {
        Long cashId = null;
        for (Map.Entry<String, Boolean> type : SeedPlan.PAYMENT_TYPES.entrySet()) {
            Optional<JsonNode> existing = find(fineract.getJson("/paymenttypes"), "name", type.getKey());
            long id;
            if (existing.isPresent()) {
                id = existing.get().path("id").asLong();
                log.info("Payment type '{}' exists (id {})", type.getKey(), id);
            } else {
                id = fineract.postJson("/paymenttypes", Map.of(
                        "name", type.getKey(), "description", type.getKey(),
                        "isCashPayment", type.getValue(), "position", 2)).path("resourceId").asLong();
                created++;
                log.info("Payment type '{}' created (id {})", type.getKey(), id);
            }
            if (type.getKey().equals(SeedPlan.CASH)) {
                cashId = id;
            }
        }
        return cashId;
    }

    /** Fineract ships the "Gender" code without values; client creation needs them. */
    private void ensureCodeValues(String codeName, List<String> values) {
        Optional<JsonNode> code = find(fineract.getJson("/codes"), "name", codeName);
        if (code.isEmpty()) {
            log.warn("Code '{}' not found; skipping its values", codeName);
            return;
        }
        long codeId = code.get().path("id").asLong();
        JsonNode existing = fineract.getJson("/codes/{id}/codevalues", codeId);
        int position = 1;
        for (String value : values) {
            if (find(existing, "name", value).isEmpty()) {
                fineract.postJson("/codes/{id}/codevalues",
                        Map.of("name", value, "position", position, "isActive", true), codeId);
                created++;
                log.info("Code value {} / {} created", codeName, value);
            }
            position++;
        }
    }

    private void ensureOffices() {
        JsonNode offices = fineract.getJson("/offices");
        ensureOffice(offices, SeedPlan.BISHOFTU, "SEED-OFFICE-BISHOFTU", SeedPlan.BISHOFTU_OPENED);
        ensureOffice(offices, SeedPlan.ADAMA, "SEED-OFFICE-ADAMA", SeedPlan.ADAMA_OPENED);
    }

    private void ensureOffice(JsonNode offices, String name, String externalId, LocalDate opened) {
        Optional<JsonNode> existing = find(offices, "name", name);
        if (existing.isPresent()) {
            officeIds.put(name, existing.get().path("id").asLong());
            log.info("Office '{}' exists (id {})", name, officeIds.get(name));
        } else {
            long id = fineract.postJson("/offices", withDateFormat(Map.of(
                    "name", name, "parentId", HEAD_OFFICE_ID, "externalId", externalId,
                    "openingDate", FineractDates.format(opened)))).path("resourceId").asLong();
            officeIds.put(name, id);
            created++;
            log.info("Office '{}' created (id {})", name, id);
        }
    }

    // ---- Roles, staff, users -------------------------------------------------------------

    /** Creates the role if missing, then (always) sets exactly these permissions on it. */
    private long ensureRole(String name, String description, List<String> permissions) {
        Optional<JsonNode> existing = find(fineract.getJson("/roles"), "name", name);
        long id;
        if (existing.isPresent()) {
            id = existing.get().path("id").asLong();
            log.info("Role '{}' exists (id {})", name, id);
        } else {
            id = fineract.postJson("/roles", Map.of("name", name, "description", description))
                    .path("resourceId").asLong();
            created++;
            log.info("Role '{}' created (id {})", name, id);
        }
        Map<String, Boolean> grants = new LinkedHashMap<>();
        permissions.forEach(p -> grants.put(p, true));
        fineract.putJson("/roles/{id}/permissions", Map.of("permissions", grants), id);
        log.info("Role '{}' permissions: {}", name, permissions);
        return id;
    }

    private void ensureStaff(Staff s) {
        long officeId = officeIds.get(s.office());
        JsonNode staff = fineract.getJson("/staff?officeId={id}&status=all", officeId);
        Optional<JsonNode> existing = s.externalId() != null
                ? find(staff, "externalId", s.externalId())
                : findStaffByName(staff, s.firstname(), s.lastname());
        long id;
        if (existing.isPresent()) {
            id = existing.get().path("id").asLong();
            log.info("Staff {} {} exists (id {})", s.firstname(), s.lastname(), id);
        } else {
            Map<String, Object> body = new LinkedHashMap<>(withDateFormat(Map.of(
                    "officeId", officeId, "firstname", s.firstname(), "lastname", s.lastname(),
                    "isLoanOfficer", s.kind() == Kind.OFFICER, "isActive", true,
                    "joiningDate", FineractDates.format(s.joiningDate()))));
            if (s.externalId() != null) body.put("externalId", s.externalId());
            if (s.mobileNo() != null) body.put("mobileNo", s.mobileNo());
            id = fineract.postJson("/staff", body).path("resourceId").asLong();
            created++;
            log.info("Staff {} {} created (id {})", s.firstname(), s.lastname(), id);
        }
        staffIds.put(s.key(), id);
        staffOffice.put(s.key(), officeId);
    }

    private void ensureUser(Staff s, long roleId) {
        if (find(fineract.getJson("/users"), "username", s.username()).isPresent()) {
            log.info("User '{}' exists", s.username());
            return;
        }
        String password = s.kind() == Kind.OFFICER ? SeedPlan.OFFICER_PASSWORD : SeedPlan.MANAGER_PASSWORD;
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", s.username());
        body.put("firstname", s.firstname());
        body.put("lastname", s.lastname());
        body.put("email", s.username() + "@adey.example");
        body.put("officeId", staffOffice.get(s.key()));
        body.put("staffId", staffIds.get(s.key()));
        body.put("roles", List.of(roleId));
        body.put("sendPasswordToEmail", false);
        body.put("password", password);
        body.put("repeatPassword", password);
        body.put("passwordNeverExpires", true);
        fineract.postJson("/users", body);
        created++;
        log.info("User '{}' created ({} role)", s.username(), s.kind());
    }

    // ---- Clients -------------------------------------------------------------------------

    private long ensureClient(Client c) {
        JsonNode page = fineract.getJson("/clients?externalId={ext}", c.externalId());
        JsonNode items = page.path("pageItems");
        if (items.size() > 0) {
            long id = items.get(0).path("id").asLong();
            ensureGender(id, c);
            return id;
        }
        Map<String, Object> body = new LinkedHashMap<>(withDateFormat(Map.of(
                "officeId", staffOffice.get(c.officerKey()),
                "staffId", staffIds.get(c.officerKey()),
                "firstname", c.firstname(), "lastname", c.lastname(),
                "externalId", c.externalId(), "mobileNo", c.mobileNo(),
                "dateOfBirth", FineractDates.format(c.dateOfBirth()),
                "legalFormId", 1, "active", true)));
        body.put("genderId", genders.idOf(c.gender()));
        body.put("activationDate", FineractDates.format(c.activatedOn()));
        body.put("submittedOnDate", FineractDates.format(c.activatedOn()));
        long id = fineract.postJson("/clients", body).path("resourceId").asLong();
        created++;
        log.info("Client {} {} created (id {})", c.firstname(), c.lastname(), id);
        return id;
    }

    /** Clients seeded before genders were part of the plan get one (only when it is missing). */
    private void ensureGender(long clientId, Client c) {
        JsonNode client = fineract.getJson("/clients/{id}", clientId);
        if (client.path("gender").path("id").asLong(0) != 0) {
            return;
        }
        fineract.putJson("/clients/{id}", Map.of("genderId", genders.idOf(c.gender())), clientId);
        created++;
        log.info("Client {} {}: gender set to {}", c.firstname(), c.lastname(), c.gender());
    }

    // ---- Loans ---------------------------------------------------------------------------

    private void ensureLoan(long clientId, Client c, FineractLoanProduct product, long cashId) {
        Loan plan = c.loan();
        long loanId = fineract.getClientAccounts(clientId).loanAccountsOrEmpty().stream()
                .filter(a -> plan.externalId().equals(a.externalId()))
                .map(LoanAccount::id)
                .findFirst()
                .orElseGet(() -> submit(clientId, c, product));

        LoanStatus status = statusOf(loanId);
        Scenario target = plan.scenario();

        if (status == LoanStatus.SUBMITTED_AND_PENDING_APPROVAL && target == Scenario.REJECTED) {
            fineract.loanCommand(loanId, "reject", new FineractLoanCommands.Reject(
                    FineractDates.format(plan.submittedOn().plusDays(3)),
                    "Debt-to-income too high for the requested amount", FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            created++;
            status = statusOf(loanId);
        }
        if (status == LoanStatus.SUBMITTED_AND_PENDING_APPROVAL && needsApproval(target)) {
            fineract.loanCommand(loanId, "approve", new FineractLoanCommands.Approve(
                    FineractDates.format(plan.approvedOn()), null, "Approved (demo seed)",
                    FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            created++;
            status = statusOf(loanId);
        }
        if (status == LoanStatus.APPROVED && plan.disbursedOn() != null) {
            fineract.loanCommand(loanId, "disburse", new FineractLoanCommands.Disburse(
                    FineractDates.format(plan.disbursedOn()), null, cashId, "Cash handed over (demo seed)",
                    FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            created++;
        }
        applyPlannedRepayments(loanId, plan, cashId);
    }

    private long submit(long clientId, Client c, FineractLoanProduct product) {
        Loan plan = c.loan();
        LocalDate expected = plan.disbursedOn() != null ? plan.disbursedOn()
                : (plan.approvedOn() != null ? plan.approvedOn() : plan.submittedOn()).plusDays(12);
        var request = LoanService.buildLoanRequest(clientId, product, plan.principal(), plan.repayments(),
                expected, plan.submittedOn(), staffIds.get(c.officerKey()), plan.externalId());
        var result = fineract.submitLoanApplication(request);
        created++;
        long loanId = result.loanId() != null ? result.loanId() : result.resourceId();
        log.info("Loan {} submitted for {} {} ({} ETB / {} months)", loanId, c.firstname(), c.lastname(),
                plan.principal(), plan.repayments());
        return loanId;
    }

    private static boolean needsApproval(Scenario target) {
        return target != Scenario.PENDING && target != Scenario.REJECTED && target != Scenario.NO_LOAN;
    }

    /**
     * The repayments a scenario calls for, derived from the loan's own schedule, each on the
     * installment's due date. Only the ones not yet recorded are posted (idempotent).
     */
    private void applyPlannedRepayments(long loanId, Loan plan, long cashId) {
        FineractLoan loan = fineract.getLoanWithScheduleAndTransactions(loanId);
        if (loan.repaymentSchedule() == null) {
            return;
        }
        List<Period> installments = loan.repaymentSchedule().installments();
        LocalDate today = FineractDates.today();

        record Payment(LocalDate date, BigDecimal amount) { }
        List<Payment> planned = new ArrayList<>();
        switch (plan.scenario()) {
            case ACTIVE_ON_TIME -> installments.stream()
                    .filter(p -> !FineractDates.fromArray(p.dueDate()).isAfter(today))
                    .forEach(p -> planned.add(new Payment(FineractDates.fromArray(p.dueDate()), p.totalDueForPeriod())));
            case CLOSED -> installments
                    .forEach(p -> planned.add(new Payment(FineractDates.fromArray(p.dueDate()), p.totalDueForPeriod())));
            case ACTIVE_PARTLY_PAID -> {
                for (int i = 0; i < plan.installmentsPaidInFull(); i++) {
                    Period p = installments.get(i);
                    planned.add(new Payment(FineractDates.fromArray(p.dueDate()), p.totalDueForPeriod()));
                }
                Period next = installments.get(plan.installmentsPaidInFull());
                planned.add(new Payment(FineractDates.fromArray(next.dueDate()), plan.partialPayment()));
            }
            default -> { }
        }

        long alreadyPaid = loan.transactions() == null ? 0 : loan.transactions().stream()
                .filter(tx -> !Boolean.TRUE.equals(tx.manuallyReversed()))
                .filter(DemoSeeder::isRepayment)
                .count();
        for (int i = (int) alreadyPaid; i < planned.size(); i++) {
            Payment pay = planned.get(i);
            fineract.repayLoan(loanId, new FineractLoanCommands.Repayment(FineractDates.format(pay.date()),
                    pay.amount(), cashId, "Repayment (demo seed)", null, FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            created++;
        }
        if (planned.size() > alreadyPaid) {
            log.info("Loan {}: posted {} repayment(s)", loanId, planned.size() - alreadyPaid);
        }
    }

    private static boolean isRepayment(FineractLoanTransaction tx) {
        return tx.type() != null && "loanTransactionType.repayment".equals(tx.type().code());
    }

    private LoanStatus statusOf(long loanId) {
        FineractLoan loan = fineract.getLoanWithSchedule(loanId);
        return LoanStatus.fromFineractId(loan.status() == null ? null : loan.status().id());
    }

    // ---- Summary -------------------------------------------------------------------------

    private void printSummary() {
        log.info(String.format("%-9s %-22s %-10s %-20s %-30s %12s %10s %s", "client", "name", "officer",
                "plan", "loan status", "outstanding", "overdue", "arrears"));
        for (Client c : SeedPlan.CLIENTS) {
            JsonNode items = fineract.getJson("/clients?externalId={ext}", c.externalId()).path("pageItems");
            long clientId = items.get(0).path("id").asLong();
            Optional<LoanAccount> account = c.loan() == null ? Optional.empty()
                    : fineract.getClientAccounts(clientId).loanAccountsOrEmpty().stream()
                    .filter(a -> c.loan().externalId().equals(a.externalId())).findFirst();
            String status = "-", outstanding = "", overdue = "", arrears = "";
            if (account.isPresent()) {
                FineractLoan loan = fineract.getLoanWithSchedule(account.get().id());
                status = "#" + loan.id() + " " + loan.status().value();
                if (loan.summary() != null) {
                    outstanding = String.valueOf(loan.summary().totalOutstanding());
                    overdue = String.valueOf(loan.summary().totalOverdue());
                    arrears = String.valueOf(loan.isInArrears());
                }
            }
            log.info(String.format("%-9s %-22s %-10s %-20s %-30s %12s %10s %s", clientId,
                    c.firstname() + " " + c.lastname(), c.officerKey(),
                    c.loan() == null ? Scenario.NO_LOAN : c.loan().scenario(), status, outstanding, overdue, arrears));
        }
    }

    // ---- Helpers -------------------------------------------------------------------------

    private static Optional<JsonNode> find(JsonNode array, String field, String value) {
        for (JsonNode n : array) {
            if (value.equals(n.path(field).asString())) {
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    private static Optional<JsonNode> findStaffByName(JsonNode array, String firstname, String lastname) {
        for (JsonNode n : array) {
            if (firstname.equals(n.path("firstname").asString().trim())
                    && lastname.equals(n.path("lastname").asString().trim())) {
                return Optional.of(n);
            }
        }
        return Optional.empty();
    }

    private static Map<String, Object> withDateFormat(Map<String, Object> body) {
        Map<String, Object> copy = new LinkedHashMap<>(body);
        copy.put("dateFormat", FineractDates.DATE_FORMAT);
        copy.put("locale", FineractDates.LOCALE);
        return copy;
    }
}
