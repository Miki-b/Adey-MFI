# 02 — Backend, Phase 1 (read-only API in front of Fineract)

Date: 2026-10-23

## What we built

A Spring Boot 3.5.16 / Java 21 service in `backend/` that reads from the local Fineract
and returns clean JSON:

- `GET /api/offices`, `GET /api/loan-products`
- `GET /api/clients` (search + office/staff filter + paging), `GET /api/clients/{id}`
- `GET /api/clients/{id}/loans` (status, installments paid/total, outstanding, next due, arrears)
- `POST /api/loans/schedule-preview` (asks Fineract to calculate a schedule without saving)

Verified live: the preview for 20,000 ETB / 12 months / 18% gives installment **1833.60**,
last **1833.61**, total interest **2003.21**, the same as `calc/loan-schedule.xlsx`.

## How the layers fit together

```
HTTP request
  -> Controller   (office/, client/, loan/, product/)  URL + validation only
  -> Service      business logic, maps Fineract shapes -> our DTO records
  -> FineractClient (fineract/)  the ONLY class that calls Fineract
  -> RestClient bean (config/)   base URL, Basic auth, tenant header, timeouts, local TLS trust
  -> Fineract
```

Errors flow back the other way: FineractClient turns HTTP errors into `FineractApiException`
(with Fineract's `defaultUserMessage` + `errors[]`) and network failures into
`FineractUnavailableException`. `GlobalExceptionHandler` turns both into RFC 7807
ProblemDetail JSON with a sensible status (400/404/422/502/503).

Key ideas:
- **Two sets of records.** `fineract/model/*` mirror Fineract's JSON, holding only the fields we read;
  unknown fields are ignored. The feature packages have our own small DTOs. The app never
  sees Fineract's shape, so Fineract can change without breaking the app.
- **`@ConfigurationProperties` record** (`FineractProperties`): typed, validated config
  instead of `@Value` strings scattered around.
- **Profiles**: `spring.profiles.default: local` means `local` is used only when nothing else
  is chosen. Self-signed-cert trust and open CORS live only in `application-local.yml`.
- **Money**: `BigDecimal` everywhere, normalised to 2 decimals (`Money.of`), so `1833.6`
  from Fineract becomes `1833.60` in our API.
- **Dates**: `FineractDates` converts Fineract's `[2026,10,3]` arrays to `LocalDate`, and formats
  `LocalDate` as `"03 October 2026"` + `dateFormat` + `locale` for requests.
- **Schedule preview copies the product's terms** (frequency, rate, amortization, interest
  type and calculation period, strategy) into the `calculateLoanSchedule` request, so the
  preview is exactly what a real application for that product would get.

## Tests (18, no Fineract needed)

- `FineractDatesTest`: array to date, date to "dd MMMM yyyy".
- `FineractClientTest`: MockRestServiceServer. Checks tenant + Basic auth headers, query
  params, Fineract error to `FineractApiException`, connection refused to `FineractUnavailableException`.
- `LoanMapperTest`: a **real saved** `calculateLoanSchedule` response maps to 1833.60 / 1833.61 /
  2003.21; plus a hand-made active-loan sample for installments paid / next due / arrears.
- `LoanControllerTest`: `@WebMvcTest` for the HTTP status codes and ProblemDetail bodies.
- `MfiBackendApplicationTests`: the Spring context starts.

## Problems hit and what we learned

1. **start.spring.io no longer offers Spring Boot 3.** It only lists 4.x now. We generated the
   project there (for the Maven wrapper), then wrote the pom for **3.5.16**, the latest 3.x on
   Maven Central. Boot 4 also renamed starters (`spring-boot-starter-webmvc`, separate `*-test`
   starters), so the generated pom couldn't simply be downgraded.
2. **Port 5432 already taken** by another project's Postgres container (`dct_postgres`).
   Fineract reaches its DB over the Docker network (`db:5432`), so the host port is optional.
   We started the stack with a throw-away compose override that removes the `db` port mapping
   (`ports: !reset []`) instead of stopping the other container or editing `docker-compose.yml`.
   Possible permanent fix: map our db to e.g. `"5433:5432"`.
3. **Client search is case-sensitive.** Fineract's `displayName` filter is a substring `LIKE` on
   PostgreSQL: `Yoh`/`Tek` match, `yoh` does not. `/search?query=` returned nothing. We kept
   `displayName` because it combines with `officeId`/`staffId` and paging.
4. **Swagger lies about dates.** Fineract's OpenAPI says `dueDate` is a string; real responses
   send `[yyyy, m, d]` arrays. Always check a real response.
5. **Fineract already validates product ranges** (principal 5,000–50,000 etc.) and names the field,
   so we don't duplicate those rules; the handler passes the message through as a 400.
6. **Rates come with 6 decimals** (`18.000000`); `Rates.percent` returns `18.00`.
7. Staff names have stray spaces (`"Derese, Samuel "`), so we trim them.
8. The interest doesn't change with the disbursement date: with "same as repayment period"
   interest, every month is exactly 1.5% of the balance, whatever its number of days.
9. Stopping `mvnw spring-boot:run` from a tool can leave the forked app JVM running. Check port
   8080 / kill the `java` process if the next start says the port is in use.

## Data facts discovered

- Offices are **Head Office (1)** and **Bishoftu Branch (2)**. CLAUDE.md said Adama.
- Staff 1: Samuel Derese, loan officer, Bishoftu Branch.
- Client 1: Yohannes Tekle (000000001), Bishoftu, staff 1, **no loans yet**, so
  `/api/clients/1/loans` currently returns `[]`. The loan-list mapping is tested with a hand-made
  sample until Phase 2 creates a real loan.
