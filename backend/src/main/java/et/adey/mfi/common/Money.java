package et.adey.mfi.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ETB amounts always have 2 decimals. Fineract sends 1833.6, we return 1833.60,
 * so every amount in our API has the same shape.
 * BigDecimal (not double) because 0.1 + 0.2 must equal exactly 0.3 when counting money.
 */
public final class Money {

    public static final int SCALE = 2;

    private Money() {
    }

    /** Null stays null; otherwise set to 2 decimals. */
    public static BigDecimal of(BigDecimal amount) {
        return amount == null ? null : amount.setScale(SCALE, RoundingMode.HALF_EVEN);
    }

    /** Like {@link #of} but treats a missing amount as 0.00 (e.g. outstanding on a fresh loan). */
    public static BigDecimal orZero(BigDecimal amount) {
        return of(amount == null ? BigDecimal.ZERO : amount);
    }
}
