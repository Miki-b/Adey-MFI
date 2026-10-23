package et.adey.mfi.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Typed view of the "fineract.*" settings in application.yml.
 * Spring binds the YAML keys (kebab-case) to these record fields at startup;
 * @Validated makes the app refuse to start if a required value is blank.
 */
@Validated
@ConfigurationProperties(prefix = "fineract")
public record FineractProperties(
        @NotBlank String baseUrl,
        @NotBlank String tenant,
        @NotBlank String username,
        @NotBlank String password,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("30s") Duration readTimeout,
        /* Skip TLS certificate checks. Local profile only, never production. */
        @DefaultValue("false") boolean trustSelfSignedCertificate) {

    /** Keep the password out of logs if this object is ever printed. */
    @Override
    public String toString() {
        return "FineractProperties[baseUrl=%s, tenant=%s, username=%s, password=***, trustSelfSignedCertificate=%s]"
                .formatted(baseUrl, tenant, username, trustSelfSignedCertificate);
    }
}
