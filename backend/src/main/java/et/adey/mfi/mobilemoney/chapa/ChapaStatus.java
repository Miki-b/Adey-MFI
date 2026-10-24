package et.adey.mfi.mobilemoney.chapa;

import et.adey.mfi.mobilemoney.MobileMoneyProvider.VerifiedStatus;

import java.util.Locale;

/**
 * Chapa's status words -> ours. Deliberately strict: only words we have seen Chapa use are mapped.
 * Anything else (refunded, reversed, a word Chapa adds next year) is UNKNOWN, which sends the
 * payment to NEEDS_ATTENTION. It is never treated as paid.
 *
 *   "success"                                        -> SUCCEEDED
 *   "failed/cancelled" (seen live), "failed", "cancelled" -> DECLINED
 *   "pending"                                        -> PENDING
 */
public final class ChapaStatus {

    private ChapaStatus() {
    }

    public static VerifiedStatus fromVerify(String chapaStatus) {
        if (chapaStatus == null) {
            return VerifiedStatus.UNKNOWN;
        }
        return switch (chapaStatus.trim().toLowerCase(Locale.ROOT)) {
            case "success" -> VerifiedStatus.SUCCEEDED;
            case "failed/cancelled", "failed", "cancelled", "canceled" -> VerifiedStatus.DECLINED;
            case "pending" -> VerifiedStatus.PENDING;
            default -> VerifiedStatus.UNKNOWN;
        };
    }
}
