package et.adey.mfi.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Loan products")
@RestController
@RequestMapping("/api/loan-products")
public class LoanProductController {

    private final LoanProductService loanProductService;

    public LoanProductController(LoanProductService loanProductService) {
        this.loanProductService = loanProductService;
    }

    @Operation(summary = "List loan products with their principal / repayment ranges")
    @GetMapping
    public List<LoanProductResponse> list() {
        return loanProductService.listProducts();
    }
}
