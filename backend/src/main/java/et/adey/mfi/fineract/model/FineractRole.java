package et.adey.mfi.fineract.model;

/** A Fineract role as listed on a user, e.g. {"id": 3, "name": "Loan Officer", "disabled": false}. */
public record FineractRole(Long id, String name, Boolean disabled) {
}
