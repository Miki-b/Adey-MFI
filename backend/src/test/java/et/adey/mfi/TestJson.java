package et.adey.mfi;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Jackson 3 mapper for tests, lenient like Spring Boot's (ignores Fineract's extra fields). */
public final class TestJson {

    private TestJson() {
    }

    public static ObjectMapper mapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}
