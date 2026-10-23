package et.adey.mfi.fineract.model;

import java.util.List;

/** GET /clients/{id}/accounts. "loanAccounts" is missing entirely when the client has no loans. */
public record FineractClientAccounts(List<LoanAccount> loanAccounts) {

    /** status.id uses the same codes as a loan: 100 pending, 200 approved, 300 active, ... */
    public record LoanAccount(Long id, String accountNo, String productName, FineractCodeValue status) {
    }

    public List<LoanAccount> loanAccountsOrEmpty() {
        return loanAccounts == null ? List.of() : loanAccounts;
    }
}
