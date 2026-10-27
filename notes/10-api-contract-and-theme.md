# 10 — Polishing the API contract, and the app's brand theme

Date: 2026-10-27

Session A of the app showed where the backend's OpenAPI spec was too vague for a generated client.
This session tightened the spec, regenerated the Dart client, and gave the app its brand colours.

## The API contract (backend)

The Flutter app's client is generated from `backend/openapi.json`, so everything the spec leaves
vague becomes awkward code in the app. Fixed:

| Before | After | How |
| --- | --- | --- |
| Dart methods `callGet`, `get1`, `get2`, `get3` | `getClient`, `getClientLoans`, `getPortfolio`, `recordRepayment`, ... | `@Operation(operationId = ...)` on all 29 endpoints |
| every field optional, so every Dart field nullable | **required = always sent and non-null** | `OpenApiConfig`: record components are required unless annotated JSpecify `@Nullable` |
| money `type: number` | `type: number, format: decimal` | same customizer, for every `BigDecimal` |
| errors undocumented | `4XX`/`5XX` → `Problem` (RFC 7807 + `code` enum) on every operation | same customizer + `common/ErrorCodes.java` |
| `LoanDetails.summary` pointed at the **reconciliation** summary | `LoanTotals` / `ReconciliationSummary` | two Java records were both named `Summary`; springdoc keyed schemas by simple name |
| `scope.role` a free string | enum `OFFICER` / `MANAGER` | our `Role` enum |

**Why "required = non-null" and not "required = present".** The generated Dart model throws if a
required field is missing or null. So the rule has to be strict, and when unsure, a field is marked
`@Nullable`. A wrongly optional field only costs the app a fallback value; a wrongly required one
makes the whole response unreadable. To prove the markings against real data, a live check
walked every readable endpoint for all four demo users in the Codespace: **4,222 required fields,
0 null**.

**Why money stays a JSON number.** Strings (`"1833.60"`) are the safest wire format for decimals,
but they would break every existing client (Postman collection, tests, the app) for little gain at our
magnitudes. A JSON number written by Java's `BigDecimal` is exact; the risk is only on the reading
side. So the spec now says `format: decimal`, and the app parses each value into `Decimal` straight
from its shortest string form (`toDecimal` / `decimalOf`), which gives back exactly what the backend wrote.

**Keeping the error codes honest.** Codes were string literals in about six different shapes
(`new ApiException(status, "CODE", ..)`, `problem.setProperty("code", ..)`, `Exception("CODE", ..)`,
switch arms, constants). `ErrorCodes.ALL` is now the single list (it feeds the spec's enum), and
`ErrorCodeCatalogTest` scans the source for every shape and checks: source ⊆ list, list ⊆ source, and
list = the README table. It immediately found `PHONE_METHOD_MISMATCH` missing from the README.

Other backend changes:
- **Login with a non-officer account** → 403 `NOT_AN_OFFICER` (was a generic `FORBIDDEN`).
- **Client search ignores case.** Fineract's `displayName` filter is a case-sensitive SQL `LIKE`. With search text,
  the backend reads the caller's token-scoped list (pages of 200, at most 2,000 clients) and filters it itself;
  without search text Fineract still pages. Limit: very large offices would need Fineract-side support.
- **Seed:** every client has a gender; existing ones are updated once (a second seed run changes nothing).
- **Payment history wording is provider-neutral:** "provider result (webhook): paid…",
  "provider result (verify API): declined or failed at the provider…". Results learnt from Chapa's verify API
  are audited as `MOBILE_PAYMENT_VERIFY` by `system:mobile-money-verifier`, not as webhooks.
- **Chapa webhook secret rotated** (Codespaces secret `CHAPA_WEBHOOK_SECRET`); verified: new secret 200, wrong 401.

## The brand theme (app)

Brand: **grass green** (primary) and **yellow** (secondary), used alternately:
- **green** for primary actions (filled buttons), the active tab, key headers and links;
- **yellow** for chips, badges, secondary (tonal) actions and the brand mark.

WCAG AA asks 4.5:1 for normal text (3:1 for large text and UI parts). The seeds don't pass as they are:

| Colour | Problem | Used instead |
| --- | --- | --- |
| green `#3F8F29` with white text | 4.06:1 | light: `#2E6B1F` (6.5:1); dark: `#8FD975` on the dark surface (11:1) |
| yellow `#F2C200` as text on white | 1.7:1 | never text on light backgrounds; a **fill** with dark text `#2B2200` (9.4:1). On dark surfaces yellow text is fine (11:1) |

**PAR colours vs brand.** Green/amber/red would clash with a green-and-yellow brand, so:
- **healthy is teal** (`#00695C` / `#5FD4C4`), not green;
- **watch is orange** (`#9A4A06` / `#FF7F3F`), not amber;
- **high is red.**

Every PAR colour still comes with a word ("Healthy", "Watch", "High risk"), so colour is never the
only signal. The first dark-mode orange (`#FDBA74`) had the yellow's exact lightness, and the test caught it.

`test/core/theme/theme_contrast_test.dart` computes the WCAG ratio for every text/background pair the app
uses, in both themes, and checks the PAR colours differ from the brand in hue and lightness.

Settings → **Theme**: System (default) / Light / Dark, stored on the phone.

## Small lessons

- Debian's `python3-minimal` (the Codespace image) has no `json` module; run such scripts from the PC
  through a private `gh codespace ports forward` instead.
- After the generated package renames files (here `summary.dart` → `loan_totals.dart`), the app's
  `build_runner` cache is stale: `fvm dart run build_runner clean`, then build.
- A Python `str.replace(old, new)` with an empty `old` inserts `new` between every character. Always
  assert the anchor exists exactly once before replacing.
