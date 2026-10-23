package et.adey.mfi.loan;

import et.adey.mfi.common.ConflictException;
import et.adey.mfi.common.FineractDates;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientAccounts.LoanAccount;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanCommands;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import et.adey.mfi.fineract.model.FineractLoanRequest;
import et.adey.mfi.paymenttype.PaymentTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

/**
 * Write side of loans: the lifecycle
 * submit -> approve (or reject / undo approval) -> disburse -> repayments.
 * Fineract enforces the state machine (e.g. you cannot disburse an unapproved loan; it answers
 * 403, which we return as 422). Our own extra rules are checked here before calling Fineract.
 */
@Slf4j
@Service
public class LoanLifecycleService {

    /** Loan states that count as "the client already has a loan in progress". */
    static final Set<LoanStatus> OPEN_STATUSES =
            Set.of(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.APPROVED, LoanStatus.ACTIVE);

    private final FineractClient fineract;
    private final PaymentTypeService paymentTypes;

    public LoanLifecycleService(FineractClient fineract, PaymentTypeService paymentTypes) {
        this.fineract = fineract;
        this.paymentTypes = paymentTypes;
    }

    // ---- Submit --------------------------------------------------------------------------

    public LoanStatusResponse submit(LoanApplicationRequest req) {
        ensureNoOpenLoan(req.clientId());

        FineractLoanProduct product = fineract.getLoanProduct(req.productId());
        FineractLoanRequest request = LoanService.buildLoanRequest(req.clientId(), product, req.principal(),
                req.numberOfRepayments(), req.expectedDisbursementDate(), req.staffId());
        FineractCommandResult result = fineract.submitLoanApplication(request);

        long loanId = result.loanId() != null ? result.loanId() : result.resourceId();
        log.info("Loan {} submitted for client {}", loanId, req.clientId());
        return currentStatus(loanId);
    }

    /**
     * ADEY MFI BUSINESS RULE (ours, not Fineract's): a client may have only one loan in progress.
     * Fineract itself happily accepts several parallel applications for the same client, so this
     * check lives in our service layer. "In progress" = pending approval, approved or active.
     * Note: two simultaneous submissions could both pass this check; acceptable for now
     * (one officer per client), revisit if that changes.
     */
    void ensureNoOpenLoan(long clientId) {
        Optional<LoanAccount> open = fineract.getClientAccounts(clientId).loanAccountsOrEmpty().stream()
                .filter(account -> OPEN_STATUSES.contains(statusOf(account)))
                .findFirst();
        if (open.isPresent()) {
            LoanAccount loan = open.get();
            throw new ConflictException("OPEN_LOAN_EXISTS",
                    "Client %d already has loan %s (%s). A new loan can only be applied for once it is closed, rejected or withdrawn."
                            .formatted(clientId, loan.accountNo(),
                                    loan.status() == null ? "status unknown" : loan.status().value()));
        }
    }

    private static LoanStatus statusOf(LoanAccount account) {
        return LoanStatus.fromFineractId(account.status() == null ? null : account.status().id());
    }

    // ---- Approval ------------------------------------------------------------------------

    public LoanStatusResponse approve(long loanId, ApproveLoanRequest req) {
        ApproveLoanRequest r = req != null ? req : new ApproveLoanRequest(null, null, null);
        fineract.loanCommand(loanId, "approve", new FineractLoanCommands.Approve(
                dateOrToday(r.approvedOnDate()), r.approvedAmount(), r.note(),
                FineractDates.DATE_FORMAT, FineractDates.LOCALE));
        return currentStatus(loanId);
    }

    public LoanStatusResponse undoApproval(long loanId, UndoApprovalRequest req) {
        fineract.loanCommand(loanId, "undoApproval",
                new FineractLoanCommands.UndoApproval(req == null ? null : req.note()));
        return currentStatus(loanId);
    }

    public LoanStatusResponse reject(long loanId, RejectLoanRequest req) {
        RejectLoanRequest r = req != null ? req : new RejectLoanRequest(null, null);
        fineract.loanCommand(loanId, "reject", new FineractLoanCommands.Reject(
                dateOrToday(r.rejectedOnDate()), r.note(), FineractDates.DATE_FORMAT, FineractDates.LOCALE));
        return currentStatus(loanId);
    }

    // ---- Money movements -----------------------------------------------------------------

    public LoanStatusResponse disburse(long loanId, DisburseLoanRequest req) {
        DisburseLoanRequest r = req != null ? req : new DisburseLoanRequest(null, null, null);
        paymentTypes.requireSelectable(r.paymentTypeId());
        fineract.loanCommand(loanId, "disburse", new FineractLoanCommands.Disburse(
                dateOrToday(r.actualDisbursementDate()), null, r.paymentTypeId(), r.note(),
                FineractDates.DATE_FORMAT, FineractDates.LOCALE));
        return currentStatus(loanId);
    }

    /**
     * Records a repayment and returns the stored transaction, which shows how Fineract split
     * the amount over penalties, fees, interest and principal (the product's allocation order).
     */
    public LoanTransactionResponse repay(long loanId, RepaymentRequest req) {
        paymentTypes.requireSelectable(req.paymentTypeId());
        FineractCommandResult result = fineract.repayLoan(loanId, new FineractLoanCommands.Repayment(
                dateOrToday(req.transactionDate()), req.amount(), req.paymentTypeId(), req.note(),
                FineractDates.DATE_FORMAT, FineractDates.LOCALE));
        return LoanMapper.toTransaction(fineract.getLoanTransaction(loanId, result.resourceId()));
    }

    // ---- Helpers -------------------------------------------------------------------------

    /** Fineract's command responses don't include the new status, so we read it back. */
    private LoanStatusResponse currentStatus(long loanId) {
        FineractLoan loan = fineract.getLoanWithSchedule(loanId);
        return new LoanStatusResponse(loan.id(), loan.accountNo(),
                LoanMapper.statusOf(loan), LoanMapper.statusLabelOf(loan));
    }

    private static String dateOrToday(LocalDate date) {
        return FineractDates.format(date != null ? date : FineractDates.today());
    }
}
