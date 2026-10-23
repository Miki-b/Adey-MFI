package et.adey.mfi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * One injectable clock for "now" (token issue/expiry times, audit timestamps), instead of calling
 * Instant.now() everywhere. Tests replace it with a fixed clock to create e.g. expired tokens.
 */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
