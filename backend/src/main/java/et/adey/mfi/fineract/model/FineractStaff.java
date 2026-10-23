package et.adey.mfi.fineract.model;

/** GET /staff/{id}: used to check a manager assigns a new client to a loan officer of their office. */
public record FineractStaff(Long id, String displayName, Long officeId, Boolean isLoanOfficer, Boolean isActive) {
}
