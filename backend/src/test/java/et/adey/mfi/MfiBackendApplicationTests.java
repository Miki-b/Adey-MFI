package et.adey.mfi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Starts the whole Spring context (no HTTP calls) to catch wiring/config mistakes. */
@SpringBootTest
@ActiveProfiles("test")
class MfiBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
