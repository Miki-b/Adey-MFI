package et.adey.mfi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point. @SpringBootApplication turns on auto-configuration and component scanning
 * of this package and everything below it. @ConfigurationPropertiesScan picks up our
 * @ConfigurationProperties records (e.g. FineractProperties).
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@OpenAPIDefinition(info = @Info(
        title = "Adey MFI backend",
        version = "v1",
        description = "Loan-officer API in front of Apache Fineract. Amounts are ETB with 2 decimals. "
                + "Log in with POST /api/auth/login, then click Authorize and paste the accessToken."),
        security = @SecurityRequirement(name = "bearerAuth"))
// Tells Swagger UI that calls need "Authorization: Bearer <token>" (adds the Authorize button).
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class MfiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MfiBackendApplication.class, args);
    }
}
