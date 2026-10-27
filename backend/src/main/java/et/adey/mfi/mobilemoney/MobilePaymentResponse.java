package et.adey.mfi.mobilemoney;

import org.jspecify.annotations.Nullable;

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
        /* "simulator" or "chapa", and TELEBIRR or MPESA. */
        String provider,
        @Nullable PaymentMethod method,
        PaymentStatus status,
        /* true once nothing more will happen on its own (POSTED, DECLINED, CHARGE_FAILED, MISMATCH, NEEDS_ATTENTION). */
        boolean finalState,
        @Nullable String providerChargeId,
        @Nullable String providerTransactionId,
        @Nullable BigDecimal providerAmount,
        @Nullable Instant paidAt,
        @Nullable Long fineractTransactionId,
        int postingAttempts,
        @Nullable Instant nextAttemptAt,
        @Nullable String lastError,
        String requestedBy,
        Instant createdAt,
        Instant updatedAt,
        List<Transition> history) {

    public record Transition(@Nullable PaymentStatus from, PaymentStatus to, Instant at, @Nullable String note) {
    }

    static MobilePaymentResponse of(PaymentIntent i) {
        return new MobilePaymentResponse(i.getId(), i.getReference(), i.getLoanId(), i.getClientId(), i.getAmount(),
                i.getCurrency(), i.getPhone(), i.getProvider(), i.getMethod(), i.getStatus(), !i.getStatus().isOpen(), i.getProviderChargeId(),
                i.getProviderTransactionId(), i.getProviderAmount(), i.getPaidAt(), i.getFineractTransactionId(),
                i.getPostingAttempts(), i.getNextAttemptAt(), i.getLastError(), i.getRequestedBy(),
                i.getCreatedAt(), i.getUpdatedAt(),
                i.getTransitions().stream()
                        .map(t -> new Transition(t.getFromStatus(), t.getToStatus(), t.getOccurredAt(), t.getNote()))
                        .toList());
    }
}
