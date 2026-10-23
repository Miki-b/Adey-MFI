package et.adey.mfi.security;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Issues access tokens. An access token is a signed JSON document ("JWT"):
 *   header.payload.signature  (each part base64url-encoded)
 * The payload ("claims") says who the officer is and when the token expires. Anyone can READ it,
 * so it holds no secrets, but nobody can CHANGE it without breaking the signature.
 */
@Service
public class TokenService {

    public static final String CLAIM_USER_ID = "uid";
    public static final String CLAIM_STAFF_ID = "staffId";
    public static final String CLAIM_OFFICE_ID = "officeId";
    public static final String CLAIM_OFFICE_NAME = "officeName";
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_ROLES = "roles";

    private final JwtEncoder encoder;
    private final SecurityProperties props;
    private final Clock clock;

    public TokenService(JwtEncoder encoder, SecurityProperties props, Clock clock) {
        this.encoder = encoder;
        this.props = props;
        this.clock = clock;
    }

    public record AccessToken(String value, Instant issuedAt, Instant expiresAt) {
    }

    public AccessToken issue(Officer officer) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(props.jwt().accessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.jwt().issuer())
                .subject(officer.username())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(CLAIM_USER_ID, officer.userId())
                .claim(CLAIM_STAFF_ID, officer.staffId())
                .claim(CLAIM_OFFICE_ID, officer.officeId())
                .claim(CLAIM_OFFICE_NAME, officer.officeName())
                .claim(CLAIM_NAME, officer.displayName())
                .claim(CLAIM_ROLES, officer.roles().stream().map(Enum::name).sorted().toList())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AccessToken(token, now, expiresAt);
    }
}
