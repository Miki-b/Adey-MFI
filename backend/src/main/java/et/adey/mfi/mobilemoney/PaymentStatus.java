package et.adey.mfi.mobilemoney;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Life of a mobile money payment intent: a small STATE MACHINE. Only the moves listed here are
 * allowed; anything else is a bug and is refused (IllegalStateException).
 *
 *   PENDING ──provider declined──▶ DECLINED
 *      │  ──charge request failed─▶ CHARGE_FAILED
 *      │  ──paid, wrong amount────▶ MISMATCH          (held, never posted automatically)
 *      ▼ paid, webhook verified (or found by reconciliation)
 *   RECEIVED ──posted to Fineract──────────────────▶ POSTED
 *      │  ──Fineract unavailable──▶ RETRYING ──▶ POSTED
 *      │                                  └──max attempts──▶ NEEDS_ATTENTION
 *      └──Fineract refused (4xx)─────────────────────────▶ NEEDS_ATTENTION
 *
 * Provider-side words: PENDING -> SUCCEEDED / DECLINED. "SUCCEEDED + verified" is our RECEIVED.
 */
public enum PaymentStatus {
    PENDING,
    DECLINED,
    CHARGE_FAILED,
    MISMATCH,
    RECEIVED,
    RETRYING,
    POSTED,
    NEEDS_ATTENTION;

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED = Map.of(
            PENDING, EnumSet.of(RECEIVED, DECLINED, CHARGE_FAILED, MISMATCH),
            RECEIVED, EnumSet.of(POSTED, RETRYING, NEEDS_ATTENTION),
            RETRYING, EnumSet.of(RETRYING, POSTED, NEEDS_ATTENTION),
            DECLINED, EnumSet.noneOf(PaymentStatus.class),
            CHARGE_FAILED, EnumSet.noneOf(PaymentStatus.class),
            MISMATCH, EnumSet.noneOf(PaymentStatus.class),
            POSTED, EnumSet.noneOf(PaymentStatus.class),
            NEEDS_ATTENTION, EnumSet.noneOf(PaymentStatus.class));

    public boolean canMoveTo(PaymentStatus next) {
        return ALLOWED.get(this).contains(next);
    }

    /** Waiting for the posting worker. */
    public boolean isAwaitingPosting() {
        return this == RECEIVED || this == RETRYING;
    }

    /** Money may still arrive or be posted: counts against the loan's outstanding balance. */
    public boolean isOpen() {
        return this == PENDING || this == RECEIVED || this == RETRYING;
    }
}
