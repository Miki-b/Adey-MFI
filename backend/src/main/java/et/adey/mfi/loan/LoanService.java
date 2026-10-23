package et.adey.mfi.loan;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import et.adey.mfi.fineract.model.FineractLoanRequest;
import et.adey.mfi.security.Officer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Read side of loans: lists, details and schedule previews. Nothing here changes data. */
@Service
public class LoanService {

    private final FineractClient fineract;
    private final LoanGuard guard;

    public LoanService(FineractClient fineract, LoanGuard guard) {
        this.fineract = fineract;
        this.guard = guard;
    }

    /**
     * Fineract's account list only has ids, so we fetch each loan with its schedule.
     * (One extra call per loan; fine for the handful of loans a client has.)
     */
    public List<LoanSummary> getClientLoans(Officer officer, long clientId) {
        guard.requireClient(officer, clientId);
        return fineract.getClientAccounts(clientId).loanAccountsOrEmpty().stream()
                .map(account -> fineract.getLoanWithSchedule(account.id()))
                .map(LoanMapper::toSummary)
                .toList();
    }

    public LoanDetails getLoan(Officer officer, long loanId) {
        FineractLoan loan = fineract.getLoanWithScheduleAndTransactions(loanId);
        guard.requireClient(officer, loan.clientId());
        return LoanMapper.toDetails(loan);
    }

    /**
     * The product holds the loan's terms (rate, frequency, amortization, ...). We copy them into
     * the request so the preview uses exactly what a real application for this product would get.
     */
    public SchedulePreviewResponse previewSchedule(Officer officer, SchedulePreviewRequest req) {
        guard.requireClient(officer, req.clientId());
        FineractLoanProduct product = fineract.getLoanProduct(req.productId());
        FineractLoanRequest request = buildLoanRequest(req.clientId(), product, req.principal(),
                req.numberOfRepayments(), req.expectedDisbursementDate(), null);
        return LoanMapper.toPreview(fineract.calculateLoanSchedule(request));
    }

    /**
     * Builds Fineract's loan request from our few inputs plus the product's terms.
     * Shared by schedule preview and loan application, so both always agree.
     */
    public static FineractLoanRequest buildLoanRequest(long clientId, FineractLoanProduct product, BigDecimal principal,
                                                       int numberOfRepayments, LocalDate expectedDisbursementDate,
                                                       Long loanOfficerId) {
        LocalDate today = FineractDates.today();
        LocalDate disbursement = expectedDisbursementDate != null ? expectedDisbursementDate : today;
        // Fineract rejects a submission date after the disbursement date (back-dated applications).
        LocalDate submitted = disbursement.isBefore(today) ? disbursement : today;
        return buildLoanRequest(clientId, product, principal, numberOfRepayments, disbursement, submitted,
                loanOfficerId, null);
    }

    /** Full version: explicit submission date and externalId (used by the demo seeder). */
    public static FineractLoanRequest buildLoanRequest(long clientId, FineractLoanProduct product, BigDecimal principal,
                                                       int numberOfRepayments, LocalDate disbursement,
                                                       LocalDate submitted, Long loanOfficerId, String externalId) {
        int repaymentEvery = product.repaymentEvery() == null ? 1 : product.repaymentEvery();
        Integer frequencyType = product.repaymentFrequencyType().id();

        return new FineractLoanRequest(
                clientId,
                product.id(),
                loanOfficerId,
                externalId,
                principal,
                // Loan term = number of repayments x repayment interval, in the same unit.
                numberOfRepayments * repaymentEvery,
                frequencyType,
                numberOfRepayments,
                repaymentEvery,
                frequencyType,
                // Rate is per the product's interest-rate period (per year for SBL).
                product.interestRatePerPeriod(),
                product.amortizationType().id(),
                product.interestType().id(),
                product.interestCalculationPeriodType().id(),
                product.transactionProcessingStrategyCode(),
                FineractDates.format(disbursement),
                FineractDates.format(submitted),
                "individual",
                FineractDates.DATE_FORMAT,
                FineractDates.LOCALE);
    }
}
