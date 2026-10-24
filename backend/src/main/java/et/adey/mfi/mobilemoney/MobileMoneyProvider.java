package et.adey.mfi.mobilemoney;

import org.springframework.http.HttpHeaders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ADAPTER PATTERN: the "port" our code talks to, whatever the mobile money provider is.
 *
 * MobilePaymentService, WebhookController, PaymentVerifier and reconciliation only know this
 * interface and its small records (in our own words: SUCCEEDED, DECLINED, ...). Each provider gets
 * one ADAPTER class that translates between this interface and the provider's API: its URLs, field
 * names, status words, signature scheme. Adding a provider = writing one new adapter; nothing else
 * changes. Exactly one adapter is active, chosen with app.mobile-money.provider:
 *
 *   simulator  SimulatorProvider (our TelePay simulator, local profile)
 *   chapa      ChapaProvider     (Chapa test mode: telebirr and M-Pesa)
 */
public interface MobileMoneyProvider {

    /** Stored on every payment intent ("simulator", "chapa"). */
    String name();

    /**
     * Ask the provider to charge the client's wallet (a USSD push: the client confirms on the phone).
     * Throws ProviderUnavailableException if the provider can't be reached or answers 5xx.
     */
    ChargeResult createCharge(ChargeRequest request);

    /** Ask the provider, over our authenticated API connection, what happened to one of our payments. */
    Verification verifyCharge(String reference);

    /** Check that a webhook really comes from the provider, and say what it tells us. */
    WebhookCheck parseAndVerifyWebhook(HttpHeaders headers, String rawBody, Instant now);

    /**
     * The provider's list of successful payments on a day, if it has one. Empty = no such endpoint:
     * reconciliation then asks verifyCharge about each of our own intents (see SettlementSource).
     */
    Optional<List<Settlement>> listSettlements(LocalDate date);

    /**
     * true = webhooks are only a hint and nothing is trusted until verifyCharge confirms it;
     * PENDING payments are also polled with verifyCharge (PaymentVerifier), so they complete even
     * if no webhook ever arrives.
     */
    boolean needsVerification();

    // ---- Records in our own words -----------------------------------------------------------

    /** reference = our unique id for this attempt (Chapa calls it tx_ref). */
    record ChargeRequest(String reference, String phone, BigDecimal amount, String currency, PaymentMethod method) {
    }

    /**
     * accepted=false: the provider answered but did not start the charge (e.g. invalid number);
     * message says why. providerChargeId is the provider's id for the charge, when it gave one.
     */
    record ChargeResult(boolean accepted, String providerChargeId, String message) {

        public static ChargeResult accepted(String providerChargeId) {
            return new ChargeResult(true, providerChargeId, null);
        }

        public static ChargeResult notAccepted(String providerChargeId, String message) {
            return new ChargeResult(false, providerChargeId, message);
        }
    }

    enum VerifiedStatus {
        /** Money taken from the wallet. */
        SUCCEEDED,
        /** Declined, failed or cancelled: no money moved. */
        DECLINED,
        /** Still waiting for the client. */
        PENDING,
        /** The provider has no payment with this reference. */
        NOT_FOUND,
        /** A status we don't know (refunded, reversed, something new): a person must look. */
        UNKNOWN
    }

    /**
     * What the provider says about one payment. providerReference = the provider's own id
     * (our receipt number in Fineract). rawStatus = the provider's word, for notes and audit.
     * summary = a short masked JSON of the answer, stored with the inbox row.
     */
    record Verification(VerifiedStatus status, String rawStatus, String reference, String providerReference,
                        BigDecimal amount, String currency, Instant paidAt, String summary) {
    }

    /** A successful payment in the provider's books. */
    record Settlement(String chargeId, String reference, String providerTransactionId, BigDecimal amount,
                      String currency, Instant settledAt) {
    }

    /** Result of checking a webhook. */
    sealed interface WebhookCheck {

        /** Not from the provider (or unreadable): answer httpStatus with this error code. */
        record Rejected(int httpStatus, String code, String message, String auditReason) implements WebhookCheck {
        }

        /** Authentic AND trustworthy on its own (signed + fresh timestamp + unique event id). */
        record Trusted(ProviderPaymentEvent event) implements WebhookCheck {
        }

        /** Authentic, but only a hint: confirm with verifyCharge before acting on it. */
        record Hint(String reference, String event) implements WebhookCheck {
        }
    }
}
