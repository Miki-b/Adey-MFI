package et.adey.mfi.auth;

import et.adey.mfi.security.Officer;
import et.adey.mfi.security.Role;

import java.util.List;

/** The logged-in officer as the app sees it (GET /api/auth/me and the login response). */
public record OfficerResponse(long userId, String username, String displayName, long staffId,
                              long officeId, String officeName, List<Role> roles) {

    public static OfficerResponse of(Officer o) {
        return new OfficerResponse(o.userId(), o.username(), o.displayName(), o.staffId(), o.officeId(),
                o.officeName(), o.roles().stream().sorted().toList());
    }
}
