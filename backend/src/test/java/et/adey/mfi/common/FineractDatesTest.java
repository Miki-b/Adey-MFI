package et.adey.mfi.common;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FineractDatesTest {

    @Test
    void readsFineractDateArray() {
        assertThat(FineractDates.fromArray(List.of(2026, 10, 3))).isEqualTo(LocalDate.of(2026, 10, 3));
    }

    @Test
    void missingDateIsNull() {
        assertThat(FineractDates.fromArray(null)).isNull();
        assertThat(FineractDates.fromArray(List.of())).isNull();
    }

    @Test
    void rejectsMalformedArray() {
        assertThatThrownBy(() -> FineractDates.fromArray(List.of(2026, 10)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void formatsForFineractRequests() {
        // Two-digit day, full English month name - matches dateFormat "dd MMMM yyyy", locale "en".
        assertThat(FineractDates.format(LocalDate.of(2026, 10, 3))).isEqualTo("03 October 2026");
        assertThat(FineractDates.format(LocalDate.of(2027, 2, 28))).isEqualTo("28 February 2027");
        assertThat(FineractDates.format(null)).isNull();
    }

    @Test
    void formatAndReadRoundTrip() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        assertThat(FineractDates.format(date)).isEqualTo("31 December 2026");
        assertThat(FineractDates.fromArray(List.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth())))
                .isEqualTo(date);
    }
}
