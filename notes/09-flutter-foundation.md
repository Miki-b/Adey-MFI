# 09 — Flutter foundation: Adey Field, Session A

Date: 2026-10-26

The app for field staff, in `app/`. Session A builds the foundation: project, generated API client,
auth, dashboard and clients. This note covers what is specific to *this* app's design, not Flutter basics.

## Stack and why

| Piece | Choice | Why here |
| --- | --- | --- |
| SDK | Flutter 3.41.6 via FVM (`.fvmrc`) | reproducible SDK per project; 3.41.6 was already cached (C: drive is nearly full) |
| State | Riverpod 3 + codegen (`@riverpod`) | providers as plain functions/classes; `AsyncValue` gives loading/error/data for free |
| Routing | go_router 17, `StatefulShellRoute` | bottom tabs that keep their own stacks; one `redirect` for the auth guard |
| HTTP | Dio 5 | interceptors (auth, refresh) and the generated client is Dio-based |
| Secrets | flutter_secure_storage | tokens in Android Keystore / iOS Keychain, never in shared_preferences |
| Money | `decimal` | never do money arithmetic in `double` |
| Lints | flutter_lints + a few strict rules | very_good_analysis also demands doc comments on every public member: noise in an app |

One version note: Flutter 3.41 ships Dart 3.11, and the newest `riverpod_generator` that runs on it
needs `riverpod` ≤ 3.2.1, so `flutter_riverpod` is pinned to `>=3.2.1 <3.3.0`. Moving to Flutter 3.47
lifts that.

## Generated client, wrapped

`packages/adey_api` is generated from `backend/openapi.json` (openapi-generator 7.26.0, `dart-dio` +
`json_serializable`) by `app/tool/generate_api.ps1`, and never edited by hand. The script:

- downloads the generator jar once (checked against Maven Central's SHA-1);
- generates with `enumUnknownDefaultCase=true`, so a status the backend adds later decodes as
  `unknownDefaultOpenApi` instead of failing the whole response;
- raises the package SDK floor to `^3.8.0` (the newest json_serializable emits Dart 3.8 syntax);
- runs `build_runner`.

What the generated code is like, and why the UI never touches it:

- **Every field is nullable**, because springdoc doesn't mark anything `required`.
- **Method names come from Java method names**, because the backend sets no `operationId`: `callGet`, `get1`, `get2`, `get3`.
- **Money is `num`.**

So each feature has a **repository** (`clients_repository.dart`, `portfolio_repository.dart`) that calls
the generated API and returns app models: non-null with sensible defaults, `Decimal` money, our own
enums (`LoanState`), and every failure turned into one `ApiError` type.

## Money: JSON number → Decimal

The backend sends `1833.60` as a JSON number, and Dart's JSON decoder makes it the `double` 1833.6.
A double's *shortest* string form (`1833.6`) is exactly the decimal the backend wrote (true for
values with up to 15 significant digits, far beyond any loan). So repositories call
`toDecimal(num)` = `Decimal.parse(n.toString())` at once. From there everything is `Decimal`, and
formatting (`formatEtb` → "ETB 1,833.60") works from the decimal string, never via `double`. Today the
app never adds money; if a screen ever must (e.g. a cart of installments), it adds Decimals. In double,
`0.1 + 0.2 != 0.3`, and there's a test showing it.

## Auth flow

```
app start ──▶ /splash ──refresh token stored?──no──▶ /login
                 │ yes
                 ▼
            GET /api/auth/me (interceptor refreshes if the access token expired)
              ok ─▶ /dashboard      refused ─▶ clear tokens, /login      offline ─▶ splash: "Try again"

/login ──POST /api/auth/login──▶ save tokens (secure storage) ──▶ GET /api/auth/me ──▶ Officer in AuthController
```

- `AuthController` (`AsyncNotifier<Officer?>`) is the single source of truth: `AsyncData(null)` =
  signed out, `AsyncData(officer)` = signed in, loading = restoring, error = couldn't check.
- The router has ONE `redirect` (`authRedirect`, a pure function with its own tests) and re-runs it
  when the auth state changes (`refreshListenable`). Manager-only routes (`/approvals`,
  `/more/reconciliation`) bounce officers to the dashboard. That is UI only: the backend refuses those calls anyway.
- Login errors: `INVALID_CREDENTIALS`; `TOO_MANY_LOGIN_ATTEMPTS` with the wait from `Retry-After`,
  rounded **up** to minutes; and `FORBIDDEN` on login, which means "not a loan officer / branch manager account".
- Logout: `POST /api/auth/logout` (revokes the refresh token family on the server), then clear storage,
  even when offline.

### The refresh interceptor

Access tokens live 15 minutes, and the dashboard fires several calls at once, so when a token expires
they all get `401 UNAUTHENTICATED` together. The backend's refresh tokens are **single-use and
rotated**, and reusing one is treated as theft: the whole token family is revoked (Phase 3). So
"every request refreshes for itself" would log the user out. `AuthInterceptor`:

1. The first 401 starts **one** refresh, a shared `Future`; concurrent 401s await the same one.
2. A request that left with the old token and comes back 401 *after* someone else refreshed just
   retries with the new token. If its token no longer matches the stored one (new token, or none
   because the session ended), it follows the stored state and does not refresh again.
3. Each request is retried at most once; refresh and retry use a "bare" Dio without the interceptor.
4. If the refresh is refused (`INVALID_REFRESH_TOKEN`), tokens are cleared and `onSessionExpired` sends the user to login
   with "your session has ended". If the refresh fails for **network** reasons, the user stays signed in
   and sees a network error.
