# Adey MFI backend

Spring Boot 4.1 API (Java 21, Jackson 3) that sits between the Flutter app and Apache Fineract.
The app never talks to Fineract directly: this service holds the Fineract credentials,
turns Fineract's verbose JSON into small clean DTOs, and is where our own business rules live.

```
Flutter app  ->  this backend (:8080)  ->  Fineract (https://localhost:8443)
                      |
                      +-> own Postgres DB adey_backend (refresh tokens, audit log)
```

Officers log in with their Fineract username + password and get a JWT; every `/api/**` call needs
it. How it works: [notes/04-security.md](../notes/04-security.md).

## Run it

Prerequisites: JDK 21, Fineract running (see `../infra`, check
`curl -k https://localhost:8443/fineract-provider/actuator/health` says `UP`), and the backend's
own database `adey_backend` in the same Postgres container (host port 5433).

**One-time database setup.** Fresh Postgres volumes get it from `infra/init-db.sql`. For a volume
created before 2026-10-23, run once:

```powershell
docker exec infra-db-1 psql -U postgres -c "CREATE ROLE adey LOGIN PASSWORD 'adey';" -c "CREATE DATABASE adey_backend OWNER adey;"
```

`adey` owns only `adey_backend` and has no rights on Fineract's databases. Flyway creates the
tables at startup (`src/main/resources/db/migration`).

```powershell
cd C:\dev\MFI\adey-mfi\backend
.\mvnw.cmd spring-boot:run        # starts on http://localhost:8080 with -Xmx512m
.\mvnw.cmd test                   # unit + web-layer tests, no Fineract needed
```

The `-Xmx512m` heap cap is set in `pom.xml` (`app.jvm.args`) and applies to both
`spring-boot:run` and the tests. If you run the jar yourself:
`java -Xmx512m -jar target/mfi-backend-0.0.1-SNAPSHOT.jar`.

**Low on RAM? Run the jar.** `mvnw spring-boot:run` keeps a Maven JVM alive *next to* the app JVM.
`.\mvnw.cmd package -DskipTests` then `java -Xmx512m -jar target\mfi-backend-0.0.1-SNAPSHOT.jar`
is one JVM. On Windows, stop the running app before re-packaging (the jar file is locked while it runs).

- Swagger UI: http://localhost:8080/swagger-ui.html. Call `POST /api/auth/login`, copy
  `accessToken`, click **Authorize**, paste it.
- Health: http://localhost:8080/actuator/health

Swagger and health need no token in the local profile only.

### Reach it from your phone (same Wi-Fi)

The backend listens on all network interfaces (`server.address: 0.0.0.0`, override with
`SERVER_ADDRESS`), so only Windows Firewall stands in the way.

1. Find the PC's Wi-Fi address: `ipconfig` -> "Wireless LAN adapter Wi-Fi" -> IPv4 Address
   (on this machine **192.168.1.3**).
2. The Wi-Fi network must be **Private** (Settings -> Network -> Wi-Fi -> network properties), not
   Public. Check with `Get-NetConnectionProfile`.
3. Allow inbound TCP 8080 on private networks only (PowerShell **as administrator**, once):
   ```powershell
   New-NetFirewallRule -DisplayName "Adey MFI backend 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private
   ```
   Remove it later with `Remove-NetFirewallRule -DisplayName "Adey MFI backend 8080"`.
4. On the phone's browser, open `http://192.168.1.3:8080/actuator/health`; it should say `UP`.
   In the Flutter app use base URL `http://192.168.1.3:8080`.

This is plain HTTP. Android blocks cleartext HTTP by default, so the Flutter debug build needs
`android:usesCleartextTraffic="true"` (or a network security config for this IP); iOS needs an
ATS exception. Never ship that in a release build. A real deployment uses HTTPS.
CORS does not matter for the mobile app (only for Flutter web).

