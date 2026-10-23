package et.adey.mfi.security;

import java.util.Set;

/**
 * The logged-in user, as read from a verified access token. Controllers receive it with
 * {@code @AuthenticationPrincipal Officer officer}. staffId/officeId here are the ONLY source of
 * "who am I" for data scoping; we never take them from query parameters.
 */
public record Officer(
        long userId,
        String username,
        String displayName,
        long staffId,
        long officeId,
        String officeName,
        Set<Role> roles) {

    public boolean isManager() {
        return roles.contains(Role.MANAGER);
    }
}