5. Only the backend's own `UNAUTHENTICATED` triggers a refresh. GitHub's tunnel also answers 401 for a
   private Codespace port; refreshing wouldn't help, so that becomes "the Codespace port is private".

The tests drive all of this against a fake backend adapter. Four concurrent requests produce exactly
one refresh, and the rotated refresh token is stored.

## Errors: one mapper

Every failure becomes an `ApiError`: kind (backend / network / serverPrivate / unexpectedResponse /
unknown), plus the backend's stable `code`, `detail`, `errors[]` and `Retry-After`.
`errorMessage(l10n, error)` is the **only** place that turns it into text: a `switch` on `code` →
localized message, with the backend's `detail` as fallback, then a generic text. Screens never switch on
codes themselves (login only adds its special meaning of `FORBIDDEN`). Dio doesn't follow redirects
(a private Codespace port redirects to GitHub's sign-in page), and the success range is 2xx only.

## Idempotency keys (used from Session B)

`IdempotentAction` holds one key per user action (one repayment form). `run(fingerprint, send)`:

- **same fingerprint (same request) → same key:** a retry after a lost answer is recognised by the backend and not booked twice;
- **success → reset:** the next submission is a new action;
- **network / 5xx / `IDEMPOTENCY_IN_PROGRESS` → keep the key** for the retry;
- **`IDEMPOTENCY_OUTCOME_UNKNOWN` / `IDEMPOTENCY_KEY_REUSED` → forget it:** the backend won't take that key again;
- **edited request (different fingerprint) → new key:** it is a different action.

The key is a UUID v4, which matches the backend's `[A-Za-z0-9_-]{8,100}`.

## Smaller decisions

- **Server URL** is a runtime setting stored in `shared_preferences` (not secret), with presets for the
  Codespace, the emulator (`10.0.2.2`) and the PC on Wi-Fi. `http://` is accepted only in debug builds: the app
  checks it, and Android's release network policy blocks cleartext (`src/debug` overrides it for debug).
  Changing the server signs out, because tokens belong to one backend.
- **Riverpod 3 retries failed providers automatically.** That is disabled (`ProviderScope(retry: ...)`),
  so failures show "Try again" instead of silently resending.
- **Dates:** only `AppDates.format` turns dates into text ("23 Oct 2026", `en_US` patterns, date part
  only, so no time-zone shift). Session D adds the Ethiopian calendar there.
- **All strings are in `lib/l10n/app_en.arb`** (plurals: "1 day late" / "117 days late"). Amharic is a second ARB file in Session D.
- **Client search is case-sensitive** (Fineract SQL `LIKE`); the search field says so.
- **PAR30 colours:** green < 5 %, amber 5–10 %, red > 10 %, with a "What is PAR30?" sheet.

## Live check (2026-10-26, Samsung Galaxy F22 over USB, backend in the Codespace)

Route: phone `127.0.0.1:8080` → `adb reverse` → PC → `gh codespace ports forward` → Codespace, so port
8080 stayed private. (The public URL was unreliable that day: GitHub's relay answered 404 although the
backend was up.)

| Check | Result |
| --- | --- |
| samuel (officer, Bishoftu) | dashboard: 0 active, 1 pending, PAR30 0.00 % (green); 2 clients; 4 tabs (no Approvals), no Reconciliation in More |
| selamawit (manager, Adama) | PAR30 **64.76 %** on ETB 36,745.16 (red), 4 active, 1 pending, 2 in arrears, principal ETB 56,736.16, overdue ETB 13,302.08, 7 overdue installments most-late first, by officer Dawit 60.32 % / Meron 67.16 %; 7 clients; 5 tabs |
| Same numbers from `GET /api/me/portfolio` (curl, as selamawit) | identical |
| Client details | profile + loans: pending chip, "In arrears" badge, progress "2 of 12", next due |
| Settings | presets, "Server is reachable", saving the server signs out |
| Sign out / sign in again | ok (Android offered to save the password; dismissed) |

Found and fixed on the device: the PAR sheet opened under the tab bar (`useRootNavigator`), dialog
buttons were full width (theme `minimumSize`), a stale login error stayed after changing the server,
and a pending loan showed "Outstanding ETB 0.00" (now "–").

Screenshots: `app/screenshots/` (01 login … 14 sign-out dialog).

## What the backend should change for the app

1. **`operationId` on every endpoint** (`@Operation(operationId = "getClient")`): the generator now names
   methods after Java methods (`callGet`, `get1`, `get2`, `get3`), and a backend rename would rename them.
2. **Mark required fields** (`@Schema(requiredMode = REQUIRED)` or `@NotNull` on response records): then the
   generated models are non-nullable and the repositories need fewer defaults.
3. **Money type in the spec**: `type: number` arrives as `num`. Adding `format: decimal` (or sending amounts as
   strings) would let the client map money straight to `Decimal`.
4. **Error responses in the spec**: only 2xx bodies are documented. Declaring the RFC 7807 problem schema
   (with `code` as an enum) would make the error mapper checkable against the API.
5. **README error table**: `PHONE_METHOD_MISMATCH` (Phase 5b) is missing.
6. **A specific code for "not an officer account" at login** (today a generic 403 `FORBIDDEN`).
7. **Seed gender**: every seeded client has `gender: null` in the Codespace (the screen shows "–").
8. **Case-insensitive client search** (Fineract's `LIKE` is case-sensitive; officers type lower case).
9. **`scope.role` in the portfolio** is a free string; an enum (`OFFICER`/`MANAGER`) like `/me`'s roles would be safer.
