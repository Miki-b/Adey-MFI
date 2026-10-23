package et.adey.mfi.security;

import java.util.Optional;

/**
 * Our two roles. They come from the user's Fineract role at login:
 * Fineract "Loan Officer" -> OFFICER, "Branch Manager" -> MANAGER.
 * Spring Security expects role authorities to be named "ROLE_...".
 */
public enum Role {
    OFFICER("Loan Officer"),
    MANAGER("Branch Manager");

    private final String fineractRoleName;

    Role(String fineractRoleName) {
        this.fineractRoleName = fineractRoleName;
    }

    public String authority() {
        return "ROLE_" + name();
    }

    public static Optional<Role> fromFineractRoleName(String name) {
        for (Role role : values()) {
            if (role.fineractRoleName.equalsIgnoreCase(name)) {
                return Optional.of(role);
            }
        }
        return Optional.empty();
    }
}
