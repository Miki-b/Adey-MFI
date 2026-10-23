package et.adey.mfi.paymenttype;

/** How money changed hands (cash, bank transfer, ...). Used by disbursements and repayments. */
public record PaymentTypeResponse(Long id, String name, String description, boolean cash) {
}