## Demo data (seed)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local,seed"
```

Starts without a web server, makes Fineract match `seed/SeedPlan.java` and exits. Safe to run
again: everything is found by a stable key (`externalId` such as `SEED-C07`, username, name)
and loans are only moved *forward* to their planned state, so a second run changes nothing.

### Demo accounts (local only)

| Username | Password | Role | Office | Staff |
| --- | --- | --- | --- | --- |
| `samuel` | `Adey#Loan2026` | Loan Officer | Bishoftu Branch | Samuel Derese |
| `hana` | `Adey#Loan2026` | Loan Officer | Bishoftu Branch | Hana Girma |
| `tadesse` | `Adey#Lead2026` | Branch Manager | Bishoftu Branch | Tadesse Bekele |
| `meron` | `Adey#Loan2026` | Loan Officer | Adama Branch | Meron Alemu |
| `dawit` | `Adey#Loan2026` | Loan Officer | Adama Branch | Dawit Haile |
| `selamawit` | `Adey#Lead2026` | Branch Manager | Adama Branch | Selamawit Tesfaye |

These are demo passwords for a local Fineract only. Fineract's "strong" password policy requires
12+ characters with upper/lower case, a digit and a symbol, and no repeated consecutive characters.

### Demo clients

| Client (id) | Officer | Situation |
| --- | --- | --- |
| Yohannes Tekle (1) | samuel | active, partly paid (Phase 2 walkthrough) |
| Abebe Kebede (2) | samuel | no loan |
| Almaz Tadesse (3) | samuel | application pending (seed); approved by `tadesse` in the Phase 3 demo |
| Kidist Wolde (4) | hana | approved, not yet disbursed |
| Yonas Mekonnen (5) | hana | rejected |
| Bethlehem Assefa (6) | hana | active, just disbursed (nothing due yet) |
| Fikirte Mengistu (7) | hana | no seeded loan; one application rejected live through our API |
| Tigist Haile (8) | meron | active, every installment paid on time |
| Getachew Ayele (9) | meron | active, 2 installments paid then a 1,000 part payment: **in arrears** |
| Mulu Gebre (10) | meron | fully repaid (closed, obligations met) |
| Solomon Tefera (11) | dawit | active, never paid: **in arrears** since 2026-08-01 |
| Hirut Desta (12) | dawit | application pending |
| Ermias Girma (13) | dawit | no loan |
| Meseret Bekele (14) | dawit | active, every installment paid on time |

Fineract roles created by the seed (minimum permissions):
- **Loan Officer**: `READ_OFFICE, READ_STAFF, READ_LOANPRODUCT, READ_PAYMENTTYPE, READ_CODE,
  READ_CODEVALUE, READ_CLIENT, CREATE_CLIENT, ACTIVATE_CLIENT, READ_LOAN, CREATE_LOAN, REPAYMENT_LOAN`
- **Branch Manager**: all of the above, plus `APPROVE_LOAN, REJECT_LOAN, APPROVALUNDO_LOAN, DISBURSE_LOAN`

Payment type **Cash** (id 4, `isCashPayment: true`) is added next to the existing "Money Transfer".

## Configuration

All settings live in `src/main/resources/application.yml` and can be overridden with
environment variables.

