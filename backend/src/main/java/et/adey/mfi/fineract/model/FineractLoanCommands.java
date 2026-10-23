package et.adey.mfi.fineract.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Request bodies for loan state changes. Each is sent with dateFormat + locale because it
 * contains a date. NON_NULL leaves optional fields out instead of sending null.
 */
public final class FineractLoanCommands {

    private FineractLoanCommands() {
    }

    /** POST /loans/{id}?command=approve. approvedLoanAmount null = approve the requested principal. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Approve(String approvedOnDate, BigDecimal approvedLoanAmount, String note,
                          String dateFormat, String locale) {
    }

    /** POST /loans/{id}?command=undoApproval: back to "Submitted and pending approval". */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UndoApproval(String note) {
    }

    /** POST /loans/{id}?command=reject. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Reject(String rejectedOnDate, String note, String dateFormat, String locale) {
    }

    /** POST /loans/{id}?command=disburse. transactionAmount null = disburse the approved amount. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Disburse(String actualDisbursementDate, BigDecimal transactionAmount, Long paymentTypeId,
                           String note, String dateFormat, String locale) {
    }

    /** POST /loans/{id}/transactions?command=repayment. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Repayment(String transactionDate, BigDecimal transactionAmount, Long paymentTypeId,
                            String note, String dateFormat, String locale) {
    }
}
