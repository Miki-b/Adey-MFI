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

- Offices: Head Office (id 1), Bishoftu Branch (id 2)
- Staff id 1: Samuel Derese (loan officer, Bishoftu). Client id 1: Yohannes Tekle (Bishoftu, staff 1).
- Loan id 1 (000000001), client 1: 20,000 / 12 months, Active since 2026-10-23, repaid 1833.60 + 1000.00,
  principal outstanding 17,743.40. Client 1 can't apply again until it closes (our one-open-loan rule).
- Payment types: Money Transfer (id 1); ids 2–3 are Fineract system-defined adjustment types (hidden by our API).
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
- Next: Phase 3: officer login (Spring Security + JWT), then the Flutter app.

## Backend facts

- Spring Boot 4.1.2 + springdoc 3.1.1 (upgraded from 3.5 on 2026-10-23, see notes/02-backend.md).
  Jackson 3: `tools.jackson.databind.*` (annotations still `com.fasterxml.jackson.annotation`).
- Fineract client search (`displayName`) is a case-sensitive substring match.
- Fineract OpenAPI says dates are strings; real responses send `[y, m, d]` arrays.