| Env variable | Property | Default |
| --- | --- | --- |
| `FINERACT_BASE_URL` | `fineract.base-url` | `https://localhost:8443/fineract-provider/api/v1` |
| `FINERACT_TENANT` | `fineract.tenant` | `default` |
| `FINERACT_USERNAME` | `fineract.username` | `mifos` |
| `FINERACT_PASSWORD` | `fineract.password` | `password` |
| `FINERACT_CONNECT_TIMEOUT` | `fineract.connect-timeout` | `5s` |
| `FINERACT_READ_TIMEOUT` | `fineract.read-timeout` | `30s` |
| `SERVER_PORT` | `server.port` | `8080` |
| `SPRING_PROFILES_ACTIVE` | active profile | none, so `local` is used |
| `DB_URL` | `spring.datasource.url` | `jdbc:postgresql://localhost:5433/adey_backend` |
| `DB_USERNAME` / `DB_PASSWORD` | `spring.datasource.*` | `adey` / `adey` |
| `JWT_SECRET` | `app.security.jwt.secret` | **none**; local profile has a marked dev-only default. 32+ characters, required elsewhere (startup fails without it) |
| - | `app.security.jwt.access-token-ttl` | `15m` |
| - | `app.security.refresh-token-ttl` | `7d` |
| - | `app.cors.allowed-origins` | empty (local: `http://localhost:[*]`, `http://127.0.0.1:[*]`) |
| `SERVER_ADDRESS` | `server.address` | `0.0.0.0` (all interfaces; `127.0.0.1` = this PC only) |
| `TELEPAY_BASE_URL` | `app.mobile-money.provider-base-url` | **none** (local: `http://localhost:8080/sim`, the simulator) |
| `TELEPAY_API_KEY` | `app.mobile-money.api-key` | **none** (local: `local-telepay-api-key`) |
| `MOBILE_MONEY_WEBHOOK_SECRET` | `app.mobile-money.webhook-secret` | **none**, 32+ chars (local: marked dev-only default) |
| `MOBILE_MONEY_CALLBACK_URL` | `app.mobile-money.callback-url` | **none** (local: `http://localhost:8080/api/webhooks/mobile-money`) |
| - | `app.mobile-money.posting.*` | `max-attempts 8`, `base-delay 10s`, `max-delay 2m`, `poll-interval 5s` |
| - | `app.reconciliation.cron` | `0 30 0 * * *` (00:30 Addis Ababa, for the previous day) |
| - | `app.security.login.max-failures` / `.window` | `5` / `15m` |

Generate a real secret with e.g. `openssl rand -base64 48`.

### Profiles

- **local** (default when no profile is set), in `application-local.yml`:
  - trusts Fineract's self-signed certificate (`fineract.trust-self-signed-certificate: true`)
  - a **local-only** JWT signing key, used when `JWT_SECRET` is not set
  - Swagger UI, OpenAPI and health reachable without a token
  - CORS for `http://localhost:<any port>` (Flutter web dev)

  **Never run production with the local profile.** Trusting any certificate lets anyone
  intercept the Fineract credentials and customer data, and the default JWT key lets anyone forge tokens.
- **seed**: one-shot demo data loader (no web server), see above.
- **test**: used by the tests (H2 in-memory database, test-only key).
- Any other profile (e.g. `SPRING_PROFILES_ACTIVE=prod`) has normal certificate checks, requires
  `JWT_SECRET`, protects Swagger/health, and allows only the configured CORS origins.

## Endpoints

Every endpoint except login/refresh/logout needs `Authorization: Bearer <accessToken>`.

Auth (Phase 3):

| Method | Path | Body | Result |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | `username, password` (Fineract credentials) | `{accessToken, tokenType: "Bearer", expiresIn: 900, accessTokenExpiresAt, refreshToken, refreshTokenExpiresAt, officer}` |
| POST | `/api/auth/refresh` | `refreshToken` | a new pair; the old refresh token stops working |
| POST | `/api/auth/logout` | `refreshToken` | 204; that refresh token (and its family) is revoked |
| GET | `/api/auth/me` | - | `{userId, username, displayName, staffId, officeId, officeName, roles}` |

Access token claims: `sub` (username), `uid`, `staffId`, `officeId`, `officeName`, `name`, `roles`
(`OFFICER` / `MANAGER`), `iss`, `iat`, `exp`, `jti`. Only Fineract users linked to a staff member
with the **Loan Officer** or **Branch Manager** role can log in.

