package et.adey.mfi.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * After Spring Security has verified a token, this turns its claims into our Officer and the
 * ROLE_OFFICER / ROLE_MANAGER authorities. The result is stored in the SecurityContext for the
 * duration of the request.
 */
@Component
public class OfficerJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Officer officer = toOfficer(jwt);
        List<GrantedAuthority> authorities = officer.roles().stream()
                .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role.authority()))
                .toList();
        return new OfficerAuthentication(officer, jwt, authorities);
    }

    static Officer toOfficer(Jwt jwt) {
        Set<Role> roles = EnumSet.noneOf(Role.class);
        List<String> roleNames = jwt.getClaimAsStringList(TokenService.CLAIM_ROLES);
        if (roleNames != null) {
            roleNames.forEach(r -> roles.add(Role.valueOf(r)));
        }
        return new Officer(
                ((Number) jwt.getClaim(TokenService.CLAIM_USER_ID)).longValue(),
                jwt.getSubject(),
                jwt.getClaimAsString(TokenService.CLAIM_NAME),
                ((Number) jwt.getClaim(TokenService.CLAIM_STAFF_ID)).longValue(),
                ((Number) jwt.getClaim(TokenService.CLAIM_OFFICE_ID)).longValue(),
                jwt.getClaimAsString(TokenService.CLAIM_OFFICE_NAME),
                Set.copyOf(roles));
    }

    /** An authenticated request: principal = Officer, credentials = the verified JWT. */
    public static class OfficerAuthentication extends AbstractAuthenticationToken {

        private final Officer officer;
        private final Jwt jwt;

        public OfficerAuthentication(Officer officer, Jwt jwt, List<GrantedAuthority> authorities) {
            super(authorities);
            this.officer = officer;
            this.jwt = jwt;
            setAuthenticated(true);
        }

        @Override
        public Object getPrincipal() {
            return officer;
        }

        @Override
        public Object getCredentials() {
            return jwt;
        }

        @Override
        public String getName() {
            return officer.username();
        }
    }
}
