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

- Swagger UI: http://localhost:8080/swagger-ui.html. Call `POST /api/auth/login`, copy
  `accessToken`, click **Authorize**, paste it.
- Health: http://localhost:8080/actuator/health

Swagger and health need no token in the local profile only.

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

| Situation | Status |
| --- | --- |
| Our validation fails, or Fineract returns 400 | 400 (with `errors[]`) |
| No / invalid / expired access token; wrong password; bad refresh token | 401 (`code`: `UNAUTHENTICATED`, `INVALID_CREDENTIALS`, `INVALID_REFRESH_TOKEN`) |
| Not your client / not your office; manager-only action; account not an officer | 403 (`code`: `FORBIDDEN`) |
| Not found in Fineract | 404 |
| Our business rule (e.g. open loan exists) | 409 (`code`: `OPEN_LOAN_EXISTS`) |
| Action not allowed in the loan's current state (e.g. disburse a pending loan) | 422 (`code`: `LOAN_WRONG_STATE`). Fineract sends these as 400; we recognise them by error code (`FineractErrorCodes`) |
| Other Fineract business rule (Fineract 403) | 422 |
| Fineract rejects our credentials (401) or fails (5xx) | 502 |
| Fineract unreachable or timed out | 503 |

## Code layout

```
et.adey.mfi
├── config/    FineractProperties, RestClient bean (auth + tenant header, local SSL trust), CORS, Clock
├── security/  SecurityConfig (filter chain), JwtConfig (sign/verify), TokenService, Officer,
│              OfficerJwtConverter (token -> Officer), AccessPolicy (who may see/do what)
├── auth/      login / refresh / logout / me, refresh tokens (hashed, rotated) in our DB
├── audit/     audit_log entity + AuditService (one row per state-changing attempt)
├── seed/      demo data loader (profile "seed")
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
