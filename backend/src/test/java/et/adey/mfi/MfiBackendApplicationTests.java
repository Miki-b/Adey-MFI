package et.adey.mfi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/** Starts the whole Spring context (no HTTP calls) to catch wiring/config mistakes. */
@SpringBootTest
class MfiBackendApplicationTests {

    @Test
    void contextLoads() {
    }
}
