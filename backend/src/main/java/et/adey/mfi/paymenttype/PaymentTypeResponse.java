package et.adey.mfi.paymenttype;

import org.jspecify.annotations.Nullable;

/** How money changed hands (cash, bank transfer, ...). Used by disbursements and repayments. */
public record PaymentTypeResponse(Long id, String name, @Nullable String description, boolean cash) {
}
