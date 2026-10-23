package et.adey.mfi.office;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** HTTP layer: maps URLs to service calls. No business logic here. */
@Tag(name = "Offices")
@RestController
@RequestMapping("/api/offices")
public class OfficeController {

    private final OfficeService officeService;

    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @Operation(summary = "List all offices (branches)")
    @GetMapping
    public List<OfficeResponse> list() {
        return officeService.listOffices();
    }
}
