package et.adey.mfi.loan;

import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanSchedule;
import et.adey.mfi.fineract.model.FineractLoanTransaction;
import org.junit.jupiter.api.Test;
import et.adey.mfi.TestJson;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Maps saved Fineract responses (src/test/resources/fineract) to our DTOs. */
class LoanMapperTest {

    // Same lenient settings Spring Boot uses (unknown fields are ignored).
    private final ObjectMapper objectMapper = TestJson.mapper();

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

    /**
     * Real loan 1 captured from Fineract after the Phase 2 walkthrough: 20,000 disbursed
     * 2026-10-23, repaid 1833.60 (installment 1 in full) and then 1000 (part of installment 2).
     */
    @Test
    void activeLoanSummaryCountsPaidInstallmentsAndFindsNextDue() throws IOException {
        FineractLoan loan = read("fineract/loan-1-active-with-schedule-and-transactions.json", FineractLoan.class);

        LoanSummary summary = LoanMapper.toSummary(loan);

        assertThat(summary.loanId()).isEqualTo(1L);
        assertThat(summary.status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(summary.statusLabel()).isEqualTo("Active");
        assertThat(summary.principal()).isEqualTo(new BigDecimal("20000.00"));
        assertThat(summary.annualInterestRate()).isEqualTo(new BigDecimal("18.00"));
        assertThat(summary.installmentsTotal()).isEqualTo(12);
        assertThat(summary.installmentsPaid()).isEqualTo(1);
        assertThat(summary.principalOutstanding()).isEqualTo(new BigDecimal("17743.40"));
        assertThat(summary.totalOutstanding()).isEqualTo(new BigDecimal("19169.61"));
        // Installment 2 is partly paid (1000 of 1833.60): next due is what is still owed on it.
        assertThat(summary.nextDueDate()).isEqualTo(LocalDate.of(2026, 12, 23));
        assertThat(summary.nextDueAmount()).isEqualTo(new BigDecimal("833.60"));
        assertThat(summary.inArrears()).isFalse();
    }

    @Test
    void loanDetailsHaveTotalsScheduleAndMoneyTransactionsOnly() throws IOException {
        FineractLoan loan = read("fineract/loan-1-active-with-schedule-and-transactions.json", FineractLoan.class);

        LoanDetails details = LoanMapper.toDetails(loan);

        assertThat(details.clientName()).isEqualTo("Yohannes Tekle");
        assertThat(details.loanOfficerName()).isEqualTo("Derese, Samuel");
        assertThat(details.dates().disbursedOn()).isEqualTo(LocalDate.of(2026, 10, 23));
        assertThat(details.dates().expectedMaturity()).isEqualTo(LocalDate.of(2027, 10, 23));

        assertThat(details.summary().interestCharged()).isEqualTo(new BigDecimal("2003.21"));
        assertThat(details.summary().principalPaid()).isEqualTo(new BigDecimal("2256.60"));
        assertThat(details.summary().interestPaid()).isEqualTo(new BigDecimal("577.00"));
        assertThat(details.summary().principalOutstanding()).isEqualTo(new BigDecimal("17743.40"));
        assertThat(details.summary().totalOverdue()).isEqualTo(new BigDecimal("0.00"));

        assertThat(details.schedule()).hasSize(12);
        LoanDetails.Installment second = details.schedule().get(1);
        assertThat(second.paid()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(second.outstanding()).isEqualTo(new BigDecimal("833.60"));
        assertThat(second.complete()).isFalse();

        // Fineract's 4 transactions include an accrual (bookkeeping); we only show money movements.
        assertThat(details.transactions()).extracting(LoanTransactionResponse::type)
                .containsExactly("DISBURSEMENT", "REPAYMENT", "REPAYMENT");
        assertThat(details.transactions().get(0).amount()).isEqualTo(new BigDecimal("20000.00"));
    }

    /** Real transaction 4: 1000 paid while installment 2 owed 277.00 interest + 1556.60 principal. */
    @Test
    void repaymentSplitFollowsPenaltiesFeesInterestPrincipal() throws IOException {
        FineractLoanTransaction tx = read("fineract/loan-1-transaction-4-partial-repayment.json",
                FineractLoanTransaction.class);

        LoanTransactionResponse response = LoanMapper.toTransaction(tx);

        assertThat(response.type()).isEqualTo("REPAYMENT");
        assertThat(response.amount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(response.penaltiesPortion()).isEqualTo(new BigDecimal("0.00"));
        assertThat(response.feesPortion()).isEqualTo(new BigDecimal("0.00"));
        assertThat(response.interestPortion()).isEqualTo(new BigDecimal("277.00"));
        assertThat(response.principalPortion()).isEqualTo(new BigDecimal("723.00"));
        assertThat(response.outstandingBalanceAfter()).isEqualTo(new BigDecimal("17743.40"));
        assertThat(response.paymentType()).isEqualTo("Money Transfer");
        // The portions always add up to the amount paid.
        assertThat(response.principalPortion().add(response.interestPortion())
                .add(response.feesPortion()).add(response.penaltiesPortion())
                .add(response.overpaymentPortion())).isEqualByComparingTo(response.amount());
    }

    @Test
    void transactionTypeCodeIsUpperSnakeCase() {
        assertThat(LoanMapper.typeCode(new FineractLoanTransaction.Type(2, "loanTransactionType.repayment",
                "Repayment", false, false, false))).isEqualTo("REPAYMENT");
        assertThat(LoanMapper.typeCode(new FineractLoanTransaction.Type(5, "loanTransactionType.repaymentAtDisbursement",
                "Repayment at disbursement", false, false, false))).isEqualTo("REPAYMENT_AT_DISBURSEMENT");
        assertThat(LoanMapper.typeCode(null)).isEqualTo("UNKNOWN");
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
