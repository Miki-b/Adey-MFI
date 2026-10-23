package et.adey.mfi.portfolio;

import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanSchedule;
import et.adey.mfi.fineract.model.FineractPage;
import et.adey.mfi.loan.LoanStatus;
import et.adey.mfi.security.AccessPolicy;
import et.adey.mfi.security.Officer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * Loads "all loans the caller may see" with as few Fineract calls as possible:
 *
 *   1. the caller's clients: GET /clients?officeId=..[&staffId=..]  (1 call per 200 clients)
 *   2. all loans with summaries: GET /loans                          (1 call per 200 loans)
 *      -> kept only if the loan's client is in (1), the same rule as everywhere else
 *   3. schedules, ACTIVE loans in scope only: GET /loans/{id}?associations=repaymentSchedule
 *      (1 call per active loan, run in parallel, at most 8 at a time, on Java 21 virtual threads)
 *
 * Fineract's loan list has no office/officer filter and no schedules, hence (1) and (3).
 * No caching: each dashboard load reads fresh data (see notes/05-portfolio-and-par.md).
 */
@Slf4j
@Component
public class PortfolioReader {

    static final int PAGE_SIZE = 200;
    static final int MAX_PARALLEL_SCHEDULE_CALLS = 8;

    private final FineractClient fineract;
    private final AccessPolicy access;

    public PortfolioReader(FineractClient fineract, AccessPolicy access) {
        this.fineract = fineract;
        this.access = access;
    }

    /** The caller's clients and their loans. withSchedules=false skips step 3. */
    public Scoped load(Officer officer, boolean withSchedules) {
        long started = System.nanoTime();
        AccessPolicy.ClientScope scope = access.clientScope(officer);

        Map<Long, FineractClientData> clients = new LinkedHashMap<>();
        for (int offset = 0; ; offset += PAGE_SIZE) {
            FineractPage<FineractClientData> page =
                    fineract.searchClients(null, scope.officeId(), scope.staffId(), offset, PAGE_SIZE);
            page.itemsOrEmpty().forEach(c -> clients.put(c.id(), c));
            if (page.itemsOrEmpty().size() < PAGE_SIZE) break;
        }

        List<FineractLoan> loans = new ArrayList<>();
        for (int offset = 0; ; offset += PAGE_SIZE) {
            FineractPage<FineractLoan> page = fineract.listLoans(offset, PAGE_SIZE);
            page.itemsOrEmpty().stream().filter(l -> clients.containsKey(l.clientId())).forEach(loans::add);
            if (page.itemsOrEmpty().size() < PAGE_SIZE) break;
        }

        Map<Long, FineractLoanSchedule> schedules = withSchedules
                ? fetchSchedules(loans.stream()
                        .filter(l -> LoanStatus.fromFineractId(l.status() == null ? null : l.status().id()) == LoanStatus.ACTIVE)
                        .map(FineractLoan::id).toList())
                : Map.of();

        List<LoanView> views = loans.stream()
                .map(l -> new LoanView(l, schedules.get(l.id()), clients.get(l.clientId())))
                .toList();
        log.debug("Portfolio for {}: {} clients, {} loans, {} schedules in {} ms", officer.username(),
                clients.size(), views.size(), schedules.size(), (System.nanoTime() - started) / 1_000_000);
        return new Scoped(clients, views);
    }

    public record Scoped(Map<Long, FineractClientData> clients, List<LoanView> loans) {
    }

    /**
     * Virtual threads are cheap Java 21 threads, ideal for waiting on HTTP. The semaphore keeps
     * at most 8 requests in flight so we don't flood Fineract.
     */
    private Map<Long, FineractLoanSchedule> fetchSchedules(List<Long> loanIds) {
        if (loanIds.isEmpty()) {
            return Map.of();
        }
        Semaphore permits = new Semaphore(MAX_PARALLEL_SCHEDULE_CALLS);
        Map<Long, Future<FineractLoanSchedule>> futures = new LinkedHashMap<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Long id : loanIds) {
                futures.put(id, executor.submit(() -> {
                    permits.acquire();
                    try {
                        return fineract.getLoanWithSchedule(id).repaymentSchedule();
                    } finally {
                        permits.release();
                    }
                }));
            }
            Map<Long, FineractLoanSchedule> result = new HashMap<>();
            for (Map.Entry<Long, Future<FineractLoanSchedule>> f : futures.entrySet()) {
                result.put(f.getKey(), f.getValue().get());
            }
            return result;
        } catch (ExecutionException e) {
            // Re-throw Fineract's own exception so the normal error handling applies.
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException(e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while loading loan schedules", e);
        }
    }
}
