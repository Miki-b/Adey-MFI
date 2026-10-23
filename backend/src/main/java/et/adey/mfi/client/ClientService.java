package et.adey.mfi.client;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.common.PageResponse;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractPage;
import et.adey.mfi.loan.LoanGuard;
import et.adey.mfi.security.AccessPolicy;
import et.adey.mfi.security.Officer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ClientService {

    private final FineractClient fineract;
    private final AccessPolicy access;
    private final LoanGuard guard;

    public ClientService(FineractClient fineract, AccessPolicy access, LoanGuard guard) {
        this.fineract = fineract;
        this.access = access;
        this.guard = guard;
    }

    /**
     * page is 0-based; Fineract wants offset = page * size. The office/staff filter comes from the
     * token (AccessPolicy.clientScope), so an officer cannot list someone else's clients.
     */
    public PageResponse<ClientSummary> search(Officer officer, String search, int page, int size) {
        String name = StringUtils.hasText(search) ? search.trim() : null;
        AccessPolicy.ClientScope scope = access.clientScope(officer);
        FineractPage<FineractClientData> result =
                fineract.searchClients(name, scope.officeId(), scope.staffId(), page * size, size);
        return PageResponse.of(result.itemsOrEmpty(), ClientService::toSummary,
                page, size, result.totalFilteredRecords());
    }

    public ClientDetails getClient(Officer officer, long clientId) {
        return toDetails(guard.requireClient(officer, clientId));
    }

    static ClientSummary toSummary(FineractClientData c) {
        return new ClientSummary(c.id(), c.displayName(), c.accountNo(), c.officeName(),
                clean(c.staffName()), Boolean.TRUE.equals(c.active()), FineractDates.fromArray(c.activationDate()));
    }

    static ClientDetails toDetails(FineractClientData c) {
        return new ClientDetails(
                c.id(),
                c.accountNo(),
                c.externalId(),
                c.displayName(),
                c.firstname(),
                c.middlename(),
                c.lastname(),
                c.mobileNo(),
                FineractDates.fromArray(c.dateOfBirth()),
                c.gender() == null ? null : c.gender().value(),
                c.status() == null ? null : c.status().value(),
                Boolean.TRUE.equals(c.active()),
                c.timeline() == null ? null : FineractDates.fromArray(c.timeline().submittedOnDate()),
                FineractDates.fromArray(c.activationDate()),
                c.officeId(),
                c.officeName(),
                c.staffId(),
                clean(c.staffName()));
    }

    /** Fineract keeps stray spaces from data entry ("Derese, Samuel "). */
    private static String clean(String s) {
        return s == null ? null : s.trim();
    }
}
