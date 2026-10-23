# Adey MFI — project context for Claude Code

Learning + portfolio project: a loan-officer system for a fictional Ethiopian microfinance
institution ("Adey MFI"), built on top of Apache Fineract. The owner (Mike) is learning
microfinance, Fineract and Spring Boot through this project — explain non-obvious choices
briefly as you work, and keep code readable over clever.

## Architecture

```
Flutter app (app/)  →  Spring Boot backend (backend/)  →  Apache Fineract (Docker, infra/)
```

- The Flutter app NEVER talks to Fineract directly. The backend hides Fineract credentials,
  cleans up Fineract's verbose responses, and holds our own business rules.
- Money is always `BigDecimal` in Java (never double). Currency: ETB, 2 decimals.

## Folders

| Folder | What |
| --- | --- |
| infra/ | docker-compose.yml: PostgreSQL 18 (host port 5433) + apache/fineract + openmf/web-app:1.14 |
| backend/ | Spring Boot API (Java 17+, Maven wrapper) |
| app/ | Flutter app (later) |
| calc/ | loan-schedule.xlsx — reference schedule that matches Fineract to the cent |
| postman/ | Postman collection + local environment |
| notes/ | Learning notes, one file per stage |

## Running locally (Windows, Docker Desktop)

- Start: `cd infra && docker compose up -d` (Fineract takes 1–3 min to boot)
- Postgres is on host port **5433** (`localhost:5433`, postgres/postgres); 5432 belongs to another project.
- Health: `curl -k https://localhost:8443/fineract-provider/actuator/health` → `{"status":"UP"}`
- Mifos admin UI: http://localhost:4200 (mifos / password, tenant default).
  Stop it when not needed to save RAM: `docker compose stop web-app`
- Stop everything: `docker compose stop`. NEVER run `docker compose down -v` without asking — it wipes all data.
- Machine RAM is limited: run the backend with `-Xmx512m`; run Flutter on a real phone, not an emulator.

## Fineract facts we learned the hard way

- Base URL: `https://localhost:8443/fineract-provider/api/v1`, self-signed certificate
  (trust it in the local dev profile only).
- Every request needs header `Fineract-Platform-TenantId: default` + Basic auth (mifos/password locally).
- Fineract version is 1.16.0-SNAPSHOT (develop build) — features may differ from docs; check
  Swagger at https://localhost:8443/fineract-provider/swagger-ui/index.html
- Dates come BACK as arrays `[2026, 10, 3]`. Dates are SENT as strings plus format fields:
  `"submittedOnDate": "03 October 2026", "dateFormat": "dd MMMM yyyy", "locale": "en"`.
- Every time quantity needs a unit pair: `loanTermFrequency` + `loanTermFrequencyType`,
  `repaymentEvery` + `repaymentFrequencyType`. Frequency type codes: 0 days, 1 weeks, 2 months.
- Other codes: amortizationType 1 = equal installments, 0 = equal principal;
  interestType 0 = declining balance, 1 = flat; interestCalculationPeriodType 1 = same as
  repayment period; transactionProcessingStrategyCode `mifos-standard-strategy`.
- State changes use commands: `POST /loans/{id}?command=approve|disburse|undoApproval`,
  `POST /loans/{id}/transactions?command=repayment`.
- Preview a schedule without saving: `POST /loans?command=calculateLoanSchedule`.
- Loan products need an explicit `allowPartialPeriodInterestCalculation: false` or loan
  submission fails (the write-side field is misspelled `allowPartialPeriodInterestCalcualtion`).
- Currencies: `PUT /currencies` REPLACES the whole list — always send every currency to keep.
- Errors return JSON with `defaultUserMessage` and an `errors[]` array naming the field.
- Wrong-state loan commands can come back as 400 (not 403), e.g. disbursing an unapproved loan.
- Command responses (`POST /loans...`) only return ids (`resourceId`), not the new status.
- Disbursement posts an Accrual transaction (bookkeeping) even with accounting "none".

## Existing data (default tenant)

- Offices: Head Office (id 1), Bishoftu Branch (id 2, opened 2026-10-03), Adama Branch (id 3, opened 2025-01-01).
- Demo data comes from the seed profile (`backend/src/main/java/et/adey/mfi/seed/SeedPlan.java`, idempotent):
  6 staff with Fineract users (officers samuel, hana, meron, dawit; managers tadesse, selamawit), roles
  "Loan Officer" / "Branch Manager", 14 clients with loans in every state. Accounts + client table:
  backend/README.md "Demo accounts (local only)".
- Loan id 1 (000000001), client 1 Yohannes (samuel): 20,000 / 12 months, Active since 2026-10-23, partly
  repaid (Phase 2/3 demos).
- Payment types: Money Transfer (id 1), Cash (id 4); ids 2–3 are Fineract system-defined (hidden by our API).
- Loan product id 1: "Small Business Loan" (SBL), ETB, principal 5,000–50,000 (default 20,000),
  6–24 monthly repayments (default 12), 18% per year, equal installments, declining balance,
  same-as-repayment-period interest, no accounting.
- Reference check: 20,000 / 12 months / 18% → installment 1,833.60, last 1,833.61,
  total interest 2,003.21.

## Current status

- Stage 0 (environment) done. Stage 1 (loan fundamentals) mostly done.
- Backend Phase 1 done (2026-10-23): Java 21 read-only API in `backend/`
  (offices, loan products, clients, client loans, schedule preview), verified live against
  Fineract, 18 tests. Run: `cd backend && .\mvnw.cmd spring-boot:run`. Details: backend/README.md,
  notes/02-backend.md.
- Backend Phase 2 done (2026-10-23): loan lifecycle (submit with our one-open-loan rule -> 409,
  approve / undo approval / reject, disburse, repayments with principal/interest/fee/penalty split),
  loan details, payment types. Verified live with loan 1; 32 tests. Notes: notes/03-loan-lifecycle.md.
- Phase 3 done (2026-10-23): demo seed (profile `seed`), wrong-state errors -> 422 by Fineract error code,
  own DB `adey_backend` (JPA + Flyway: refresh_tokens, audit_log), officer login with Spring Security + JWT
  (login/refresh/logout/me, refresh rotation + reuse detection), data scoping (officer = own clients,
  manager = own office), manager-only approve/reject/undo/disburse, audit row per action. 56 tests.
  Notes: notes/04-security.md.
- Next: the Flutter app (see the report's list of what it needs from the backend).

## Backend facts

- Spring Boot 4.1.2 + springdoc 3.1.1 (upgraded from 3.5 on 2026-10-23, see notes/02-backend.md).
  Jackson 3: `tools.jackson.databind.*` (annotations still `com.fasterxml.jackson.annotation`).
- Fineract client search (`displayName`) is a case-sensitive substring match.
- Fineract OpenAPI says dates are strings; real responses send `[y, m, d]` arrays.
- Fineract reports `inArrears` at the loan's top level (summary.inArrears is null in this build).
- Wrong-state loan commands come back as 400; recognise them by `userMessageGlobalisationCode`
  (list in `FineractErrorCodes`).
- Fineract password policy is "strong": 12+ chars, mixed case, digit, symbol, no repeated consecutive chars.
- Our backend calls Fineract as the service account `mifos`; our AccessPolicy, not Fineract permissions,
  protects the API. Officers' Fineract roles are read at login (POST /authentication).
- Backend DB: `adey_backend` / login `adey` (password `adey`, local) in the same Postgres, host port 5433.
  JWT_SECRET is required outside the local profile.
- Drive C: has run full before (2026-10-23). If builds or Postgres act strangely, check free space first.
