package et.adey.mfi;

import et.adey.mfi.security.Officer;
import et.adey.mfi.security.OfficerJwtConverter.OfficerAuthentication;
import et.adey.mfi.security.Role;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

/** Officers matching the seed data, plus a MockMvc helper to send a request "as" one of them. */
public final class TestOfficers {

    private TestOfficers() {
    }

    /** Samuel: loan officer, staff 1, Bishoftu Branch (office 2). */
    public static final Officer SAMUEL = new Officer(4, "samuel", "Samuel Derese", 1, 2, "Bishoftu Branch",
            Set.of(Role.OFFICER));
    /** Hana: loan officer, staff 2, Bishoftu Branch. */
    public static final Officer HANA = new Officer(5, "hana", "Hana Girma", 2, 2, "Bishoftu Branch",
            Set.of(Role.OFFICER));
    /** Tadesse: branch manager, staff 3, Bishoftu Branch. */
    public static final Officer TADESSE = new Officer(6, "tadesse", "Tadesse Bekele", 3, 2, "Bishoftu Branch",
            Set.of(Role.MANAGER));

    /** Meron: loan officer, staff 4, Adama Branch (office 3). */
    public static final Officer MERON = new Officer(7, "meron", "Meron Alemu", 4, 3, "Adama Branch",
            Set.of(Role.OFFICER));
    /** Selamawit: branch manager, staff 6, Adama Branch. */
    public static final Officer SELAMAWIT = new Officer(9, "selamawit", "Selamawit Tesfaye", 6, 3, "Adama Branch",
            Set.of(Role.MANAGER));

    /** Skips the token: puts the officer straight into the security context for one MockMvc request. */
    public static RequestPostProcessor as(Officer officer) {
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "none").subject(officer.username())
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        List<org.springframework.security.core.GrantedAuthority> authorities = officer.roles().stream()
                .map(r -> (org.springframework.security.core.GrantedAuthority) new SimpleGrantedAuthority(r.authority()))
                .toList();
        return authentication(new OfficerAuthentication(officer, jwt, authorities));
    }
}
