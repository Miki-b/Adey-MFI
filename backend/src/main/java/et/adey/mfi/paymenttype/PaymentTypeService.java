package et.adey.mfi.paymenttype;

import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractPaymentType;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class PaymentTypeService {

    private final FineractClient fineract;

    public PaymentTypeService(FineractClient fineract) {
        this.fineract = fineract;
    }

    /**
     * Only types an officer may pick. Fineract's system-defined ones (repayment adjustment
     * chargeback/refund) are internal bookkeeping, so we hide them.
     */
    public List<PaymentTypeResponse> listPaymentTypes() {
        return fineract.getPaymentTypes().stream()
                .filter(p -> !Boolean.TRUE.equals(p.isSystemDefined()))
                .sorted(Comparator.comparing((FineractPaymentType p) -> p.position() == null ? Integer.MAX_VALUE : p.position())
                        .thenComparing(FineractPaymentType::name))
                .map(p -> new PaymentTypeResponse(p.id(), p.name(), p.description(),
                        Boolean.TRUE.equals(p.isCashPayment())))
                .toList();
    }
}
