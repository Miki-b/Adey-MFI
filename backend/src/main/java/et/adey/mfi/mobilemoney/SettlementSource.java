package et.adey.mfi.mobilemoney;

import et.adey.mfi.common.FineractDates;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.Settlement;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.Verification;
import et.adey.mfi.mobilemoney.MobileMoneyProvider.VerifiedStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * "What does the provider say was paid on this day?" for reconciliation.
 *
 * The simulator has a settlement listing, so we use it. Chapa has none for direct charges, so we
 * ask its verify API about each of OUR payment intents created that day or the day before
 * (a payment can complete after midnight) and keep the successful ones paid on that day.
 *
 * Trade-off: a listing comes from the provider's books, so it also shows money we have no record of
 * at all (e.g. a charge created outside our system with our account). Verifying our own intents can
 * only find payments we started. That is fine for this app, where every charge starts from an
 * intent, but a real MFI would also compare Chapa's dashboard export or bank settlement statement.
 * It also costs one API call per intent, which is fine for a branch's daily volume.
 */
@Slf4j
@Component
public class SettlementSource {

    private final MobileMoneyProvider provider;
    private final PaymentIntentRepository intents;

    public SettlementSource(MobileMoneyProvider provider, PaymentIntentRepository intents) {
        this.provider = provider;
        this.intents = intents;
    }

    public List<Settlement> forDate(LocalDate date) {
        Optional<List<Settlement>> listed = provider.listSettlements(date);
        if (listed.isPresent()) {
            return listed.get();
        }
        Instant from = date.minusDays(1).atStartOfDay(FineractDates.BUSINESS_ZONE).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(FineractDates.BUSINESS_ZONE).toInstant();
        List<Settlement> paid = new ArrayList<>();
        List<PaymentIntent> candidates = intents.findByProviderAndCreatedAtBetweenOrderByIdAsc(provider.name(), from, to);
        for (PaymentIntent intent : candidates) {
            Verification v = provider.verifyCharge(intent.getReference());
            if (v.status() == VerifiedStatus.SUCCEEDED && v.paidAt() != null
                    && v.paidAt().atZone(FineractDates.BUSINESS_ZONE).toLocalDate().equals(date)) {
                paid.add(new Settlement(v.providerReference(), intent.getReference(), v.providerReference(), v.amount(),
                        v.currency(), v.paidAt()));
            }
        }
        log.info("Settlements for {} from {} verify calls: {} of {} intents paid that day", date, provider.name(),
                paid.size(), candidates.size());
        return paid;
    }
}
