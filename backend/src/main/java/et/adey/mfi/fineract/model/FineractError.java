package et.adey.mfi.fineract.model;

import java.util.List;

/**
 * Fineract's error body, e.g.
 * {"defaultUserMessage": "Validation errors exist.", "errors": [{"parameterName": "principal", ...}]}
 */
public record FineractError(
        String developerMessage,
        String defaultUserMessage,
        String userMessageGlobalisationCode,
        List<FieldError> errors) {

    public record FieldError(
            String parameterName,
            String defaultUserMessage,
            String developerMessage,
            String userMessageGlobalisationCode) {
    }

    public List<FieldError> errorsOrEmpty() {
        return errors == null ? List.of() : errors;
    }
}
