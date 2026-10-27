package et.adey.mfi.loan;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Result of a schedule preview. installmentAmount is the regular installment;
 * lastInstallmentAmount can differ by a few cents because of rounding.
 */
public record SchedulePreviewResponse(
        @Nullable String currency,
        @Nullable BigDecimal principal,
        int numberOfRepayments,
        @Nullable LocalDate expectedDisbursementDate,
        @Nullable BigDecimal installmentAmount,
        @Nullable BigDecimal lastInstallmentAmount,
        BigDecimal totalInterest,
        BigDecimal totalRepayable,
        List<SchedulePeriod> periods) {
}
