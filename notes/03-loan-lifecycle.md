# 03 — Loan lifecycle (backend Phase 2)

Date: 2026-10-23

## The lifecycle in lending terms

```
            submit                approve               disburse            repayments...
 (client) ---------> PENDING ---------------> APPROVED ------------> ACTIVE ------------> CLOSED
                        |  ^                     |
                 reject |  | undo approval       | (undo approval also possible here,
                        v  +---------------------+  until money is paid out)
                    REJECTED
```

| Step | Endpoint | What it means at an MFI |
| --- | --- | --- |
| **Submit (apply)** | `POST /api/loans` | The loan officer records the client's request: amount, term, product. Nothing is owed yet. The application waits for a credit decision. |
| **Approve** | `POST /api/loans/{id}/approve` | A credit decision: someone with authority (branch manager, credit committee) agrees to lend, possibly a lower amount than asked (`approvedAmount`). Still no money has moved. |
| **Undo approval** | `POST /api/loans/{id}/undo-approval` | The decision is taken back (e.g. wrong amount entered) and the loan returns to pending. Only possible before disbursement. |
| **Reject** | `POST /api/loans/{id}/reject` | The application is declined (e.g. too much existing debt, weak cash flow). Final; the client may apply again later. |
| **Disburse** | `POST /api/loans/{id}/disburse` | The money is handed over (cash, transfer). From this moment the client owes principal, interest starts to count and the repayment schedule is fixed. The loan is **Active**. |
| **Repayment** | `POST /api/loans/{id}/repayments` | The client pays. Fineract allocates each payment to the oldest unpaid installment, in the product's order. When everything is paid the loan becomes **Closed (obligations met)**. |

Two kinds of rules guard this:
- **Fineract's state machine.** You can't disburse an unapproved loan, approve a rejected one, etc.
- **Our own rule** (in `LoanLifecycleService`, not in Fineract): *one loan in progress per client*.
  A pending, approved or active loan blocks a new application with `409 Conflict`
  (`code: OPEN_LOAN_EXISTS`). MFIs use this to avoid over-indebtedness: a client shouldn't
  carry two loans from us at once. Fineract would happily accept parallel applications.

## How a repayment is split

The product uses `mifos-standard-strategy`, named in Fineract "**Penalties, Fees, Interest,
Principal order**". For each payment Fineract takes the oldest installment that isn't fully
paid and fills its buckets in that order, then moves to the next installment with whatever
is left.

Why that order? It protects the lender's income and keeps the balance honest. Penalties and fees
are collected first, then the interest earned so far, and only then does the payment reduce
the principal, which is the amount future interest is calculated on.

Live example (loan 1: 20,000 ETB, 12 months, 18%/yr declining balance, disbursed 2026-10-23):

| Payment | Applied to | Penalties | Fees | Interest | Principal | Principal outstanding after |
| --- | --- | --- | --- | --- | --- | --- |
| 1833.60 | installment 1 (due 300.00 interest + 1533.60 principal) | 0 | 0 | **300.00** | **1533.60** | **18,466.40** |
| 1000.00 | installment 2 (due 277.00 interest + 1556.60 principal) | 0 | 0 | **277.00** | **723.00** | 17,743.40 |

The 1000 first paid installment 2's interest in full (277.00), and the remaining 723.00 went to
its principal. Installment 2 still has 833.60 outstanding (1556.60 − 723.00 principal), which is
exactly what `nextDueAmount` shows in `GET /api/clients/1/loans`.

Installment 2's interest is 277.00 = 18,466.40 × 18% / 12. Interest is charged on the balance
*after* installment 1, which is why paying principal early lowers future interest
(declining balance).

## What we built

- `LoanLifecycleService`: submit (with our rule), approve, undo approval, reject, disburse, repay.
- `LoanService.buildLoanRequest(...)`: one builder shared by the schedule preview and the real
  application, so a preview always matches what gets submitted.
- `FineractClient`: `submitLoanApplication`, `loanCommand(id, command, body)`, `repayLoan`,
  `getLoanTransaction`, `getLoanWithScheduleAndTransactions`, `getPaymentTypes`.
- `GET /api/loans/{id}` (totals, dates, schedule with paid/outstanding, transactions) and
  `GET /api/payment-types`.
- Fineract's command responses only contain ids, so after each step we read the loan back to
  return its new status. A repayment returns the stored transaction, which shows the split.
- Tests: `LoanLifecycleServiceTest` runs the real `FineractClient` against `MockRestServiceServer`,
  checking the business rule (no `POST /loans` is even attempted), the exact request bodies
  (dates, optional fields left out) and the repayment split. `LoanMapperTest` now uses the
  **real** captured loan 1 and transaction 4 responses. 32 tests in total.

## Problems hit and surprises

1. **Some wrong-state errors are 400, not 403.** Disbursing an unapproved loan returns
   `400 "Loan Disbursal is not allowed. Loan Account is not in approved and not disbursed state."`
   (field `id`). We pass it through as 400 with Fineract's message; still clear for the user.
2. **An "Accrual" transaction appears at disbursement** (2003.21, the whole interest), even with
   accounting set to "none". It's Fineract booking interest as income, not money moving. We hide
   accrual entries (`type.accrual`, `accrualActivity`, `accrualAdjustment`) from the loan details.
3. **Command responses don't include the new status.** Hence the extra `GET /loans/{id}` after each step.
4. **Payment types already existed**: "Money Transfer" (id 1, user-defined) plus two
   system-defined ones (*Repayment Adjustment Chargeback/Refund*) that Fineract uses internally.
   So we did *not* create Cash / Bank Transfer (the instruction was "only if there are none").
   We hide the system-defined ones. To add Cash later:
   `POST /paymenttypes {"name":"Cash","description":"Cash","isCashPayment":true,"position":2}`.
5. **Early repayments still go to installment 1.** Both payments were made on the disbursement
   day, a month before the first due date; Fineract still allocated them to installment 1 and then 2.
   Interest isn't reduced for paying early with this product (no interest recalculation).
6. **The race in our rule.** Two simultaneous applications for the same client could both pass the
   check. That's acceptable for one officer per client; a database lock or Fineract-side check
   would be needed if that changes.
7. **Not run live:** reject and undo-approval. Loan 1 is active, so our own rule blocks any new
   application for client 1 (the only client), and undoing loan 1's approval was no longer
   possible after disbursement. Both are covered by the mock-server tests.
8. **Line endings on Windows.** Editing files with Python rewrote LF as CRLF. Harmless for most
   files (`core.autocrlf` normalises them), but the root `README.md` contains a UTF-16 line, so
   git treats it as binary and doesn't normalise. Edit that one byte-for-byte.

## State left in the local Fineract

- Loan 1 (account 000000001) for client 1: 20,000 ETB, **Active**, disbursed 2026-10-23,
  repaid 1833.60 + 1000.00, principal outstanding 17,743.40.
