package et.adey.mfi.mobilemoney;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentIntentRepository extends JpaRepository<PaymentIntent, Long> {

    Optional<PaymentIntent> findByReference(String reference);

    Optional<PaymentIntent> findByProviderTransactionId(String providerTransactionId);

    List<PaymentIntent> findByLoanIdOrderByIdDesc(Long loanId);

    List<PaymentIntent> findByLoanIdAndStatusIn(Long loanId, Collection<PaymentStatus> statuses);

    /** What the posting worker should handle now. */
    List<PaymentIntent> findTop20ByStatusInAndNextAttemptAtLessThanEqualOrderByNextAttemptAtAsc(
            Collection<PaymentStatus> statuses, Instant now);

    List<PaymentIntent> findByPaidAtBetween(Instant from, Instant to);
}
