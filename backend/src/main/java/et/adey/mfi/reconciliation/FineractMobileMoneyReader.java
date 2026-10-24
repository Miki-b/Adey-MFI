package et.adey.mfi.reconciliation;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractPage;
import et.adey.mfi.mobilemoney.MobileMoneyProperties;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * Fineract's side for reconciliation: all non-reversed "Mobile Money" repayments on a date.
 * Fineract has no "all transactions of a day" search, so we list the loans (paged) and read the
 * transactions of every loan that can hold repayments (active, closed, overpaid), 8 at a time.
 * Fine for a branch; at larger scale use a Fineract report (/runreports) or a nightly export.
 */
@Component
public class FineractMobileMoneyReader {

    private static final Set<Integer> LOANS_WITH_REPAYMENTS = Set.of(300, 600, 700);

    private final FineractClient fineract;
    private final MobileMoneyProperties props;

    public FineractMobileMoneyReader(FineractClient fineract, MobileMoneyProperties props) {
        this.fineract = fineract;
        this.props = props;
    }

    public List<ReconciliationMatcher.FineractPayment> read(LocalDate date) {
        List<FineractLoan> loans = new ArrayList<>();
        for (int offset = 0; ; offset += 200) {
            FineractPage<FineractLoan> page = fineract.listLoans(offset, 200);
            page.itemsOrEmpty().stream()
                    .filter(l -> l.status() != null && LOANS_WITH_REPAYMENTS.contains(l.status().id()))
                    .forEach(loans::add);
            if (page.itemsOrEmpty().size() < 200) break;
        }
        List<ReconciliationMatcher.FineractPayment> result = new ArrayList<>();
        Semaphore permits = new Semaphore(8);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<List<ReconciliationMatcher.FineractPayment>>> futures = new ArrayList<>();
            for (FineractLoan l : loans) {
                futures.add(executor.submit(() -> {
                    permits.acquire();
                    try {
                        return mobileMoneyRepayments(fineract.getLoanWithScheduleAndTransactions(l.id()),
                                l.clientOfficeId(), date);
                    } finally {
                        permits.release();
                    }
                }));
            }
            for (Future<List<ReconciliationMatcher.FineractPayment>> f : futures) {
                result.addAll(f.get());
            }
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException re) throw re;
            throw new IllegalStateException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        return result;
    }

    private List<ReconciliationMatcher.FineractPayment> mobileMoneyRepayments(FineractLoan loan, Long officeId,
                                                                              LocalDate date) {
        if (loan.transactions() == null) {
            return List.of();
        }
        return loan.transactions().stream()
                .filter(tx -> tx.type() != null && "loanTransactionType.repayment".equals(tx.type().code()))
                .filter(tx -> !Boolean.TRUE.equals(tx.manuallyReversed()))
                .filter(tx -> date.equals(FineractDates.fromArray(tx.date())))
                .filter(tx -> tx.paymentDetailData() != null && tx.paymentDetailData().paymentType() != null
                        && props.paymentTypeName().equalsIgnoreCase(tx.paymentDetailData().paymentType().name()))
                .map(tx -> new ReconciliationMatcher.FineractPayment(loan.id(), officeId, tx.id(),
                        tx.paymentDetailData().receiptNumber(), tx.amount()))
                .toList();
    }
}
