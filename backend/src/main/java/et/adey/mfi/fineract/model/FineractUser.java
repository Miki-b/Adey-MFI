package et.adey.mfi.fineract.model;

import java.util.List;

/** GET /users/{id}: used on token refresh to pick up the user's CURRENT office, staff and roles. */
public record FineractUser(
        Long id,
        String username,
        String firstname,
        String lastname,
        Long officeId,
        String officeName,
        StaffRef staff,
        List<FineractRole> selectedRoles) {

    public record StaffRef(Long id, String displayName, Long officeId) {
    }
}
