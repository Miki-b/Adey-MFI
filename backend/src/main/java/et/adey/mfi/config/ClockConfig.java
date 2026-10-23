package et.adey.mfi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/**
 * One injectable clock for "now" (token issue/expiry times, audit timestamps), instead of calling
 * Instant.now() everywhere. Tests replace it with a fixed clock to create e.g. expired tokens.
 */
@Configuration
// Also turns on @Scheduled jobs (idempotency key clean-up, login limiter clean-up).
@EnableScheduling
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
