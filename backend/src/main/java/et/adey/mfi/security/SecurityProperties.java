package et.adey.mfi.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * "app.security.*" settings. Validated at startup: without a JWT secret of 32+ characters the
 * application refuses to start, so a server can never run with an empty or weak signing key.
 */
@Validated
@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        @Valid @NotNull Jwt jwt,
        @DefaultValue("7d") Duration refreshTokenTtl,
        @DefaultValue("false") boolean publicDocs) {

    public record Jwt(
            /* HS256 needs at least 256 bits = 32 bytes of key. */
            @NotBlank @Size(min = 32, message = "must be at least 32 characters (set JWT_SECRET)") String secret,
            @DefaultValue("adey-mfi-backend") String issuer,
            @DefaultValue("15m") Duration accessTokenTtl) {

        @Override
        public String toString() {
            return "Jwt[secret=***, issuer=%s, accessTokenTtl=%s]".formatted(issuer, accessTokenTtl);
        }
    }
}
