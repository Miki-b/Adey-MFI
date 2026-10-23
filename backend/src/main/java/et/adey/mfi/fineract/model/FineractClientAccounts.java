package et.adey.mfi.fineract.model;

import java.util.List;

/** GET /clients/{id}/accounts. "loanAccounts" is missing entirely when the client has no loans. */
public record FineractClientAccounts(List<LoanAccount> loanAccounts) {

    public record LoanAccount(Long id, String accountNo, String productName) {
    }

    public List<LoanAccount> loanAccountsOrEmpty() {
        return loanAccounts == null ? List.of() : loanAccounts;
    }
}
