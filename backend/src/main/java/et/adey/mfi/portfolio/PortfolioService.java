package et.adey.mfi.portfolio;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.loan.LoanGuard;
import et.adey.mfi.security.Officer;
import org.springframework.stereotype.Service;

import java.util.List;

/** Loan lists and dashboards: PortfolioReader fetches, PortfolioCalculator computes. */
@Service
public class PortfolioService {

    private final PortfolioReader reader;
    private final LoanGuard guard;

    public PortfolioService(PortfolioReader reader, LoanGuard guard) {
        this.reader = reader;
        this.guard = guard;
    }

    public List<LoanListItem> listLoans(Officer officer, LoanListStatus status, Boolean inArrears, Long clientId) {
        if (clientId != null) {
            guard.requireClient(officer, clientId); // 403 if not the caller's client, like everywhere else
        }
        // Schedules are only needed for active loans (next due, days overdue); skip them for e.g. PENDING.
        boolean needSchedules = status == null || status == LoanListStatus.ACTIVE || inArrears != null;
        PortfolioReader.Scoped scoped = reader.load(officer, needSchedules);
        return PortfolioCalculator.list(scoped.loans(), status, inArrears, clientId, FineractDates.today());
    }

    public Portfolio portfolio(Officer officer) {
        PortfolioReader.Scoped scoped = reader.load(officer, true);
        return PortfolioCalculator.portfolio(officer, scoped.clients(), scoped.loans(), FineractDates.today());
    }
}
