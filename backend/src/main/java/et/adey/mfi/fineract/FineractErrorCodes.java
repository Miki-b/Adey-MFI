package et.adey.mfi.fineract;

import et.adey.mfi.fineract.model.FineractError;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Fineract error codes (userMessageGlobalisationCode) that mean "this action is not allowed in the
 * loan's current state". Fineract reports these as HTTP 400 validation errors, the same status it
 * uses for a missing field, so we recognise them by code instead of by HTTP status.
 *
 * Each code was observed live against our Fineract (see notes/04-security.md, Step 2).
 * Add new codes here as they come up; unknown codes keep the normal status mapping.
 */
public final class FineractErrorCodes {

    private FineractErrorCodes() {
    }

    /** Our stable code in the ProblemDetail, so the app can react without parsing text. */
    public static final String WRONG_LOAN_STATE = "LOAN_WRONG_STATE";

    /** Fineract code -> our clearer message for a loan officer. */
    private static final Map<String, String> WRONG_STATE_MESSAGES = Map.of(
            "error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state",
            "This loan cannot be disbursed: only an approved loan that has not been disbursed yet can be paid out.",
            "error.msg.loan.approve.account.is.not.submitted.and.pending.state",
            "This loan cannot be approved: only an application that is pending approval can be approved.",
            "error.msg.loan.undo.approval.account.is.not.approved",
            "The approval cannot be undone: the loan is not in the approved state (it may already be disbursed).",
            "error.msg.loan.reject.account.is.not.submitted.pending.approval.state",
            "This loan cannot be rejected: only an application that is pending approval can be rejected.",
            "error.msg.loan.must.be.active.fully.paid.or.overpaid",
            "A repayment can only be recorded on a disbursed (active) loan.");

    /** Our message if any of Fineract's error codes is a known wrong-state code. */
    public static Optional<String> wrongStateMessage(FineractError error) {
        if (error.userMessageGlobalisationCode() != null
                && WRONG_STATE_MESSAGES.containsKey(error.userMessageGlobalisationCode())) {
            return Optional.of(WRONG_STATE_MESSAGES.get(error.userMessageGlobalisationCode()));
        }
        return error.errorsOrEmpty().stream()
                .map(FineractError.FieldError::userMessageGlobalisationCode)
                // Map.of() throws on containsKey(null), and Fineract sometimes sends no code.
                .filter(Objects::nonNull)
                .filter(WRONG_STATE_MESSAGES::containsKey)
                .map(WRONG_STATE_MESSAGES::get)
                .findFirst();
    }
}
