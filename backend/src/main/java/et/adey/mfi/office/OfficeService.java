package et.adey.mfi.office;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractOffice;
import org.springframework.stereotype.Service;

import java.util.List;

/** Business layer for offices: fetch from Fineract and map to our DTO. */
@Service
public class OfficeService {

    private final FineractClient fineract;

    public OfficeService(FineractClient fineract) {
        this.fineract = fineract;
    }

    public List<OfficeResponse> listOffices() {
        return fineract.getOffices().stream().map(OfficeService::toResponse).toList();
    }

    static OfficeResponse toResponse(FineractOffice o) {
        return new OfficeResponse(o.id(), o.name(), o.parentId(), FineractDates.fromArray(o.openingDate()));
    }
}
