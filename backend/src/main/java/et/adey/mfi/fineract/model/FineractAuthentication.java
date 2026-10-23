package et.adey.mfi.fineract.model;

import java.util.List;

/**
 * Answer of POST /authentication for a correct username + password.
 * Fineract also returns "base64EncodedAuthenticationKey" (= base64 of username:password) and the
 * full permission list. We deliberately have NO field for the key, so it is dropped while the
 * JSON is read and can never be stored or logged.
 */
public record FineractAuthentication(
        String username,
        Long userId,
        Boolean authenticated,
        Long officeId,
        String officeName,
        Long staffId,
        String staffDisplayName,
        List<FineractRole> roles,
        Boolean shouldRenewPassword) {
}
