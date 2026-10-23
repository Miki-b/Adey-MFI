package et.adey.mfi.loan;

import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.security.AccessPolicy;
import et.adey.mfi.security.Officer;
import org.springframework.stereotype.Component;

/**
 * Loads a client AND checks the officer may access it, in one step, so no code path can forget
 * the check. A loan is accessible exactly when its client is.
 */
@Component
public class LoanGuard {

    private final FineractClient fineract;
    private final AccessPolicy access;

    public LoanGuard(FineractClient fineract, AccessPolicy access) {
        this.fineract = fineract;
        this.access = access;
    }

    public FineractClientData requireClient(Officer officer, long clientId) {
        FineractClientData client = fineract.getClient(clientId);
        access.requireClientAccess(officer, client);
        return client;
    }
}
