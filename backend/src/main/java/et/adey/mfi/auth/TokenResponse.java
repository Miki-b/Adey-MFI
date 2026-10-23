package et.adey.mfi.auth;

import java.time.Instant;

/**
 * Login/refresh result. The app sends accessToken as "Authorization: Bearer <accessToken>" and
 * keeps refreshToken in secure storage to get a new pair when the access token expires.
 */
public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        OfficerResponse officer) {
}
