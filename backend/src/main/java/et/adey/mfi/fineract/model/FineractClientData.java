package et.adey.mfi.fineract.model;

import java.util.List;

/** One client from GET /clients or GET /clients/{id}. */
public record FineractClientData(
        Long id,
        String accountNo,
        String externalId,
        String displayName,
        String firstname,
        String middlename,
        String lastname,
        String mobileNo,
        List<Integer> dateOfBirth,
        FineractCodeValue gender,
        FineractCodeValue status,
        Boolean active,
        List<Integer> activationDate,
        Long officeId,
        String officeName,
        Long staffId,
        String staffName,
        Timeline timeline) {

    public record Timeline(List<Integer> submittedOnDate) {
    }
}
