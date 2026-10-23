# 05 — Portfolio, PAR, and the last backend round (Phase 4)

Date: 2026-10-23

## What PAR is, in plain words

A microfinance institution lends small amounts to many people. Its biggest risk is that borrowers
stop repaying. **Portfolio at Risk (PAR)** is the standard way to measure that risk:

> Of all the money our clients still owe us (principal outstanding), how much sits with clients
> who are late?

```
           principal OUTSTANDING of loans with an installment more than N days late
PAR(N) =   ------------------------------------------------------------------------   x 100
                       principal outstanding of all active loans
```

- **PAR1** (N = 1): any real lateness. An early warning; often caused by a client who is a few days late.
- **PAR30** (N = 30): more than a month late. The headline number for lenders, investors and regulators.
  In microfinance, a PAR30 above about **5%** is a warning sign and above **10%** a serious
  problem. Healthy MFIs stay well under 5%.

### Why the WHOLE outstanding balance, not just the overdue amount

When a client stops paying, the risk isn't the one missed installment: it's **everything they
still owe**. If Getachew has missed 3 installments (about 10,000 ETB) but still owes 24,745 ETB of
principal, all 24,745 is at risk of never coming back. Counting only the overdue installments would
make a bad portfolio look healthy. That's why PAR is defined on principal outstanding, and why our
code has a test asserting it is *not* the overdue amount.

### Why MFIs watch it daily

- **Early action.** A loan that is 3 days late is usually still recoverable with a visit or a call;
  at 90 days it often isn't. PAR1 tells officers where to go *this week*.
- **Officer performance.** PAR30 per loan officer (our manager dashboard) shows whose clients
  struggle, which points to coaching, workload or client selection.
- **Provisioning and funding.** Regulators and investors judge an MFI by PAR30. It decides how much
  money must be set aside for losses and whether lenders will fund the MFI.

## Our numbers (seeded data, 2026-10-23)

**Adama Branch (manager Selamawit):**

| Loan | Client | Officer | Principal outstanding | Days overdue | In PAR30? |
| --- | --- | --- | ---: | ---: | --- |
| 7 | Getachew Ayele | Meron | 24,745.16 | 105 | yes |
| 9 | Solomon Tefera | Dawit | 12,000.00 | 83 | yes |
| 6 | Tigist Haile | Meron | 12,098.49 | 0 | no |
| 11 | Meseret Bekele | Dawit | 7,892.51 | 0 | no |
| | **total** | | **56,736.16** | | |

```
PAR30 = (24,745.16 + 12,000.00) / 56,736.16 x 100 = 36,745.16 / 56,736.16 x 100 = 64.76 %
```

PAR1 is the same here (nobody is 2–30 days late). Per officer: **Meron 67.16 %**
(24,745.16 / 36,843.65), **Dawit 60.32 %** (12,000.00 / 19,892.51).

If we had wrongly used the *overdue amount* (13,302.08), we'd report 23.45%. That's still bad,
but far too optimistic.

64.76% is of course absurd for a real MFI: the seed deliberately put two of four active Adama
loans deep in arrears so the dashboard has something to show. **Bishoftu Branch** has PAR30
**0.00%** (two active loans, both up to date).

`daysOverdue` = days since the **oldest unpaid** installment's due date: Getachew's installment 3
was due 2026-07-10 → 105 days; Solomon's installment 1 was due 2026-08-01 → 83 days. An
installment due *today* is not yet overdue (0 days).

## How the dashboard gets its data (and why no cache)

Fineract's `GET /loans` returns every loan with its summary (outstanding, overdue, `inArrears`,
`overdueSinceDate`) in one call, but it can't filter by office or officer and has no schedules
(`associations=repaymentSchedule` is ignored on the list; the `delinquent` block is empty without
delinquency buckets and COB jobs). So `PortfolioReader` makes:

1. `GET /clients?officeId=..[&staffId=..]`: the caller's clients (1 call per 200 clients);
2. `GET /loans`: all loans (1 call per 200), kept only if their client is in (1);
3. `GET /loans/{id}?associations=repaymentSchedule` for **active** loans in scope only, in parallel
   on Java 21 virtual threads, at most 8 at a time.

Samuel needs 3 calls, Selamawit 6. Measured 1.0–1.5 s (3.5 s for the very first call after start,
while the JVM warms up). No cache: the numbers stay fresh after every repayment, and the speed is fine. If it
gets slow with real volumes, the next steps are, in order: a 30–60 s cache per user; Fineract's
own reports (`/runreports`, which include PAR); or a nightly copy into our own database.

## Idempotency: why field officers need it

An officer collects 1,833.60 ETB in a village with one bar of signal and taps *Record repayment*.
The request reaches the backend and Fineract books it, but the answer is lost on the way back.
The app shows "network error". The officer, with the client watching, taps again. Without protection
that's **a second 1,833.60 repayment** that never happened: the client's balance is wrong and the
cash count at the branch won't match.

With `Idempotency-Key`, the app creates a random key once per form submission and sends the same key
on every retry. The backend stores the first answer, so the retry gets that answer back (header
`Idempotent-Replayed: true`) **without calling Fineract again**. Live: two identical requests,
one Fineract transaction (#41).

Design choices (in `IdempotencyService`):
- the key is tied to the request body (SHA-256), so reusing a key for a *different* payment is
  refused (422);
- a first attempt that was **refused** (403/409/422/400) releases the key, because nothing happened;
- a first attempt that failed **after possibly reaching Fineract** (timeout, 5xx) blocks the key
  (`IDEMPOTENCY_OUTCOME_UNKNOWN`). We choose "please check the loan" over "maybe charge twice";
- keys are per user, expire after 24 h, cleaned up hourly.

## Login hardening

5 failed logins in 15 minutes per username **or** per IP → 429 + `Retry-After`. Per username stops
guessing one officer's password; per IP stops one device trying many usernames. Kept **in memory**:
fine for one backend, but counts reset on restart and aren't shared between instances. A shared
store (database or Redis) is needed before running more than one instance. Every login success,
failure and lockout is audited.

## Problems hit

1. **Fineract code values look different from enum options.** Gender comes back as
   `{"id": 24, "name": "Female"}`; enum options (status, frequency) as `{"id", "code", "value"}`.
   Our client's gender was null until `FineractCodeValue.label()` read `name` too.
2. **The Gender code ships empty**; the seed now creates Male/Female.
3. **The loan list's `status` filter takes numbers** (`status=300`). Words give a 500 from Fineract.
   We filter in memory instead.
4. **A Phase 1 bug surfaced:** `GET /api/clients?size=1000` gave **500**. `@Validated` controllers throw
   `ConstraintViolationException` for out-of-range query parameters, which fell into the catch-all
   handler. Now 400 `VALIDATION_FAILED`. Found by the new "every error has a code" test.
5. **Error codes on Spring's built-in errors:** for some errors (unknown URL) Spring only builds the
   ProblemDetail inside `super.handleExceptionInternal`, so the code must be added *after* calling it.
6. **Shared test context:** failed-login tests in one class could lock out logins in another (the
   limiter is a singleton), so every API test resets it first.

## State left in the local Fineract

- New clients: Selam Worku (id 16) and Mekdes Alemayehu (id 18), both Samuel's, created through the
  API during the demos.
- Loan 1 (Yohannes): two more 300 ETB repayments (Phase 4 idempotency demos).
- Dawit is not locked any more after a backend restart (the limiter is in memory).
