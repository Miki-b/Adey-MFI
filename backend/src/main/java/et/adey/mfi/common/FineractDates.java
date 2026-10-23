package et.adey.mfi.common;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Fineract's two date formats in one place.
 * <ul>
 *   <li>Responses carry dates as JSON arrays: {@code [2026, 10, 3]}.</li>
 *   <li>Requests send dates as text plus format fields:
 *       {@code "03 October 2026", "dateFormat": "dd MMMM yyyy", "locale": "en"}.</li>
 * </ul>
 */
public final class FineractDates {

    /** Value for the "dateFormat" field in Fineract requests. */
    public static final String DATE_FORMAT = "dd MMMM yyyy";
    /** Value for the "locale" field in Fineract requests (month names in English). */
    public static final String LOCALE = "en";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern(DATE_FORMAT, Locale.forLanguageTag(LOCALE));

    /** Same time zone as the Fineract tenant (FINERACT_DEFAULT_TENANTDB_TIMEZONE in infra/). */
    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Africa/Addis_Ababa");

    private FineractDates() {
    }

    /** Today's date in Addis Ababa, regardless of the server's own time zone. */
    public static LocalDate today() {
        return LocalDate.now(BUSINESS_ZONE);
    }

    /** {@code [2026, 10, 3]} → 2026-10-03. Null or empty → null (Fineract omits unset dates). */
    public static LocalDate fromArray(List<Integer> parts) {
        if (parts == null || parts.isEmpty()) {
            return null;
        }
        if (parts.size() != 3) {
            throw new IllegalArgumentException("Expected [year, month, day] but got " + parts);
        }
        return LocalDate.of(parts.get(0), parts.get(1), parts.get(2));
    }

    /** 2026-10-03 → "03 October 2026". */
    public static String format(LocalDate date) {
        return date == null ? null : FORMATTER.format(date);
    }
}
