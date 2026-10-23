package et.adey.mfi.paymenttype;

import et.adey.mfi.common.BadRequestException;
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

    /**
     * Checks a paymentTypeId from a disbursement or repayment: it must be one of the types
     * listPaymentTypes() offers (e.g. Cash, Money Transfer). null = not given, which is allowed.
     */
    public void requireSelectable(Long paymentTypeId) {
        if (paymentTypeId == null) {
            return;
        }
        boolean ok = listPaymentTypes().stream().anyMatch(p -> p.id().equals(paymentTypeId));
        if (!ok) {
            throw new BadRequestException("paymentTypeId",
                    "Payment type %d is not available. Use one from GET /api/payment-types.".formatted(paymentTypeId));
        }
    }
}