**Who sees what.** An officer sees only clients assigned to them (and those clients' loans). A
manager sees every client of their office. Both come from the token, never from query parameters.
**Approve, reject, undo approval and disburse need the MANAGER role**; officers can apply, preview
and record repayments.

Reads (Phase 1):

| Method | Path | What |
| --- | --- | --- |
| GET | `/api/offices` | id, name, parentId, openingDate |
| GET | `/api/loan-products` | ranges for principal / repayments, rate, interest method, amortization |
| GET | `/api/payment-types` | payment types an officer can pick (Fineract's system-defined ones hidden) |
| GET | `/api/clients?search=&page=0&size=20` | paged client list, scoped to the caller |
| POST | `/api/clients` | create a client (see "Client onboarding"); 201 + `Location`; `Idempotency-Key` supported |
| PUT | `/api/clients/{id}` | edit name / mobile / gender / date of birth / national ID (only the fields sent) |
| GET | `/api/loans?status=&inArrears=&clientId=` | loan list scoped to the caller (see "Loan lists") |
| GET | `/api/me/portfolio` | dashboard: counts, outstanding, PAR1/PAR30, due today, overdue, per officer (managers) |
| GET | `/api/clients/{id}` | client details |
| GET | `/api/clients/{id}/loans` | per loan: status, progress, outstanding, next due, arrears |
| GET | `/api/loans/{id}` | totals, key dates, full schedule (with paid/outstanding per installment), transactions |
| POST | `/api/loans/schedule-preview` | repayment schedule without creating a loan |

Loan lifecycle (Phase 2):

| Method | Path | Body (all dates optional, default today) | Result |
| --- | --- | --- | --- |
| POST | `/api/loans` | `clientId, productId, principal, numberOfRepayments, expectedDisbursementDate?` | 201 + `Location`, `{loanId, accountNo, status, statusLabel}`; loan officer = the caller (a manager applies on behalf of the client's officer) |
| POST | `/api/loans/{id}/approve` | `approvedOnDate?, approvedAmount?, note?` (body optional) | `{loanId, status: APPROVED, ...}`. **Manager** |
| POST | `/api/loans/{id}/undo-approval` | `note?` (body optional) | back to `SUBMITTED_AND_PENDING_APPROVAL`. **Manager** |
| POST | `/api/loans/{id}/reject` | `rejectedOnDate?, note?` (body optional) | `REJECTED`. **Manager** |
| POST | `/api/loans/{id}/disburse` | `actualDisbursementDate?, paymentTypeId?, note?` (body optional) | `ACTIVE`. **Manager** |
| POST | `/api/loans/{id}/repayments` | `amount, transactionDate?, paymentTypeId?, note?` | the transaction with `principalPortion`, `interestPortion`, `feesPortion`, `penaltiesPortion` |

Status values: `SUBMITTED_AND_PENDING_APPROVAL`, `APPROVED`, `ACTIVE`, `WITHDRAWN_BY_CLIENT`,
`REJECTED`, `CLOSED`, `WRITTEN_OFF`, `RESCHEDULED`, `OVERPAID`. `statusLabel` carries Fineract's text.

**Our business rule:** a client with a loan that is pending approval, approved or active cannot
apply for another one. The answer is `409 Conflict` with `"code": "OPEN_LOAN_EXISTS"`. Fineract
itself would allow it; the check is in `LoanLifecycleService`.

Repayments are split in the product's order **penalties, fees, interest, principal**, applied to
the oldest unpaid installment first. Example: 1000 against installment 2 (277.00 interest due)
gives `interestPortion 277.00`, `principalPortion 723.00`.

`GET /api/loans/{id}` lists money movements only. Fineract's accrual entries (interest booked
as income) are bookkeeping and are left out.

`paymentTypeId` must be one of `GET /api/payment-types` (e.g. Cash = 4); anything else gives 400.

**Audit log.** Every apply / approve / undo approval / reject / disburse / repayment attempt writes
one row to `audit_log` (who, staff, office, action, client, loan, amount, time, outcome
`SUCCESS` / `DENIED` / `REJECTED` / `ERROR`, and the error message). Refused attempts are included.

```powershell
docker exec infra-db-1 psql -U adey -d adey_backend -c "select * from audit_log order by id desc limit 20"
```

### Client onboarding (Phase 4)

`POST /api/clients` body: `firstname, lastname, mobileNo, gender (MALE|FEMALE), dateOfBirth,
externalId? (national ID), staffId? (managers only), activate? (default true)`.

- The office is always the caller's. An **officer** creates clients assigned to themselves
  (sending another `staffId` gives 403). A **manager** must choose `staffId`: an active loan officer
  of their own office.
- Our rules: mobile = 10 digits starting `09` or `07`; client at least **18 years** old.
- Duplicate mobile number or national ID -> 409 `DUPLICATE_CLIENT` (Fineract refuses, nothing created).
- Create and update are audited (`CLIENT_CREATE`, `CLIENT_UPDATE`).

### Loan lists (Phase 4)

`GET /api/loans` returns the loans of the caller's clients (officer) or office (manager). Filters:
`status` = `PENDING | APPROVED | ACTIVE | CLOSED | REJECTED`, `inArrears` = `true | false`, `clientId`.
Each item: `loanId, accountNo, clientId, clientName, clientMobileNo, productName, loanOfficerName,
status, statusLabel, principal, principalOutstanding, totalOverdue, daysOverdue, inArrears,
nextDueDate, nextDueAmount, submittedOn`.

- `daysOverdue` = days since the oldest unpaid installment's due date (0 if none; due today = 0).
- `inArrears=true` (collection list) is sorted by `daysOverdue`, most first. `status=PENDING`
  (manager approval queue) is sorted oldest application first. Otherwise by client name.
- `CLOSED` = repaid, written off or rescheduled; `REJECTED` includes withdrawn by the client.

### Dashboard (Phase 4)

`GET /api/me/portfolio`: `counts` (clients, active loans, pending, approved-not-disbursed, in
arrears), `amounts` (principal outstanding, total overdue), `par` (`par1`, `par30` in %, and the
principal at risk behind each), `dueToday` / `overdue` (installments to collect, max 50 each, with the
full counts), and for managers `officers` (per officer: clients, active loans, outstanding, overdue,
PAR30). PAR uses the whole principal **outstanding** of late loans; see
[notes/05-portfolio-and-par.md](../notes/05-portfolio-and-par.md).

How it is fetched: 1 call for the caller's clients + 1 for all loans (each per 200 records) + 1
schedule call per **active** loan in scope, in parallel (8 at a time). About 1–1.5 s with the demo
data; no caching yet.

### Safe retries: `Idempotency-Key` (Phase 4)

`POST /api/loans`, `POST /api/loans/{id}/repayments` and `POST /api/clients` accept an optional
header `Idempotency-Key: <uuid>`. The app creates one key per user action (e.g. per repayment
form submission) and **re-sends the same key on every retry** of that action.

| Situation | Answer |
| --- | --- |
| First time | runs; response stored for 24 h |
| Same key + same body again | the stored response, header `Idempotent-Replayed: true`, **Fineract not called** |
| Same key + different body | 422 `IDEMPOTENCY_KEY_REUSED` |
| Same key while the first request still runs | 409 `IDEMPOTENCY_IN_PROGRESS` (retry shortly) |
| First attempt was refused (400/403/409/422) | key released: the retry runs normally |
| First attempt failed with Fineract unreachable/5xx | 409 `IDEMPOTENCY_OUTCOME_UNKNOWN`: check the loan, then use a NEW key |

Keys: 8–100 characters `[A-Za-z0-9_-]`, per user, stored in `idempotency_keys` (Flyway V2).

### Login limits (Phase 4)

5 failed logins within 15 minutes for the same **username** or from the same **IP** -> further
attempts get **429** `TOO_MANY_LOGIN_ATTEMPTS` with a `Retry-After` header (seconds), even with the
right password, until the oldest failure is 15 minutes old. A successful login clears that
username's count. Counts are **in memory** (reset on restart, not shared between instances).
Every login success, failure and lockout is in `audit_log` (action `LOGIN`, IP in `detail`).

### Mobile money repayments (Phase 5)

```
Officer app ─POST /api/loans/{id}/mobile-payments─▶ backend: checks, stores intent PENDING
                                                     ─POST {provider}/charges─▶ TelePay: charge PENDING
                                                                                  client approves on phone
backend ◀──POST /api/webhooks/mobile-money (HMAC-signed)──────────────────────── TelePay
  verify signature + timestamp + new eventId → inbox row + intent RECEIVED → 200 at once
  posting worker (every 5 s): receipt not yet in Fineract? → repayment "Mobile Money",
     receipt number = provider transaction id → POSTED
     Fineract down → RETRYING (10 s, 20 s, 40 s, 80 s, 2 min …) → POSTED, or NEEDS_ATTENTION after 8
nightly / on demand: reconciliation (provider settlements ↔ intents ↔ Fineract receipts)
```

| Method | Path | Who | What |
| --- | --- | --- | --- |
| POST | `/api/loans/{id}/mobile-payments` | officer (own clients) / manager (office) | `{amount, phone}`; amount ≤ outstanding − payments already in flight; `Idempotency-Key` supported; 201 with the intent |
| GET | `/api/mobile-payments/{id}` | same scope | status + full history; poll until `finalState` is true. Works while Fineract is down |
| GET | `/api/loans/{id}/mobile-payments` | same scope | all intents of a loan, newest first |
| POST | `/api/webhooks/mobile-money` | the provider (no JWT) | headers `X-TelePay-Timestamp` (unix seconds) and `X-TelePay-Signature: v1=<hex HMAC-SHA256(secret, timestamp + "." + raw body)>`; timestamp within 5 min; repeated `eventId` = no-op |
| POST | `/api/reconciliation/run?date=YYYY-MM-DD` | manager | run now (default today); answer = the run's report for your office |
| GET | `/api/reconciliation/{date}` | manager | latest completed run: counts, totals, findings of your office (+ findings with unknown office) |
| POST | `/api/reconciliation/findings/{id}/resolve` | manager (own office) | `{note}`. PAID_NOT_POSTED with a known intent is **posted** through the normal posting path; every other finding needs a note |

Payment intent states: `PENDING`, `DECLINED`, `CHARGE_FAILED` (provider unreachable when asking),
`MISMATCH` (provider reported a different amount, held, never posted automatically), `RECEIVED`,
`RETRYING`, `POSTED`, `NEEDS_ATTENTION`. Every change is stored with its time
(`payment_intent_transitions`). Webhooks that pass the signature check are kept in
`webhook_events` (the inbox); rejected ones are only in `audit_log` (action `WEBHOOK`, `DENIED`).

Reconciliation findings: `MATCHED`, `PAID_NOT_POSTED` (paid at the provider, no Fineract repayment with
that receipt), `POSTED_NOT_PAID` (Mobile Money repayment in Fineract without a provider payment),
`AMOUNT_MISMATCH`. A finding resolved once stays resolved in later runs (same type + receipt).

### TelePay simulator (local profile only)

A fake mobile money provider **inside this app** (to save RAM), at `/sim/**`. In reality it would
be a separate service on the internet; here it is still kept apart: its own in-memory state (lost on
restart), its own HTTP client and signing code, and it reaches the backend only over HTTP.

| Call | Meaning |
| --- | --- |
| `POST /sim/charges` | our backend asks for a charge (needs `Authorization: Bearer <TELEPAY_API_KEY>`) |
| `GET /sim/charges/{id}` | the charge, including every webhook delivery attempt and its HTTP answer |
| `POST /sim/charges/{id}/approve` | the customer presses "Pay": status SUCCEEDED, webhook sent |
| `POST /sim/charges/{id}/decline` | the customer presses "Decline": webhook with status DECLINED |
| `GET /sim/settlements?date=YYYY-MM-DD` | the provider's successful payments that day (needs the API key) |

**Chaos switches** (query parameters on `approve`), combine as you like:

| Flag | Effect |
| --- | --- |
| `duplicate=true` | send the same webhook (same `eventId`) twice |
| `delaySeconds=20` | wait before sending |
| `wrongSecret=true` | sign with a wrong secret (our backend answers 401) |
| `drop=true` | never send the webhook (reconciliation finds it as PAID_NOT_POSTED) |
| `reportAmount=1000` | report (and settle) a different amount than requested |
| `staleSeconds=600` | sign with a timestamp 10 minutes in the past (401) |

Example (PowerShell): `curl.exe -X POST "http://localhost:8080/sim/charges/ch_abc/approve?drop=true"`

### OpenAPI for the Flutter client

`backend/openapi.json` is the full OpenAPI 3.1 spec. `OpenApiSpecTest` re-exports it on every
`mvnw test`, so commit it whenever it changes. Generate the Dart client from it, e.g.
`openapi-generator-cli generate -i backend/openapi.json -g dart-dio -o app/lib/api`.

Conventions:
- Money is a JSON number with 2 decimals (`1833.60`), ETB. Rates are percent per year (`18.00`).
- Dates are ISO `yyyy-MM-dd`.
- Paging is 0-based: `{content, page, size, totalElements, totalPages}`; `size` max 100.
- `search` matches part of the client's display name and is **case-sensitive**
  (Fineract runs SQL `LIKE` on PostgreSQL): `Tek` finds "Yohannes Tekle", `tek` does not.
  Fineract's `/search` endpoint was checked too (2026-10-23): also case-sensitive, and it has
  no paging or office/staff filters, so we keep `displayName`.

Schedule preview example:

```bash
curl -X POST http://localhost:8080/api/loans/schedule-preview \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"productId":1,"principal":20000,"numberOfRepayments":12}'
# -> installmentAmount 1833.60, lastInstallmentAmount 1833.61, totalInterest 2003.21
```

`expectedDisbursementDate` (`yyyy-MM-dd`) is optional and defaults to today (Addis Ababa time).

### Errors

Errors are [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) problem details:

```json
{"type":"urn:adey:problem:fineract","title":"Invalid request","status":400,
 "detail":"The parameter `principal` amount 90000.0 must be between 5000.00 and 50000.00 .",
 "instance":"/api/loans/schedule-preview",
 "errors":[{"field":"principal","message":"The parameter `principal` amount 90000.0 must be between 5000.00 and 50000.00 ."}]}
```

**Every error has a stable `code`.** The app should switch on `code`; `detail` is for people and
may change. Complete list:

| HTTP | `code` | Meaning |
| --- | --- | --- |
| 400 | `VALIDATION_FAILED` | a body field or query parameter is invalid (`errors[]` lists them) |
| 400 | `MALFORMED_REQUEST` | the body is not valid JSON / wrong types |
| 400 | `INVALID_PARAMETER` | a query/path/header parameter has the wrong type or is missing (e.g. `status=FOO`) |
| 400 | `INVALID_REQUEST` | one of our rules on the input (e.g. client under 18, unusable payment type) |
| 400 | `FINERACT_VALIDATION` | Fineract rejected a field (e.g. principal outside the product range) |
| 400 | `IDEMPOTENCY_KEY_INVALID` | `Idempotency-Key` has the wrong format |
| 400 | `RESOLUTION_NOTE_REQUIRED` | resolving this reconciliation finding needs a `note` |
| 401 | `UNAUTHENTICATED` | no, invalid or expired access token: refresh or log in |
| 401 | `INVALID_CREDENTIALS` | wrong username or password |
| 401 | `INVALID_REFRESH_TOKEN` | refresh token expired, revoked or already used: log in again |
| 401 | `WEBHOOK_SIGNATURE_INVALID` | (provider only) webhook signature missing or wrong |
| 401 | `WEBHOOK_TIMESTAMP_INVALID` | (provider only) webhook timestamp more than 5 minutes off |
| 403 | `FORBIDDEN` | not your client / office, manager-only action, or not an officer account |
| 404 | `NOT_FOUND` | no such client/loan/URL |
| 405 | `METHOD_NOT_ALLOWED` | wrong HTTP method for this URL |
| 406 | `NOT_ACCEPTABLE` | unsupported `Accept` header |
| 409 | `OPEN_LOAN_EXISTS` | our rule: the client already has a pending / approved / active loan |
| 409 | `DUPLICATE_CLIENT` | mobile number or national ID already used by another client |
| 409 | `IDEMPOTENCY_IN_PROGRESS` | the first request with this key is still running |
| 409 | `IDEMPOTENCY_OUTCOME_UNKNOWN` | the first request with this key failed midway; check, then use a new key |
| 409 | `CONFLICT` | other conflict reported by Fineract |
| 409 | `FINDING_ALREADY_RESOLVED` | this reconciliation finding was already resolved |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | send `Content-Type: application/json` |
| 422 | `LOAN_WRONG_STATE` | action not allowed in the loan's current state (e.g. disburse a pending loan) |
| 422 | `FINERACT_RULE` | other Fineract business rule |
| 422 | `IDEMPOTENCY_KEY_REUSED` | this key was used for a different request |
| 422 | `AMOUNT_EXCEEDS_OUTSTANDING` | mobile payment larger than what is still owed (minus payments in flight) |
| 422 | `LOAN_NOT_ACTIVE` | mobile payments only on active (disbursed) loans |
| 429 | `TOO_MANY_LOGIN_ATTEMPTS` | login locked; wait `Retry-After` seconds |
| 500 | `INTERNAL_ERROR` | bug on our side (details only in the server log) |
| 502 | `FINERACT_ERROR` | Fineract failed or rejected our service credentials |
| 503 | `FINERACT_UNAVAILABLE` | Fineract unreachable or timed out |
| 503 | `PROVIDER_UNAVAILABLE` | the mobile money provider could not be reached (intent marked CHARGE_FAILED) |
| 503 | `POSTING_PENDING` | reconciliation accepted the payment but Fineract posting failed; the worker retries |

## Code layout

```
et.adey.mfi
├── config/    FineractProperties, RestClient bean (auth + tenant header, local SSL trust), CORS, Clock
├── security/  SecurityConfig (filter chain), JwtConfig (sign/verify), TokenService, Officer,
│              OfficerJwtConverter (token -> Officer), AccessPolicy (who may see/do what)
├── auth/      login / refresh / logout / me, refresh tokens (hashed, rotated) in our DB
├── audit/     audit_log entity + AuditService (one row per state-changing attempt)
├── seed/      demo data loader (profile "seed")
├── portfolio/ GET /api/loans + /api/me/portfolio: PortfolioReader (fetch) + PortfolioCalculator (PAR etc.)
├── idempotency/ Idempotency-Key support (idempotency_keys table)
├── mobilemoney/ intents + state machine, TelePayClient, webhook (signature, inbox), posting worker
├── reconciliation/ matcher (pure), Fineract reader, runs/findings, resolve
├── simulator/ TelePay simulator, local profile only (/sim/**)
├── fineract/  FineractClient: the ONLY class that calls Fineract; raw Fineract records in model/;
│              FineractErrorCodes (wrong-state codes -> 422)
├── common/    FineractDates, Money, Rates, PageResponse, GlobalExceptionHandler
├── office/    controller -> service -> DTO
├── product/
├── paymenttype/
├── client/
└── loan/      LoanService (reads, preview), LoanLifecycleService (submit -> repay, our rules),
               LoanMapper (pure Fineract -> DTO mapping, unit-tested against real responses)
```
