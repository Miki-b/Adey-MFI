package et.adey.mfi.product;

import et.adey.mfi.common.Money;
import et.adey.mfi.common.Rates;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractCodeValue;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanProductService {

    private final FineractClient fineract;

    public LoanProductService(FineractClient fineract) {
        this.fineract = fineract;
    }

    public List<LoanProductResponse> listProducts() {
        return fineract.getLoanProducts().stream().map(LoanProductService::toResponse).toList();
    }

    static LoanProductResponse toResponse(FineractLoanProduct p) {
        return new LoanProductResponse(
                p.id(),
                p.name(),
                p.shortName(),
                p.currency() == null ? null : p.currency().code(),
                Money.of(p.minPrincipal()),
                Money.of(p.principal()),
                Money.of(p.maxPrincipal()),
                p.minNumberOfRepayments(),
                p.numberOfRepayments(),
                p.maxNumberOfRepayments(),
                p.repaymentEvery(),
                RepaymentFrequency.fromFineractId(idOf(p.repaymentFrequencyType())),
                Rates.percent(p.annualInterestRate()),
                InterestMethod.fromFineractId(idOf(p.interestType())),
                Amortization.fromFineractId(idOf(p.amortizationType())));
    }

    private static Integer idOf(FineractCodeValue codeValue) {
        return codeValue == null ? null : codeValue.id();
    }
}
