package et.adey.mfi.mobilemoney.chapa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static et.adey.mfi.mobilemoney.MobileMoneyProvider.VerifiedStatus;
import static org.assertj.core.api.Assertions.assertThat;

class ChapaStatusTest {

    @ParameterizedTest
    @CsvSource({
            "success, SUCCEEDED",
            "SUCCESS, SUCCEEDED",
            "' success ', SUCCEEDED",
            "failed/cancelled, DECLINED",
            "failed, DECLINED",
            "cancelled, DECLINED",
            "pending, PENDING",
            "Pending, PENDING",
            // Never seen, or money going back: a person must look, it is never treated as paid.
            "refunded, UNKNOWN",
            "reversed, UNKNOWN",
            "successful, UNKNOWN",
            "'', UNKNOWN"
    })
    void mapsChapaWordsToOurs(String chapa, VerifiedStatus ours) {
        assertThat(ChapaStatus.fromVerify(chapa)).isEqualTo(ours);
    }

    @Test
    void missingStatusIsUnknown() {
        assertThat(ChapaStatus.fromVerify(null)).isEqualTo(VerifiedStatus.UNKNOWN);
    }
}
