package et.adey.mfi.loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Result of a schedule preview. installmentAmount is the regular installment;
 * lastInstallmentAmount can differ by a few cents because of rounding.
 */
public record SchedulePreviewResponse(
        String currency,
        BigDecimal principal,
        int numberOfRepayments,
        LocalDate expectedDisbursementDate,
        BigDecimal installmentAmount,
        BigDecimal lastInstallmentAmount,
        BigDecimal totalInterest,
        BigDecimal totalRepayable,
        List<SchedulePeriod> periods) {
}
