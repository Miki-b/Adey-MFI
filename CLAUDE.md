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
| app/ | Flutter app "Adey Field" (FVM, Riverpod, go_router, generated Dart client in app/packages/adey_api) |
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
  submission fails. On product CREATE (POST /loanproducts) the correct spelling is required; the
  misspelled `allowPartialPeriodInterestCalcualtion` is refused there (checked 2026-10-24).
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
- Phase 4 done (2026-10-23), backend feature-complete for the MVP: client onboarding (POST/PUT
  /api/clients), loan lists (GET /api/loans: PENDING queue, arrears list by daysOverdue), dashboard
  (GET /api/me/portfolio with PAR1/PAR30 on principal outstanding, per-officer for managers),
  Idempotency-Key on loan application / repayment / client creation (Flyway V2), login rate limit
  (in memory, 5 per 15 min per username/IP -> 429), stable `code` on every error, backend/openapi.json
  exported by OpenApiSpecTest. 91 tests. Notes: notes/05-portfolio-and-par.md.
- Phase 5 done (2026-10-24): mobile money repayments with the built-in TelePay simulator (local profile,
  /sim/**, chaos switches), HMAC-signed webhooks (5 min window, eventId dedupe, inbox pattern), posting
  worker with exponential backoff and receipt-number double-post guard, daily + on-demand reconciliation
  (MATCHED / PAID_NOT_POSTED / POSTED_NOT_PAID / AMOUNT_MISMATCH) with manager resolve. Flyway V3, V4.
  122 tests. Notes: notes/07-mobile-money.md.
- Codespaces (2026-10-24): `.devcontainer/` (Java 21 + docker-in-docker + sshd, 2 CPU / 8 GB) runs db +
  Fineract + seed + backend in the cloud; only port 8080 forwarded (private). Driven from Windows with
  `scripts/codespace.ps1 start|stop|status|logs|url|public|private`. See README "Run everything in GitHub
  Codespaces".
- Phase 5b done (2026-10-24): adapter pattern for mobile money (`MobileMoneyProvider`; `SimulatorProvider`,
  `ChapaProvider`), selected with `app.mobile-money.provider=simulator|chapa` (env MOBILE_MONEY_PROVIDER).
  Chapa test mode for telebirr + M-Pesa: webhooks only a hint, verify-before-post (`PaymentVerifier`, also
  polls PENDING with backoff), reconciliation by verify calls (`SettlementSource`). Request takes `method`
  TELEBIRR|MPESA. Flyway V5. 165 tests. Verified live in the Codespace. Notes: notes/08-chapa.md.
- Flutter Session A done (2026-10-26): app/ "Adey Field" (plan in app/PLAN.md, sessions A–D): FVM-pinned
  Flutter 3.41.6, generated client (app/tool/generate_api.ps1), auth with refresh-once interceptor, dashboard
  (PAR30), clients + details, server URL setting, error mapper, Decimal money, idempotency helper. Notes:
  notes/09-flutter-foundation.md.
- API contract + theme (2026-10-27): operationIds on all endpoints, required = non-null from JSpecify `@Nullable`
  on response records (`config/OpenApiConfig`), money `format: decimal`, `Problem` on 4XX/5XX, `ErrorCodes.ALL` +
  `ErrorCodeCatalogTest`, NOT_AN_OFFICER, case-insensitive client search, seed genders, provider-neutral inbox
  wording, Chapa webhook secret rotated. 169 backend tests. App: client regenerated, brand theme (green/yellow,
  WCAG AA tested), Theme setting; 72 app tests. Notes: notes/10-api-contract-and-theme.md.
- Next: Flutter Session B (R4–R7: new client, loan application, cash repayment, mobile money).

## Backend facts

- Spring Boot 4.1.2 + springdoc 3.1.1 (upgraded from 3.5 on 2026-10-23, see notes/02-backend.md).
- New response fields: annotate `@Nullable` (org.jspecify) if they can ever be null; otherwise the spec marks them
  required and the generated Dart client fails to parse a null. New error codes: add to `common/ErrorCodes.java`
  and the README table (ErrorCodeCatalogTest checks both).
  Jackson 3: `tools.jackson.databind.*` (annotations still `com.fasterxml.jackson.annotation`).
- Fineract client search (`displayName`) is a case-sensitive substring match; our `/api/clients?search=` filters
  the scoped list itself, ignoring case (max 2,000 clients scanned).
- Fineract OpenAPI says dates are strings; real responses send `[y, m, d]` arrays.
- Fineract reports `inArrears` at the loan's top level (summary.inArrears is null in this build).
- Wrong-state loan commands come back as 400; recognise them by `userMessageGlobalisationCode`
  (list in `FineractErrorCodes`).
- Fineract password policy is "strong": 12+ chars, mixed case, digit, symbol, no repeated consecutive chars.
- Our backend calls Fineract as the service account `mifos`; our AccessPolicy, not Fineract permissions,
  protects the API. Officers' Fineract roles are read at login (POST /authentication).
- Backend DB: `adey_backend` / login `adey` (password `adey`, local) in the same Postgres, host port 5433.
  JWT_SECRET is required outside the local profile.
- Fineract code values (e.g. gender) are `{id, name}`; enum options are `{id, code, value}`.
- Fineract `GET /loans` list: summaries yes, schedules no (associations ignored), no office filter,
  `status` filter only numeric. `delinquent` block empty (no delinquency buckets configured).
- Seeded Adama PAR30 = 64.76% (36,745.16 / 56,736.16), on purpose; Bishoftu 0.00%.
- LAN: PC Wi-Fi IP 192.168.1.3 (Private network); firewall rule for TCP 8080 documented in backend/README.md.
- RAM is tight (~1.5 GB free): run the backend as ONE JVM (`mvnw package -DskipTests`, then
  `java -Xmx512m -jar target/mfi-backend-0.0.1-SNAPSHOT.jar`), stop it before `mvnw test` and before
  re-packaging (Windows locks the running jar).
- Mobile money: payment type "Mobile Money" (id 5); receipt number = provider transaction id. The simulator
  shares the backend JVM but is reached only over HTTP; its state is in memory (lost on restart).
- Spring Security's bearer filter reads every `Authorization: Bearer`; /sim/** and /api/webhooks/** are
  excluded on purpose (SecurityConfig.ourBearerTokens).
- Heavy work (Fineract, tests, builds) can run in the Codespace instead of locally:
  `gh codespace ssh -c <name> -- 'cd /workspaces/Adey-MFI/backend && ./mvnw -q test'`.
  Codespaces secrets (user, scoped to this repo): ADEY_JWT_SECRET (missing → start-stack.sh generates an
  ephemeral one), CHAPA_SECRET_KEY (present → provider chapa, else simulator), CHAPA_WEBHOOK_SECRET
  (also in Chapa's dashboard), CHAPA_ENCRYPTION_KEY (unused). New secrets reach a running Codespace's
  secrets file without a restart. gh needs the `codespace` scope.
- The Codespace's fresh seed has different ids than the local Fineract: in Bishoftu the active loan is
  loan 4 (Bethlehem Assefa, officer hana); samuel has no active loan there.
  Always `scripts/codespace.ps1 stop` at the end (2-core = 2 core-hours/hour; auto-stop after 30 min idle).
- The seed also bootstraps an EMPTY Fineract (ETB currency, SBL product, Bishoftu Branch, staff). Yohannes
  (client 1, loan 1) exists only in the local Fineract, not in a fresh Codespace.
- Drive C: has run full before (2026-10-23). If builds or Postgres act strangely, check free space first.

## Chapa facts (test mode, checked live 2026-10-24)

- API `https://api.chapa.co/v1`, `Authorization: Bearer CHASECK_TEST-…` (our app refuses non-test keys).
- Charge: `POST /charges?type=telebirr|mpesa`, multipart `amount, currency, tx_ref, mobile`. "Not initiated"
  comes as HTTP 200 with `"status":"failed"`. Success gives `data.meta.ref_id` (= Chapa reference, our receipt).
- Verify: `GET /transaction/verify/{tx_ref}`; `data.status` is `success`, `failed/cancelled` or `pending`;
  `amount` is a JSON number; unknown tx_ref → `"Invalid transaction reference"`, data null.
- Test numbers that work: telebirr 0900123456, M-Pesa 0700123456 (auto-complete in seconds). 0900112233 from
  the docs was rejected. Any other number → failed/cancelled.
- Webhook URL + secret hash only in the dashboard. `x-chapa-signature` = hex HMAC-SHA256(secret, raw body)
  (confirmed live); `chapa-signature` = HMAC(secret, secret), constant, ignored. No timestamp, no event id.
- No settlement listing for direct charges. Encryption key only for card/OTP flows.

## Flutter app facts

- Run Flutter only through FVM, from app/: `fvm flutter ...` / `fvm dart ...` (PowerShell finds `fvm`; Git Bash
  doesn't have it on PATH). Pinned 3.41.6 (Dart 3.11) => flutter_riverpod pinned `>=3.2.1 <3.3.0` (newer
  riverpod_generator needs Dart 3.12). FVM's "Can't load Kernel binary" warning is harmless.
- Never edit app/packages/adey_api; regenerate with `app\tool\generate_api.ps1` after backend/openapi.json changes.
  Generated fields are all nullable, money is `num` -> repositories convert with `toDecimal` (num.toString()).
- After regenerating the client, if build_runner fails with InvalidOutputException: `fvm dart run build_runner clean`.
- Theme: green = primary actions / active tab / headers, yellow = chips / tonal buttons / accents, never yellow text on
  light backgrounds; PAR colours teal/orange/red. test/core/theme/theme_contrast_test.dart guards WCAG AA.
- Riverpod/l10n outputs (*.g.dart, lib/l10n/app_localizations*.dart) are committed; regenerate with
  `fvm dart run build_runner build --delete-conflicting-outputs` and `fvm flutter gen-l10n`.
- Don't edit UTF-8 source files with Windows PowerShell 5.1 Get-Content/Set-Content (it mangles "–" etc.); use Python or the editor.
- intl: use `DateFormat(..., 'en_US')` (built in); plain 'en' needs initializeDateFormatting.
- Test phone: Samsung Galaxy F22 (SM-E225F) over USB, storage nearly full (uninstall before reinstalling).
  Best route keeps 8080 private: `gh codespace ports forward 8080:18080` + `adb reverse tcp:8080 tcp:18080`,
  app preset "USB cable" (http://127.0.0.1:8080, debug builds). The public-port route was flaky (relay 404s). A private port answers 401 `WWW-Authenticate: tunnel`
  or 302 to github.dev sign-in; the app shows "Codespace port is private".
- The Codespace's python3 is Debian python3-minimal (no json module): run checks from the PC via a private port forward.
- In the Codespace's seed: Bishoftu = office 2 (samuel, hana, tadesse), Adama = office 3 (meron, dawit, selamawit).
