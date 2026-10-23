# 04 — Demo data, fixes, our own database, officer login (backend Phase 3)

Date: 2026-10-23

## Part 1 — Demo seed

**Choice: a Spring profile (`seed`), not a script.** A shell script would have to rebuild the tenant
header, TLS trust, date formats and error parsing with curl and a JSON tool on Windows, untyped and
untested. The profile reuses `FineractClient` and our records. It starts no web server, runs
`DemoSeeder` and exits:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local,seed"
```

**Idempotent** = running it twice gives the same result as once. Every record has a stable key
(office/role by name, user by username, staff/client/loan by `externalId` such as `SEED-C07`). Each
step first looks the record up and only creates what is missing. Loans are only moved *forward*
toward their planned state (submit → approve/reject → disburse → planned repayments; already
posted repayments are counted). The second run reported `0 records created or changed`.

**Business date (and why we didn't need it).** Fineract normally uses the real calendar date as
"today". With *business date* enabled (global config `enable-business-date`, off here) you can set
Fineract's own "today" (`/businessdate`) and move it forward day by day, while close-of-business
(COB) jobs age loans, charge penalties and mark arrears. It's how you simulate months passing.
We didn't need it: Fineract accepts **back-dated** transactions. Adama Branch "opened" on 2025-01-01,
so its loans could be disbursed months ago and repaid (or not) on past dates. Seen from today, those
loans are naturally on time, partly paid or in arrears. Bishoftu Branch opened on 2026-10-03,
so its loans are recent.

**Arrears surprise.** This Fineract build reports `inArrears` at the **top level** of the loan; the
OpenAPI schema puts it in `summary`, where it is always null. Our API had shown every loan as not in
arrears. Fixed (`FineractLoan.isInArrears()`), and loan details now also show `overdueSince`.

**Reject and undo-approval live through our endpoints**, during seeding:
- undo-approval on Kidist's approved loan #3, which went back to pending, then was approved again on its
  original date (seed state unchanged);
- a new application for Fikirte (#12), rejected via `POST /api/loans/12/reject`.

## Part 2 — Fixes

**Wrong-state errors by code.** Fineract answers "disburse a pending loan", "approve an active
loan" etc. with **HTTP 400** (the same status as a missing field). The real reason is in
`errors[].userMessageGlobalisationCode`. `FineractErrorCodes` lists the five codes we observed and
maps them to **422** with a clear message and `code: LOAN_WRONG_STATE`:

| Action in wrong state | Fineract code |
| --- | --- |
| disburse (not approved) | `error.msg.loan.disbursal.account.is.not.approve.not.disbursed.state` |
| approve (not pending) | `error.msg.loan.approve.account.is.not.submitted.and.pending.state` |
| undo approval (not approved) | `error.msg.loan.undo.approval.account.is.not.approved` |
| reject (not pending) | `error.msg.loan.reject.account.is.not.submitted.pending.approval.state` |
| repayment (not active) | `error.msg.loan.must.be.active.fully.paid.or.overpaid` |

An explicit list, not a guess from the code text: unknown codes keep their normal mapping. The new tests
caught a bug: `Map.of(...).containsKey(null)` throws, and some Fineract errors have no code.

**Cash.** The seed added payment type *Cash* (id 4). Disburse and repay now check `paymentTypeId`
against `GET /api/payment-types`: Cash and Money Transfer pass. Fineract's internal adjustment types
(ids 2, 3) and unknown ids get a 400 before Fineract is called.

## Part 3 — Our own database

Same Postgres container, separate database **`adey_backend`** owned by a separate login **`adey`**.
`adey` cannot read Fineract's tables (`permission denied for table m_client`), so a bug in our backend
cannot touch Fineract's data. Fresh volumes get it from `infra/init-db.sql`; existing ones need the
one-time command in `backend/README.md`.

- **JPA** (Hibernate) maps Java classes (`@Entity`) to tables; Spring Data writes the queries from
  repository interfaces.
- **Flyway** owns the schema. `V1__refresh_tokens_and_audit_log.sql` ran once and is recorded in
  `flyway_schema_history`. Changes go in new files (V2, V3, ...), never by editing V1.
  Hibernate only *validates* that the tables match (`ddl-auto: validate`).
- Tests use **H2** in PostgreSQL mode with the same migrations, so they need no Docker.

## Part 4 — How officer login works

### The pieces, in plain words

- **Password check: Fineract.** We don't keep a second user database. `POST /api/auth/login`
  forwards username + password to Fineract `POST /authentication`. We never store the password.
- **Access token: a JWT, 15 minutes.** A JWT is three base64 parts: `header.payload.signature`.
  The payload holds *claims* (who you are and when the token expires). Anyone can read the payload
  (paste one into a base64 decoder), so it holds nothing secret. Nobody can *change* it: the
  signature is an HMAC-SHA256 of header + payload made with our secret key (`JWT_SECRET`), and
  changing one character breaks it.
- **Refresh token: random, 7 days, stored hashed.** When the access token expires, the app swaps the
  refresh token for a new pair without asking for the password again. It is not a JWT, just 256
  random bits. We store only its SHA-256 hash, so a database leak gives no usable tokens. That is
  the same idea as storing password hashes.
- **Spring Security filter chain.** Every request passes through it *before* any controller.
  The bearer-token filter reads `Authorization: Bearer ...` and the `JwtDecoder` checks signature,
  issuer and expiry. If valid, `OfficerJwtConverter` turns the claims into our `Officer`, and
  controllers receive it as `@AuthenticationPrincipal Officer officer`.

### Login, a call, refresh, logout

```
 Flutter app                         Adey backend                                  Fineract
 ───────────                         ────────────                                  ────────
 1. LOGIN
 POST /api/auth/login  ───────────▶  AuthService.login
   {username, password}                 │  POST /authentication {username, password} ──▶ checks password
                                        │  ◀── userId, staffId, officeId, roles ────────
                                        │  must have staff + "Loan Officer"/"Branch Manager"
                                        │  sign JWT (15 min) with JWT_SECRET
                                        │  new refresh token -> store SHA-256 in refresh_tokens
 ◀── {accessToken, refreshToken, officer}

 2. EVERY CALL
 GET /api/clients ────────────────▶  Security filter chain
   Authorization: Bearer eyJ...         │  signature ok? issuer ok? not expired?   no ──▶ 401
                                        │  claims -> Officer(staffId 1, office 2, OFFICER)
                                        ▼
                                     ClientController -> ClientService
                                        │  AccessPolicy: officer -> staffId=1, officeId=2 (from token)
                                        │  GET /clients?officeId=2&staffId=1 ───────────▶
 ◀── only Samuel's clients ────────────────────────────────────────────────────────────

 3. AFTER 15 MIN: REFRESH
 POST /api/auth/refresh ──────────▶  RefreshTokenService.consume
   {refreshToken}                       │  hash -> row found, not revoked, not expired?    no ──▶ 401
                                        │  revoke it (single use)
                                        │  GET /users/{id} (current roles/office) ──────▶
                                        │  new JWT + new refresh token (same "family")
 ◀── {accessToken, refreshToken} (new pair)

    Someone replays an OLD refresh token? ─▶ it is already revoked = stolen copy
                                        ─▶ revoke the whole family (thief AND officer must log in again)

 4. LOGOUT
 POST /api/auth/logout {refreshToken} ─▶ revoke the family. The access token still works until it
                                         expires (at most 15 min). That is the price of not looking
                                         every request up in a database.
