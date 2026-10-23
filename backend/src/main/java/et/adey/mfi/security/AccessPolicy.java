package et.adey.mfi.security;

import et.adey.mfi.fineract.model.FineractClientData;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Who may see or change what. One place for the rules, used by every client and loan endpoint:
 * - an OFFICER works only with clients assigned to them (client.staffId == officer's staffId);
 * - a MANAGER works with every client of their own office (client.officeId == manager's officeId);
 * - approve / reject / undo approval / disburse need the MANAGER role.
 * Breaking a rule throws AccessDeniedException -> HTTP 403.
 */
@Component
public class AccessPolicy {

    /** Which clients a search may return: always limited to the caller's office, and for officers to their own. */
    public record ClientScope(long officeId, Long staffId) {
    }

    public ClientScope clientScope(Officer officer) {
        return officer.isManager()
                ? new ClientScope(officer.officeId(), null)
                : new ClientScope(officer.officeId(), officer.staffId());
    }

    public void requireClientAccess(Officer officer, FineractClientData client) {
        boolean sameOffice = Objects.equals(client.officeId(), officer.officeId());
        boolean allowed = officer.isManager()
                ? sameOffice
                : sameOffice && Objects.equals(client.staffId(), officer.staffId());
        if (!allowed) {
            throw new AccessDeniedException(officer.isManager()
                    ? "Client %d belongs to another office.".formatted(client.id())
                    : "Client %d is not assigned to you.".formatted(client.id()));
        }
    }

    public void requireManager(Officer officer) {
        if (!officer.isManager()) {
            throw new AccessDeniedException("Only a branch manager can do this.");
        }
    }
}
