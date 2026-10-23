package et.adey.mfi.product;

/** Fineract period frequency codes: 0 days, 1 weeks, 2 months, 3 years. */
public enum RepaymentFrequency {
    DAYS, WEEKS, MONTHS, YEARS, UNKNOWN;

    public static RepaymentFrequency fromFineractId(Integer id) {
        if (id == null) return UNKNOWN;
        return switch (id) {
            case 0 -> DAYS;
            case 1 -> WEEKS;
            case 2 -> MONTHS;
            case 3 -> YEARS;
            default -> UNKNOWN;
        };
    }
}
