package et.adey.mfi.loan;

import et.adey.mfi.audit.AuditAction;
import et.adey.mfi.audit.AuditService;
import et.adey.mfi.common.ConflictException;
import et.adey.mfi.common.FineractDates;
import et.adey.mfi.fineract.FineractClient;
import et.adey.mfi.fineract.model.FineractClientAccounts.LoanAccount;
import et.adey.mfi.fineract.model.FineractClientData;
import et.adey.mfi.fineract.model.FineractCommandResult;
import et.adey.mfi.fineract.model.FineractLoan;
import et.adey.mfi.fineract.model.FineractLoanCommands;
import et.adey.mfi.fineract.model.FineractLoanProduct;
import et.adey.mfi.fineract.model.FineractLoanRequest;
import et.adey.mfi.paymenttype.PaymentTypeService;
import et.adey.mfi.security.AccessPolicy;
import et.adey.mfi.security.Officer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Write side of loans: the lifecycle
 * submit -> approve (or reject / undo approval) -> disburse -> repayments.
 *
 * Every method runs inside audit.record(...), so each attempt leaves exactly one audit_log row.
 * Inside that block, in this order:
 *   1. role check (approve/reject/undo/disburse need MANAGER)  -> 403, audited as DENIED
 *   2. access check (the loan's client must be the officer's / in the manager's office) -> 403
 *   3. our business rules, then the Fineract call (Fineract enforces its own state machine).
 * The role check is done here rather than with a URL rule or @PreAuthorize on purpose: those stop
 * the request before our code runs, and then refused attempts would be missing from the audit log.
 */
@Slf4j
@Service
public class LoanLifecycleService {

    /** Loan states that count as "the client already has a loan in progress". */
    static final Set<LoanStatus> OPEN_STATUSES =
            Set.of(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.APPROVED, LoanStatus.ACTIVE);

    private final FineractClient fineract;
    private final PaymentTypeService paymentTypes;
    private final LoanGuard guard;
    private final AccessPolicy access;
    private final AuditService audit;

    public LoanLifecycleService(FineractClient fineract, PaymentTypeService paymentTypes, LoanGuard guard,
                                AccessPolicy access, AuditService audit) {
        this.fineract = fineract;
        this.paymentTypes = paymentTypes;
        this.guard = guard;
        this.access = access;
        this.audit = audit;
    }

    // ---- Submit --------------------------------------------------------------------------

    public LoanStatusResponse submit(Officer officer, LoanApplicationRequest req) {
        return audit.record(officer, AuditAction.LOAN_APPLY, rec -> {
            rec.clientId(req.clientId()).amount(req.principal());
            FineractClientData client = guard.requireClient(officer, req.clientId());
            ensureNoOpenLoan(req.clientId());

            // The loan officer on the loan comes from the token, never from the request:
            // an officer applies as themselves; a manager applies on behalf of the client's officer.
            Long loanOfficerId = officer.isManager() ? client.staffId() : Long.valueOf(officer.staffId());

            FineractLoanProduct product = fineract.getLoanProduct(req.productId());
            FineractLoanRequest request = LoanService.buildLoanRequest(req.clientId(), product, req.principal(),
                    req.numberOfRepayments(), req.expectedDisbursementDate(), loanOfficerId);
            FineractCommandResult result = fineract.submitLoanApplication(request);

            long loanId = result.loanId() != null ? result.loanId() : result.resourceId();
            rec.loanId(loanId);
            log.info("Loan {} submitted for client {} by {}", loanId, req.clientId(), officer.username());
            return currentStatus(loanId);
        });
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

    // ---- Approval (manager only) ---------------------------------------------------------

    public LoanStatusResponse approve(Officer officer, long loanId, ApproveLoanRequest req) {
        ApproveLoanRequest r = req != null ? req : new ApproveLoanRequest(null, null, null);
        return managerAction(officer, AuditAction.LOAN_APPROVE, loanId, loan -> {
            fineract.loanCommand(loanId, "approve", new FineractLoanCommands.Approve(
                    dateOrToday(r.approvedOnDate()), r.approvedAmount(), r.note(),
                    FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            return currentStatus(loanId);
        }, r.approvedAmount());
    }

    public LoanStatusResponse undoApproval(Officer officer, long loanId, UndoApprovalRequest req) {
        return managerAction(officer, AuditAction.LOAN_UNDO_APPROVAL, loanId, loan -> {
            fineract.loanCommand(loanId, "undoApproval",
                    new FineractLoanCommands.UndoApproval(req == null ? null : req.note()));
            return currentStatus(loanId);
        }, null);
    }

    public LoanStatusResponse reject(Officer officer, long loanId, RejectLoanRequest req) {
        RejectLoanRequest r = req != null ? req : new RejectLoanRequest(null, null);
        return managerAction(officer, AuditAction.LOAN_REJECT, loanId, loan -> {
            fineract.loanCommand(loanId, "reject", new FineractLoanCommands.Reject(
                    dateOrToday(r.rejectedOnDate()), r.note(), FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            return currentStatus(loanId);
        }, null);
    }

    // ---- Money movements -----------------------------------------------------------------

    /** Paying out the money is a manager decision too. */
    public LoanStatusResponse disburse(Officer officer, long loanId, DisburseLoanRequest req) {
        DisburseLoanRequest r = req != null ? req : new DisburseLoanRequest(null, null, null);
        return managerAction(officer, AuditAction.LOAN_DISBURSE, loanId, loan -> {
            paymentTypes.requireSelectable(r.paymentTypeId());
            fineract.loanCommand(loanId, "disburse", new FineractLoanCommands.Disburse(
                    dateOrToday(r.actualDisbursementDate()), null, r.paymentTypeId(), r.note(),
                    FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            return currentStatus(loanId);
        }, null);
    }

    /**
     * Records a repayment (officers and managers) and returns the stored transaction, which shows
     * how Fineract split the amount over penalties, fees, interest and principal.
     */
    public LoanTransactionResponse repay(Officer officer, long loanId, RepaymentRequest req) {
        return audit.record(officer, AuditAction.LOAN_REPAY, rec -> {
            rec.loanId(loanId).amount(req.amount());
            FineractLoan loan = loadLoan(officer, loanId, rec);
            paymentTypes.requireSelectable(req.paymentTypeId());
            FineractCommandResult result = fineract.repayLoan(loanId, new FineractLoanCommands.Repayment(
                    dateOrToday(req.transactionDate()), req.amount(), req.paymentTypeId(), req.note(), null,
                    FineractDates.DATE_FORMAT, FineractDates.LOCALE));
            return LoanMapper.toTransaction(fineract.getLoanTransaction(loanId, result.resourceId()));
        });
    }

    // ---- Helpers -------------------------------------------------------------------------

    /** Audit + manager role + loan access, then the action. */
    private <T> T managerAction(Officer officer, AuditAction action, long loanId,
                                Function<FineractLoan, T> work, BigDecimal amount) {
        return audit.record(officer, action, rec -> {
            rec.loanId(loanId).amount(amount);
            access.requireManager(officer);
            FineractLoan loan = loadLoan(officer, loanId, rec);
            if (amount == null && action == AuditAction.LOAN_DISBURSE) {
                rec.amount(loan.approvedPrincipal());
            }
            return work.apply(loan);
        });
    }

    /**
     * Loads the loan, notes its client in the audit record, THEN checks access. In that order, a
     * refused attempt on someone else's loan is still logged with the client it concerned.
     */
    private FineractLoan loadLoan(Officer officer, long loanId, AuditService.AuditRecord rec) {
        FineractLoan loan = fineract.getLoanWithSchedule(loanId);
        rec.clientId(loan.clientId());
        guard.requireClient(officer, loan.clientId());
        return loan;
    }

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
