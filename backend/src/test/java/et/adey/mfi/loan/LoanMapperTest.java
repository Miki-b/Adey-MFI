package et.adey.mfi.loan;

import com.fasterxml.jackson.databind.ObjectMapper;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanSchedule;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Maps saved Fineract responses (src/test/resources/fineract) to our DTOs. */
class LoanMapperTest {

    // Same lenient settings Spring Boot uses (unknown fields are ignored).
    private final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();

    @Test
    void schedulePreviewMatchesReferenceSpreadsheet() throws IOException {
        // Real calculateLoanSchedule response: 20,000 ETB, 12 monthly, 18%/yr declining, equal installments.
        FineractLoanSchedule schedule = read("fineract/calculate-schedule-20000-12m.json", FineractLoanSchedule.class);

        SchedulePreviewResponse preview = LoanMapper.toPreview(schedule);

        assertThat(preview.currency()).isEqualTo("ETB");
        assertThat(preview.principal()).isEqualTo(new BigDecimal("20000.00"));
        assertThat(preview.numberOfRepayments()).isEqualTo(12);
        assertThat(preview.expectedDisbursementDate()).isEqualTo(LocalDate.of(2026, 10, 23));
        assertThat(preview.installmentAmount()).isEqualTo(new BigDecimal("1833.60"));
        assertThat(preview.lastInstallmentAmount()).isEqualTo(new BigDecimal("1833.61"));
        assertThat(preview.totalInterest()).isEqualTo(new BigDecimal("2003.21"));
        assertThat(preview.totalRepayable()).isEqualTo(new BigDecimal("22003.21"));

        // The disbursement row is not an installment.
        assertThat(preview.periods()).hasSize(12);
        SchedulePeriod first = preview.periods().get(0);
        assertThat(first.number()).isEqualTo(1);
        assertThat(first.dueDate()).isEqualTo(LocalDate.of(2026, 11, 23));
        assertThat(first.principal()).isEqualTo(new BigDecimal("1533.60"));
        assertThat(first.interest()).isEqualTo(new BigDecimal("300.00"));
        assertThat(first.balanceAfter()).isEqualTo(new BigDecimal("18466.40"));
        assertThat(preview.periods().get(11).balanceAfter()).isEqualTo(new BigDecimal("0.00"));

        // Sanity: principal parts add up to the loan amount, interest parts to total interest.
        BigDecimal principalSum = preview.periods().stream().map(SchedulePeriod::principal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal interestSum = preview.periods().stream().map(SchedulePeriod::interest)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(principalSum).isEqualByComparingTo("20000.00");
        assertThat(interestSum).isEqualByComparingTo("2003.21");
    }

    @Test
    void activeLoanSummaryCountsPaidInstallmentsAndFindsNextDue() throws IOException {
        FineractLoan loan = read("fineract/loan-active-with-schedule.json", FineractLoan.class);

        LoanSummary summary = LoanMapper.toSummary(loan);

        assertThat(summary.loanId()).isEqualTo(7L);
        assertThat(summary.status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(summary.principal()).isEqualTo(new BigDecimal("10000.00"));
        assertThat(summary.annualInterestRate()).isEqualTo(new BigDecimal("18.00"));
        assertThat(summary.installmentsTotal()).isEqualTo(3);
        assertThat(summary.installmentsPaid()).isEqualTo(1);
        assertThat(summary.principalOutstanding()).isEqualTo(new BigDecimal("6716.39"));
        assertThat(summary.totalOutstanding()).isEqualTo(new BigDecimal("6863.37"));
        // Installment 2 is partly paid: next due is what is still owed on it.
        assertThat(summary.nextDueDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(summary.nextDueAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(summary.inArrears()).isTrue();
    }

    @Test
    void pendingLoanHasNoNextDueAndZeroOutstanding() throws IOException {
        String json = """
                {"id": 8, "accountNo": "000000008", "loanProductName": "Small Business Loan",
                 "status": {"id": 100, "value": "Submitted and pending approval"},
                 "principal": 20000.0, "annualInterestRate": 18.0, "numberOfRepayments": 12}
                """;
        FineractLoan loan = objectMapper.readValue(json, FineractLoan.class);

        LoanSummary summary = LoanMapper.toSummary(loan);

        assertThat(summary.status()).isEqualTo(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
        assertThat(summary.installmentsTotal()).isEqualTo(12);
        assertThat(summary.installmentsPaid()).isZero();
        assertThat(summary.totalOutstanding()).isEqualTo(new BigDecimal("0.00"));
        assertThat(summary.nextDueDate()).isNull();
        assertThat(summary.nextDueAmount()).isNull();
        assertThat(summary.inArrears()).isFalse();
    }

    private <T> T read(String path, Class<T> type) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return objectMapper.readValue(in, type);
        }
    }
}
