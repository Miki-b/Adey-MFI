package et.adey.mfi.mobilemoney;

import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.common.ApiException;
import et.adey.mfi.common.Money;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.loan.LoanGuard;
import et.adey.mfi.loan.LoanStatus;
import et.adey.mfi.security.AccessPolicy;
import et.adey.mfi.security.Officer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.security.SecureRandom;

/**
 * Officer side: request a mobile money repayment, follow it, list a loan's history.
 * Nothing here posts to Fineract: posting only happens in MobileMoneyPostingService, after the
 * provider has confirmed the payment.
 */
@Slf4j
@Service
public class MobilePaymentService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final FineractClient fineract;
    private final LoanGuard guard;
    private final PaymentIntentRepository intents;
    private final TelePayClient telePay;
    private final MobileMoneyProperties props;
    private final AuditService audit;
    private final Clock clock;
    private final AccessPolicy access;

    public MobilePaymentService(FineractClient fineract, LoanGuard guard, PaymentIntentRepository intents,
                                TelePayClient telePay, MobileMoneyProperties props, AuditService audit, Clock clock,
                                AccessPolicy access) {
        this.access = access;
        this.fineract = fineract;
        this.guard = guard;
        this.intents = intents;
        this.telePay = telePay;
        this.props = props;
        this.audit = audit;
        this.clock = clock;
    }

    /**
     * 1. the loan's client must be the officer's (or in the manager's office), the loan ACTIVE;
     * 2. amount <= total outstanding minus mobile payments already on their way (no overpaying);
     * 3. store the intent (PENDING) BEFORE calling the provider, so we never lose track of a charge;
     * 4. ask the provider to charge the phone; the client confirms on the handset.
     */
    public MobilePaymentResponse request(Officer officer, long loanId, MobilePaymentRequest req) {
        return audit.record(officer, AuditAction.MOBILE_PAYMENT_REQUEST, rec -> {
            rec.loanId(loanId).amount(req.amount());
            FineractLoan loan = fineract.getLoanWithSchedule(loanId);
            rec.clientId(loan.clientId());
            FineractClientData client = guard.requireClient(officer, loan.clientId());

            if (LoanStatus.fromFineractId(loan.status() == null ? null : loan.status().id()) != LoanStatus.ACTIVE) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "LOAN_NOT_ACTIVE",
                        "Mobile money repayments are only possible on an active (disbursed) loan.");
            }
            BigDecimal outstanding = Money.orZero(loan.summary() == null ? null : loan.summary().totalOutstanding());
            BigDecimal inFlight = intents.findByLoanIdAndStatusIn(loanId,
                            EnumSet.of(PaymentStatus.PENDING, PaymentStatus.RECEIVED, PaymentStatus.RETRYING))
                    .stream().map(PaymentIntent::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal available = outstanding.subtract(inFlight);
            if (req.amount().compareTo(available) > 0) {
                throw new ApiException(HttpStatus.UNPROCESSABLE_CONTENT, "AMOUNT_EXCEEDS_OUTSTANDING",
                        "At most %s %s can be collected on this loan (outstanding %s, already requested %s)."
                                .formatted(Money.of(available), props.currency(), outstanding, Money.of(inFlight)));
            }

            PaymentIntent intent = intents.save(new PaymentIntent(newReference(), loanId, loan.clientId(),
                    client.officeId(), client.staffId(), officer.username(), req.phone(), Money.of(req.amount()), props.currency(),
                    clock.instant()));
            try {
                TelePayClient.Charge charge = telePay.createCharge(new TelePayClient.ChargeRequest(intent.getReference(),
                        req.phone(), intent.getAmount(), intent.getCurrency(), props.callbackUrl()));
                intent.chargeCreated(charge.chargeId());
                intent = intents.save(intent);
            } catch (ProviderUnavailableException e) {
                intent.recordError(e.getMessage());
                intent.moveTo(PaymentStatus.CHARGE_FAILED, clock.instant(), e.getMessage());
                intents.save(intent);
                throw e;
            }
            log.info("Mobile payment {} ({} ETB) requested on loan {} by {}: charge {}", intent.getReference(),
                    intent.getAmount(), loanId, officer.username(), intent.getProviderChargeId());
            return MobilePaymentResponse.of(intent);
        });
    }

    /**
     * Status polling must work even while Fineract is down (that is when officers worry most), so
     * access is checked against the office/officer stored on the intent, not by asking Fineract.
     */
    public MobilePaymentResponse get(Officer officer, long intentId) {
        PaymentIntent intent = intents.findById(intentId).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "No mobile payment " + intentId + "."));
        requireAccess(officer, intent);
        return MobilePaymentResponse.of(intent);
    }

    public List<MobilePaymentResponse> listForLoan(Officer officer, long loanId) {
        List<PaymentIntent> found = intents.findByLoanIdOrderByIdDesc(loanId);
        if (found.isEmpty()) {
            // Nothing stored to check against: ask Fineract whose loan it is.
            guard.requireClient(officer, fineract.getLoanWithSchedule(loanId).clientId());
            return List.of();
        }
        requireAccess(officer, found.get(0));
        return found.stream().map(MobilePaymentResponse::of).toList();
    }

    private void requireAccess(Officer officer, PaymentIntent intent) {
        if (intent.getStaffId() == null) {
            guard.requireClient(officer, intent.getClientId()); // pre-V4 rows: fall back to Fineract
        } else {
            access.requireAccess(officer, intent.getOfficeId(), intent.getStaffId(), "Mobile payment " + intent.getId());
        }
    }

    /** e.g. "AMP-3F9A0C2B7E41": short, unique, safe to show on a phone screen. */
    private static String newReference() {
        byte[] bytes = new byte[6];
        RANDOM.nextBytes(bytes);
        return "AMP-" + HexFormat.of().withUpperCase().formatHex(bytes);
    }
}
