# Adey MFI backend

Spring Boot 4.1 API (Java 21, Jackson 3) that sits between the Flutter app and Apache Fineract.
The app never talks to Fineract directly: this service holds the Fineract credentials,
turns Fineract's verbose JSON into small clean DTOs, and is where our own business rules live.

```
Flutter app  ->  this backend (:8080)  ->  Fineract (https://localhost:8443)
```

## Run it

Prerequisites: JDK 21, Fineract running (see `../infra`, check
`curl -k https://localhost:8443/fineract-provider/actuator/health` says `UP`).

```powershell
cd C:\dev\MFI\adey-mfi\backend
.\mvnw.cmd spring-boot:run        # starts on http://localhost:8080 with -Xmx512m
.\mvnw.cmd test                   # unit + web-layer tests, no Fineract needed
```

The `-Xmx512m` heap cap is set in `pom.xml` (`app.jvm.args`) and applies to both
`spring-boot:run` and the tests. If you run the jar yourself:
`java -Xmx512m -jar target/mfi-backend-0.0.1-SNAPSHOT.jar`.

- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

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

### Profiles

- **local** (default when no profile is set), in `application-local.yml`:
  - trusts Fineract's self-signed certificate (`fineract.trust-self-signed-certificate: true`)
  - allows CORS from any origin (for Flutter web/dev)

  **Never run production with the local profile.** Trusting any certificate lets anyone
  intercept the Fineract credentials and customer data.
- Any other profile (e.g. `SPRING_PROFILES_ACTIVE=prod`) has normal certificate checks and no CORS.

## Endpoints

Reads (Phase 1):

| Method | Path | What |
| --- | --- | --- |
| GET | `/api/offices` | id, name, parentId, openingDate |
| GET | `/api/loan-products` | ranges for principal / repayments, rate, interest method, amortization |
| GET | `/api/payment-types` | payment types an officer can pick (Fineract's system-defined ones hidden) |
| GET | `/api/clients?search=&officeId=&staffId=&page=0&size=20` | paged client list |
| GET | `/api/clients/{id}` | client details |
| GET | `/api/clients/{id}/loans` | per loan: status, progress, outstanding, next due, arrears |
| GET | `/api/loans/{id}` | totals, key dates, full schedule (with paid/outstanding per installment), transactions |
| POST | `/api/loans/schedule-preview` | repayment schedule without creating a loan |

Loan lifecycle (Phase 2):

| Method | Path | Body (all dates optional, default today) | Result |
| --- | --- | --- | --- |
| POST | `/api/loans` | `clientId, productId, principal, numberOfRepayments, expectedDisbursementDate?, staffId?` | 201 + `Location`, `{loanId, accountNo, status, statusLabel}` |
| POST | `/api/loans/{id}/approve` | `approvedOnDate?, approvedAmount?, note?` (body optional) | `{loanId, status: APPROVED, ...}` |
| POST | `/api/loans/{id}/undo-approval` | `note?` (body optional) | back to `SUBMITTED_AND_PENDING_APPROVAL` |
| POST | `/api/loans/{id}/reject` | `rejectedOnDate?, note?` (body optional) | `REJECTED` |
| POST | `/api/loans/{id}/disburse` | `actualDisbursementDate?, paymentTypeId?, note?` (body optional) | `ACTIVE` |
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
| Our validation fails, or Fineract returns 400 | 400 (with `errors[]`). Note: Fineract reports some wrong-state actions (e.g. disbursing an unapproved loan) as 400, not 403 |
| Not found in Fineract | 404 |
| Our business rule (e.g. open loan exists) | 409 (with `code`) |
| Fineract business rule (Fineract 403) | 422 |
| Fineract rejects our credentials (401) or fails (5xx) | 502 |
| Fineract unreachable or timed out | 503 |

## Code layout

```
et.adey.mfi
├── config/    FineractProperties, RestClient bean (auth + tenant header, local SSL trust), local CORS
├── fineract/  FineractClient: the ONLY class that calls Fineract; raw Fineract records in model/
├── common/    FineractDates, Money, Rates, PageResponse, GlobalExceptionHandler
├── office/    controller -> service -> DTO
├── product/
├── paymenttype/
├── client/
└── loan/      LoanService (reads, preview), LoanLifecycleService (submit -> repay, our rules),
               LoanMapper (pure Fineract -> DTO mapping, unit-tested against real responses)
```
