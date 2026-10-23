package et.adey.mfi.product;

/** How the loan is paid down. Fineract amortizationType: 1 = equal installments, 0 = equal principal. */
public enum Amortization {
    EQUAL_INSTALLMENTS, EQUAL_PRINCIPAL, UNKNOWN;

    public static Amortization fromFineractId(Integer id) {
        if (id == null) return UNKNOWN;
        return switch (id) {
            case 1 -> EQUAL_INSTALLMENTS;
            case 0 -> EQUAL_PRINCIPAL;
            default -> UNKNOWN;
        };
    }
}
