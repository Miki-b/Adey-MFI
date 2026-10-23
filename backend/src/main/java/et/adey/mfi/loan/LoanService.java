package et.adey.mfi.loan;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import et.adey.mfi.fineract.model.FineractScheduleRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final FineractClient fineract;

    public LoanService(FineractClient fineract) {
        this.fineract = fineract;
    }

    /**
     * Fineract's account list only has ids, so we fetch each loan with its schedule.
     * (One extra call per loan; fine for the handful of loans a client has.)
     */
    public List<LoanSummary> getClientLoans(long clientId) {
        return fineract.getClientAccounts(clientId).loanAccountsOrEmpty().stream()
                .map(account -> fineract.getLoanWithSchedule(account.id()))
                .map(LoanMapper::toSummary)
                .toList();
    }

    /**
     * The product holds the loan's terms (rate, frequency, amortization, ...). We copy them into
     * the request so the preview uses exactly what a real application for this product would get.
     */
    public SchedulePreviewResponse previewSchedule(SchedulePreviewRequest req) {
        FineractLoanProduct product = fineract.getLoanProduct(req.productId());
        return LoanMapper.toPreview(fineract.calculateLoanSchedule(buildScheduleRequest(req, product)));
    }

    static FineractScheduleRequest buildScheduleRequest(SchedulePreviewRequest req, FineractLoanProduct product) {
        LocalDate today = FineractDates.today();
        LocalDate disbursement = req.expectedDisbursementDate() != null ? req.expectedDisbursementDate() : today;
        // Fineract rejects a submission date after the disbursement date (back-dated previews).
        LocalDate submitted = disbursement.isBefore(today) ? disbursement : today;

        int repaymentEvery = product.repaymentEvery() == null ? 1 : product.repaymentEvery();
        Integer frequencyType = product.repaymentFrequencyType().id();

        return new FineractScheduleRequest(
                req.clientId(),
                req.productId(),
                req.principal(),
                // Loan term = number of repayments x repayment interval, in the same unit.
                req.numberOfRepayments() * repaymentEvery,
                frequencyType,
                req.numberOfRepayments(),
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
