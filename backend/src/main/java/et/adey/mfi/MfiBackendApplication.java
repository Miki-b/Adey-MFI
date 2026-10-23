package et.adey.mfi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
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
        description = "Loan-officer API in front of Apache Fineract. Amounts are ETB with 2 decimals."))
public class MfiBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MfiBackendApplication.class, args);
    }
}
