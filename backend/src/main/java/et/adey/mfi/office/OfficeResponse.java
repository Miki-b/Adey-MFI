package et.adey.mfi.office;

import java.time.LocalDate;

/** An office/branch as our API returns it. parentId is null for the Head Office. */
public record OfficeResponse(Long id, String name, Long parentId, LocalDate openingDate) {
}
