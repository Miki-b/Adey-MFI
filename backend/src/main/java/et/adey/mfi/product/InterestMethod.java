package et.adey.mfi.product;

/** How interest is charged. Fineract interestType: 0 = declining balance, 1 = flat. */
public enum InterestMethod {
    DECLINING_BALANCE, FLAT, UNKNOWN;

    public static InterestMethod fromFineractId(Integer id) {
        if (id == null) return UNKNOWN;
        return switch (id) {
            case 0 -> DECLINING_BALANCE;
            case 1 -> FLAT;
            default -> UNKNOWN;
        };
    }
}