```

### Who may do what (authorization)

| Rule | Where |
| --- | --- |
| Every `/api/**` needs a valid token (except login/refresh/logout) | `SecurityConfig` |
| Swagger/health without a token: **local profile only** | `SecurityConfig` + `app.security.public-docs` |
| Officer: only clients with `staffId` = their staffId (and those clients' loans) | `AccessPolicy`, used via `LoanGuard` |
| Manager: every client of their office | `AccessPolicy` |
| `staffId` / `officeId` come from the token; the query parameters were removed | `ClientController`, `AccessPolicy.clientScope` |
| Approve / reject / undo approval / disburse: MANAGER only | `AccessPolicy.requireManager` in `LoanLifecycleService` |

Why the role check is in the service and not `@PreAuthorize` or a URL rule: those stop the request
*before* our code runs, so a refused attempt would never reach the audit log. In the service, it runs
*inside* `audit.record(...)`.

**403 vs 404.** Another officer's client gives 403 "not assigned to you" (as specified). It does
reveal that the id exists; a stricter system would answer 404.

**CSRF is off** on purpose: CSRF attacks ride on cookies the browser sends automatically. We use no
cookies or sessions (`STATELESS`), so there is nothing to ride on.

**CORS** is now a list (`app.cors.allowed-origins`), local: `http://localhost:[*]`. Browsers only;
the mobile app ignores CORS.

### Audit log

Every apply / approve / undo approval / reject / disburse / repayment *attempt* writes one row:
who (username, user, staff, office), what (action, client, loan, amount), when, and the outcome:
`SUCCESS`, `DENIED` (403), `REJECTED` (business rule or Fineract said no) or `ERROR` (technical).
The loan's client is recorded *before* the access check, so a refused attempt on someone else's
loan still shows whose client it was.

### Choices worth knowing

- **HMAC (one shared secret) instead of RSA key pairs.** Only this backend issues and checks tokens,
  so one secret is enough. If another service ever needs to verify our tokens without being able to
  issue them, switch to RSA/EC (private key signs, public key verifies).
- **Our backend still talks to Fineract as the service account `mifos`.** Fineract's own per-user
  permissions are therefore *not* what protects the API; our `AccessPolicy` is. The Fineract roles
  decide who you are at login, and still protect the Mifos web UI if those users log in there.
- **JWT_SECRET**: required outside the local profile (startup fails without 32+ characters). The
  local default is marked local-only in `application-local.yml`.

## Problems hit

1. **Drive C: was full (6 MB free).** Postgres can't write, builds fail, data can get damaged. We
   stopped, the owner freed space (9.6 GB), we checked the Postgres/Fineract logs for disk-full errors
   (none) before continuing.
2. **Fineract's password policy is "strong"**: 12+ characters, mixed case, digit, symbol, *no repeated
   consecutive characters*, so "Officer..." fails because of the "ff".
3. **`inArrears` at the loan's top level** (see Part 1).
4. **`@WebMvcTest` no longer fit.** The web-slice test doesn't load our `SecurityConfig` and would test
   Boot's default security (login form, CSRF) instead. The controller tests became
   `@SpringBootTest` with the real filter chain.
5. **`@Value` can't read a YAML list.** `@Value("${app.cors.allowed-origins}")` came back empty
   because YAML stores lists as `allowed-origins[0]`, `[1]`: every CORS request was refused.
   `@ConfigurationProperties` binds lists properly; a test now guards it.
6. **The seed profile has no web server**, so `SecurityConfig` is `@ConditionalOnWebApplication`.
7. **Fineract login returns `base64EncodedAuthenticationKey`** (= base64 of `username:password`). Our
   `FineractAuthentication` record has no field for it, so it is dropped while parsing.

## Known limits (fine for now)

- An access token stays valid up to 15 minutes after logout or after a role change in Fineract.
- If writing an audit row fails, the action's result is still returned and an `AUDIT WRITE FAILED`
  error is logged (fail-open). A stricter system would refuse the action when the audit DB is down.
- Two simultaneous applications for the same client could both pass the one-open-loan check.
- Login attempts are not rate-limited and not audited yet.
