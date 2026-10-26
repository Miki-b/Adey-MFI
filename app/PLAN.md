# Adey Field: plan

The Flutter app for Adey MFI's field staff. It talks only to the Adey backend (`backend/`), never to
Fineract. This file is the plan across sessions; each session updates the status column.

## Users

| User | Works where | Needs |
| --- | --- | --- |
| **Loan officer** | in the field: client visits, market days | collect repayments (cash, telebirr, M-Pesa), onboard clients, take loan applications, know who to visit today |
| **Branch manager** | in the branch | approve / reject / disburse loans, watch branch PAR per officer, reconcile mobile money |

Both log in with their Fineract username and password. The backend decides what each may see (an
officer sees own clients only; a manager sees the office) and enforces manager-only actions. The app
only hides what a role can't use.

## Requirements

MVP:

| # | Requirement | Backend | Session | Status |
| --- | --- | --- | --- | --- |
| R1 | Login (and logout, session refresh) | `/api/auth/*` | A | done |
| R2 | Officer dashboard: portfolio, PAR30, due today, overdue | `GET /api/me/portfolio` | A | done |
| R3 | Client search and details (with loans) | `GET /api/clients`, `/api/clients/{id}`, `/api/clients/{id}/loans` | A | done |
| R4 | New client | `POST /api/clients` (Idempotency-Key) | B | |
| R5 | Loan application with schedule preview | `POST /api/loans/schedule-preview`, `POST /api/loans` | B | |
| R6 | Cash repayment with interest/principal split | `POST /api/loans/{id}/repayments` | B | |
| R7 | telebirr / M-Pesa collection with live status | `POST /api/loans/{id}/mobile-payments`, poll `GET /api/mobile-payments/{id}` | B | |
| R8 | Collections: overdue loans, most late first | `GET /api/loans?inArrears=true` | C | |
| R9 | Manager approvals: approve, reject, disburse | `GET /api/loans?status=PENDING`, `/approve`, `/reject`, `/disburse` | C | |
| R10 | Branch PAR by officer | `GET /api/me/portfolio` (`officers`) | A (dashboard), C (detail) | dashboard done |
| R11 | Reconciliation (managers) | `/api/reconciliation/*` | C | |

Later:

| # | Requirement | Session |
| --- | --- | --- |
| R12 | Amharic + Ethiopian calendar | D |
| R13 | Shareable receipts | D |
| R14 | Offline repayments (queue, then sync with the same Idempotency-Key) | later |
| R15 | PIN lock | D |
| R16 | End-of-day cash summary | later |

## Sessions

| Session | Scope | Status |
| --- | --- | --- |
| **A: foundation** | FVM, project, generated API client, server settings, auth (R1), dashboard (R2, R10 overview), clients (R3), shared formatting / errors / l10n / idempotency helper, tests | done |
| **B: officer money flows** | R4 new client, R5 loan application + schedule preview, R6 cash repayment, R7 mobile money with live status | |
| **C: manager and collections** | R8 collections, R9 approvals (approve, reject, disburse), R10 branch PAR by officer, R11 reconciliation | |
| **D: field polish and demo** | R12 Amharic + Ethiopian calendar, R13 receipts, R15 PIN lock, release APK, demo video | |

## Screen map

```
Login
  └─▶ Home (bottom tabs)
        ├─ Dashboard
        ├─ Clients ──▶ New client
        │     └─▶ Client details ──▶ New loan
        │            └─▶ Loan details ──▶ Record repayment (cash or mobile money)
        ├─ Collections
        ├─ Approvals (managers only) ──▶ Loan review
        └─ More ──▶ Reconciliation (managers only)
                └─▶ Settings (server URL, about, logout)
```

## Rules that hold in every session

- Money comes from the backend as values and is formatted, never computed in `double`; if a
  calculation is ever needed, use `Decimal`.
- Every user-facing string is in `lib/l10n/app_en.arb`.
- Errors are shown through one mapper from the backend's stable `code` values (with `detail` as fallback).
- State-changing actions send an `Idempotency-Key` created when the user confirms, reused on retries.
- The generated client in `packages/adey_api` is never edited by hand; repositories wrap it.
