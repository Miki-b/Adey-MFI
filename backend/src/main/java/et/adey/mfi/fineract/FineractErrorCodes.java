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

    /** Our stable code for "a client with this mobile number / national ID already exists". */
    public static final String DUPLICATE_CLIENT = "DUPLICATE_CLIENT";

    /** Fineract codes for unique-field clashes (it answers 403 with these; nothing is created). */
    private static final Map<String, String> DUPLICATE_MESSAGES = Map.of(
            "error.msg.client.duplicate.mobileNo", "A client with this mobile number already exists.",
            "error.msg.client.duplicate.externalId", "A client with this national ID (externalId) already exists.");

    /** Our message if Fineract refused because a unique client field is already taken. */
    public static Optional<String> duplicateMessage(FineractError error) {
        return firstKnown(error, DUPLICATE_MESSAGES);
    }

    /** Our message if any of Fineract's error codes is a known wrong-state code. */
    public static Optional<String> wrongStateMessage(FineractError error) {
        return firstKnown(error, WRONG_STATE_MESSAGES);
    }

    /** Looks at the top-level code first, then each field error's code. */
    private static Optional<String> firstKnown(FineractError error, Map<String, String> messages) {
        if (error.userMessageGlobalisationCode() != null
                && messages.containsKey(error.userMessageGlobalisationCode())) {
            return Optional.of(messages.get(error.userMessageGlobalisationCode()));
        }
        return error.errorsOrEmpty().stream()
                .map(FineractError.FieldError::userMessageGlobalisationCode)
                // Map.of() throws on containsKey(null), and Fineract sometimes sends no code.
                .filter(Objects::nonNull)
                .filter(messages::containsKey)
                .map(messages::get)
                .findFirst();
    }
}
