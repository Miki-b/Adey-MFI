package et.adey.mfi.mobilemoney;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** A mobile money payment intent as the app sees it: poll GET /api/mobile-payments/{id} until it is final. */
public record MobilePaymentResponse(
        Long id,
        String reference,
        Long loanId,
        Long clientId,
        BigDecimal amount,
        String currency,
        String phone,
        PaymentStatus status,
        /* true once nothing more will happen on its own (POSTED, DECLINED, CHARGE_FAILED, MISMATCH, NEEDS_ATTENTION). */
        boolean finalState,
        String providerChargeId,
        String providerTransactionId,
        BigDecimal providerAmount,
        Instant paidAt,
        Long fineractTransactionId,
        int postingAttempts,
        Instant nextAttemptAt,
        String lastError,
        String requestedBy,
        Instant createdAt,
        Instant updatedAt,
        List<Transition> history) {

    public record Transition(PaymentStatus from, PaymentStatus to, Instant at, String note) {
    }

    static MobilePaymentResponse of(PaymentIntent i) {
        return new MobilePaymentResponse(i.getId(), i.getReference(), i.getLoanId(), i.getClientId(), i.getAmount(),
                i.getCurrency(), i.getPhone(), i.getStatus(), !i.getStatus().isOpen(), i.getProviderChargeId(),
                i.getProviderTransactionId(), i.getProviderAmount(), i.getPaidAt(), i.getFineractTransactionId(),
                i.getPostingAttempts(), i.getNextAttemptAt(), i.getLastError(), i.getRequestedBy(),
                i.getCreatedAt(), i.getUpdatedAt(),
                i.getTransitions().stream()
                        .map(t -> new Transition(t.getFromStatus(), t.getToStatus(), t.getOccurredAt(), t.getNote()))
                        .toList());
    }
}
