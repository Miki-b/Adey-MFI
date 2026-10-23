package et.adey.mfi.client;

import et.adey.mfi.fineract.FineractClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Translates Gender.MALE/FEMALE to Fineract's code-value ids (looked up by name under the code
 * "Gender"). The ids never change once created, so we look them up once and keep them.
 * The demo seed creates the "Male" / "Female" values if they are missing.
 */
@Component
public class GenderCodeValues {

    private final FineractClient fineract;
    private final AtomicReference<Map<Gender, Long>> ids = new AtomicReference<>();

    public GenderCodeValues(FineractClient fineract) {
        this.fineract = fineract;
    }

    public long idOf(Gender gender) {
        Map<Gender, Long> map = ids.get();
        if (map == null || !map.containsKey(gender)) {
            map = load();
            ids.set(map);
        }
        Long id = map.get(gender);
        if (id == null) {
            throw new IllegalStateException("Fineract has no Gender code value '" + gender.fineractName()
                    + "'. Run the seed profile to create it.");
        }
        return id;
    }

    private Map<Gender, Long> load() {
        Map<Gender, Long> map = new EnumMap<>(Gender.class);
        Long codeId = null;
        for (JsonNode code : fineract.getJson("/codes")) {
            if ("Gender".equals(code.path("name").asString())) {
                codeId = code.path("id").asLong();
            }
        }
        if (codeId == null) {
            return map;
        }
        for (JsonNode value : fineract.getJson("/codes/{id}/codevalues", codeId)) {
            for (Gender g : Gender.values()) {
                if (g.fineractName().equalsIgnoreCase(value.path("name").asString())) {
                    map.put(g, value.path("id").asLong());
                }
            }
        }
        return map;
    }
}
