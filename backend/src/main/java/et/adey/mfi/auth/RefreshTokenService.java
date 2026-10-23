package et.adey.mfi.auth;

import et.adey.mfi.security.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Refresh tokens: random, opaque (not JWTs), single-use and revocable.
 * - The app gets the raw token once; we store only its SHA-256 hash.
 * - Every refresh ROTATES: the old token is revoked and a new one issued.
 * - Presenting an already-revoked token means it was copied/stolen (the real app would have the
 *   newer one), so the whole family is revoked: thief and officer are both logged out.
 */
@Slf4j
@Service
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final SecurityProperties props;
    private final Clock clock;

    public RefreshTokenService(RefreshTokenRepository repository, SecurityProperties props, Clock clock) {
        this.repository = repository;
        this.props = props;
        this.clock = clock;
    }

    /** A newly issued refresh token: the raw value goes to the app, the row stays here. */
    public record Issued(String rawToken, RefreshToken stored) {
    }

    @Transactional
    public Issued issueNewFamily(long userId, String username) {
        return issue(UUID.randomUUID().toString(), userId, username);
    }

    /**
     * Validates and consumes a refresh token; returns the row it belonged to.
     * The caller then issues a new token in the same family ({@link #issueInFamily}).
     */
    @Transactional(noRollbackFor = AuthenticationFailedException.class)
    public RefreshToken consume(String rawToken) {
        Instant now = clock.instant();
        RefreshToken token = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> invalid());
        if (token.isRevoked()) {
            int revoked = repository.revokeFamily(token.getFamilyId(), now);
            log.warn("Refresh token reuse detected for user '{}' (family {}): revoked {} active token(s)",
                    token.getUsername(), token.getFamilyId(), revoked);
            throw invalid();
        }
        if (token.isExpired(now)) {
            throw invalid();
        }
        token.revoke(now);
        return token;
    }

    @Transactional
    public Issued issueInFamily(RefreshToken previous) {
        return issue(previous.getFamilyId(), previous.getUserId(), previous.getUsername());
    }

    /** Logout: revoke the token and its whole family. Unknown tokens are ignored (idempotent). */
    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hash(rawToken))
                .ifPresent(t -> repository.revokeFamily(t.getFamilyId(), clock.instant()));
    }

    private Issued issue(String familyId, long userId, String username) {
        byte[] bytes = new byte[32]; // 256 random bits
        RANDOM.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant now = clock.instant();
        RefreshToken stored = repository.save(new RefreshToken(hash(raw), familyId, userId, username,
                now, now.plus(props.refreshTokenTtl())));
        return new Issued(raw, stored);
    }

    static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static AuthenticationFailedException invalid() {
        return new AuthenticationFailedException("INVALID_REFRESH_TOKEN",
                "The refresh token is invalid, expired or already used. Please log in again.");
    }
}
