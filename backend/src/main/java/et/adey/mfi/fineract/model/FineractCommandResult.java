package et.adey.mfi.fineract.model;

import java.util.Map;

/**
 * What Fineract returns for every write (submit, approve, disburse, repayment, ...):
 * the ids involved plus a "changes" map. resourceId is the new/changed thing:
 * the loan id for POST /loans, the transaction id for a repayment.
 */
public record FineractCommandResult(
        Long officeId,
        Long clientId,
        Long loanId,
        Long resourceId,
        Map<String, Object> changes) {
}
