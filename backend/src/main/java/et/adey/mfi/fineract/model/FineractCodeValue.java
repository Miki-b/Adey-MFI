package et.adey.mfi.fineract.model;

/** Fineract's generic "enum option": {"id": 2, "code": "...months", "value": "Months"}. */
public record FineractCodeValue(Integer id, String code, String value) {
}
