package et.adey.mfi.loan;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One installment row. total = principal + interest (+ fees/penalties, none on SBL). */
public record SchedulePeriod(
        int number,
        LocalDate dueDate,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal total,
        BigDecimal balanceAfter) {
}
