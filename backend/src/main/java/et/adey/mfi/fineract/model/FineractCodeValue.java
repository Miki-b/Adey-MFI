package et.adey.mfi.fineract.model;

/**
 * Fineract's generic "enum option": {"id": 2, "code": "...months", "value": "Months"}.
 * Code values (e.g. gender) use a different shape, {"id": 24, "name": "Female"}, hence "name".
 */
public record FineractCodeValue(Integer id, String code, String value, String name) {

    /** The human label, whichever of the two shapes Fineract used. */
    public String label() {
        return value != null ? value : name;
    }
}
