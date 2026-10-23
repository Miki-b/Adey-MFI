package et.adey.mfi.fineract.model;

import java.util.List;

/** Paged list wrapper used by e.g. GET /clients: {"totalFilteredRecords": 1, "pageItems": [...]}. */
public record FineractPage<T>(long totalFilteredRecords, List<T> pageItems) {

    public List<T> itemsOrEmpty() {
        return pageItems == null ? List.of() : pageItems;
    }
}
