package et.adey.mfi.common;

import java.math.BigDecimal;

/**
 * Interest rates (percent). Fineract stores them with 6 decimals (18.000000);
 * we return at least 2 (18.00) but keep real precision (12.125 stays 12.125).
 */
public final class Rates {

    private Rates() {
    }

    public static BigDecimal percent(BigDecimal rate) {
        if (rate == null) {
            return null;
        }
        BigDecimal stripped = rate.stripTrailingZeros();
        return rate.setScale(Math.max(2, stripped.scale()));
    }
}
