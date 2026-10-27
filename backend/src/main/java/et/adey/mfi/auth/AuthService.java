package et.adey.mfi.auth;

import et.adey.mfi.audit.AuditOutcome;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.common.Names;
import et.adey.mfi.fineract.FineractApiException;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractAuthentication;
import et.adey.mfi.fineract.model.FineractRole;
import et.adey.mfi.fineract.model.FineractUser;
import et.adey.mfi.security.Officer;
import et.adey.mfi.security.Role;
import et.adey.mfi.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Login, refresh and logout.
 *
 * Passwords are checked by Fineract (POST /authentication), so there is no second user database.
 * We never store the password; after a successful check we issue OUR OWN tokens:
 *   - access token (JWT, 15 min): sent on every API call;
 *   - refresh token (random, 7 days, stored hashed): exchanged for a new pair when the access
 *     token expires.
 */
@Slf4j
@Service
public class AuthService {

    private final FineractClient fineract;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokens;
    private final LoginAttemptLimiter limiter;
    private final AuditService audit;

    public AuthService(FineractClient fineract, TokenService tokenService, RefreshTokenService refreshTokens,
                       LoginAttemptLimiter limiter, AuditService audit) {
        this.fineract = fineract;
        this.tokenService = tokenService;
        this.refreshTokens = refreshTokens;
        this.limiter = limiter;
        this.audit = audit;
    }

    /**
     * 1. refused straight away (429) if this username or IP had too many recent failures;
     * 2. password checked by Fineract; a wrong one counts as a failure;
     * 3. the account must be an Adey officer/manager (403 otherwise; not counted as a guess).
     * Every outcome is written to the audit log.
     */
    public TokenResponse login(String username, String password, String ip) {
        try {
            limiter.checkAllowed(username, ip);
        } catch (TooManyLoginAttemptsException e) {
            audit.recordLogin(username, null, ip, AuditOutcome.LOCKED, "too many failed attempts");
            throw e;
        }
        FineractAuthentication auth;
        try {
            auth = fineract.authenticate(username, password);
        } catch (FineractApiException e) {
            // Same answer for "no such user" and "wrong password", so nobody can probe usernames.
            log.info("Login failed for '{}' from {} (Fineract HTTP {})", username, ip, e.getFineractStatus());
            limiter.recordFailure(username, ip);
            audit.recordLogin(username, null, ip, AuditOutcome.DENIED, "invalid credentials");
            throw new AuthenticationFailedException("INVALID_CREDENTIALS", "Invalid username or password.");
        }
        if (!Boolean.TRUE.equals(auth.authenticated())) {
            limiter.recordFailure(username, ip);
            audit.recordLogin(username, null, ip, AuditOutcome.DENIED, "invalid credentials");
            throw new AuthenticationFailedException("INVALID_CREDENTIALS", "Invalid username or password.");
        }
        Officer officer;
        try {
            officer = toOfficer(auth.userId(), auth.username(), auth.staffId(), auth.staffDisplayName(),
                    auth.officeId(), auth.officeName(), auth.roles());
        } catch (NotAnOfficerException e) {
            audit.recordLogin(username, null, ip, AuditOutcome.DENIED, "not an officer or manager account");
            throw e;
        }
        limiter.recordSuccess(username);
        audit.recordLogin(officer.username(), officer, ip, AuditOutcome.SUCCESS, null);
        log.info("Login: '{}' (staff {}, office {}, roles {})", officer.username(), officer.staffId(),
                officer.officeId(), officer.roles());
        return issueTokens(officer, refreshTokens.issueNewFamily(officer.userId(), officer.username()));
    }

    /** Exchange a refresh token for a new access + refresh token (the old refresh token stops working). */
    public TokenResponse refresh(String rawRefreshToken) {
        RefreshToken previous = refreshTokens.consume(rawRefreshToken);
        // Re-read the user so that role/office/staff changes in Fineract apply from the next refresh.
        FineractUser user = fineract.getUser(previous.getUserId());
        FineractUser.StaffRef staff = user.staff();
        Officer officer = toOfficer(user.id(), user.username(), staff == null ? null : staff.id(),
                staff == null ? null : staff.displayName(), user.officeId(), user.officeName(), user.selectedRoles());
        return issueTokens(officer, refreshTokens.issueInFamily(previous));
    }

    public void logout(String rawRefreshToken) {
        refreshTokens.revoke(rawRefreshToken);
    }

    /**
     * Only users linked to a staff member AND holding one of our roles may use the app.
     * (The Fineract admin "mifos" has neither, so it cannot log in here.)
     */
    static Officer toOfficer(Long userId, String username, Long staffId, String staffDisplayName,
                             Long officeId, String officeName, List<FineractRole> fineractRoles) {
        Set<Role> roles = EnumSet.noneOf(Role.class);
        if (fineractRoles != null) {
            fineractRoles.stream()
                    .filter(r -> !Boolean.TRUE.equals(r.disabled()))
                    .forEach(r -> Role.fromFineractRoleName(r.name()).ifPresent(roles::add));
        }
        if (staffId == null || roles.isEmpty()) {
            throw new NotAnOfficerException();
        }
        return new Officer(userId, username, Names.readable(staffDisplayName), staffId, officeId, officeName,
                Set.copyOf(roles));
    }

    private TokenResponse issueTokens(Officer officer, RefreshTokenService.Issued refresh) {
        TokenService.AccessToken access = tokenService.issue(officer);
        return new TokenResponse(access.value(), "Bearer",
                Duration.between(access.issuedAt(), access.expiresAt()).toSeconds(),
                access.expiresAt(), refresh.rawToken(), refresh.stored().getExpiresAt(), OfficerResponse.of(officer));
    }
}
