package et.adey.mfi.fineract.model;

import java.util.List;

/** One item of GET /offices. Dates are [yyyy, m, d] arrays. */
public record FineractOffice(Long id, String name, Long parentId, List<Integer> openingDate) {
}
