# 08 — A real provider: Chapa (test mode) behind an adapter (Phase 5b)

Date: 2026-10-24

Phase 5 collected repayments through our own simulator, "TelePay". Phase 5b adds a **real**
payment gateway, [Chapa](https://developer.chapa.co), in test mode, for **telebirr** (Ethio Telecom,
numbers 09…) and **M-Pesa** (Safaricom Ethiopia, numbers 07…). No real money moves. The inbox, the
posting worker and the reconciliation matcher did not change.

## The adapter pattern

Before, `MobilePaymentService` called `TelePayClient` directly, so our business code knew TelePay's URLs
and status words. Now there is an interface, the **port**, written in our own words:

```
MobileMoneyProvider
  createCharge(reference, phone, amount, method)   -> accepted? provider's charge id
  verifyCharge(reference)                          -> SUCCEEDED | DECLINED | PENDING | NOT_FOUND | UNKNOWN
  parseAndVerifyWebhook(headers, rawBody)          -> Rejected | Trusted(event) | Hint(reference)
  listSettlements(date)                            -> list, or "none" (empty)
  needsVerification()                              -> are webhooks only hints?
```

Each provider gets one **adapter** class that translates. It knows that Chapa wants multipart forms,
calls our reference `tx_ref`, says `failed/cancelled`, and signs webhooks in `x-chapa-signature`.

```
MobilePaymentService ─┐                       ┌─ SimulatorProvider ─ TelePayClient ─HTTP─▶ /sim (our simulator)
WebhookController  ───┼─▶ MobileMoneyProvider ┤
PaymentVerifier    ───┤     (interface)       └─ ChapaProvider ──────────────HTTP──▶ api.chapa.co
SettlementSource   ───┘
```

Spring creates exactly one adapter, picked with `app.mobile-money.provider=simulator|chapa`
(`@ConditionalOnProperty`). Adding a direct telebirr integration later means writing a
`TelebirrProvider`; nothing else changes. The adapter is also where provider-specific settings are
checked. The simulator needs its URL, API key and 32-character secret. Chapa needs a **test** key: the
app refuses to start with anything not starting `CHASECK_TEST-`, and the key never appears in a log or
an error message.

## What differs from our simulator

| | TelePay simulator | Chapa (seen live) |
| --- | --- | --- |
| Charge request | JSON, plus a callback URL per charge | multipart form; webhook URL **only in the dashboard** |
| "Not started" | HTTP error | HTTP **200** with `"status":"failed"`; the body decides, not the status code |
| Customer step | we press Approve/Decline | USSD push on the phone; **test numbers complete by themselves in a few seconds** |
| Status lookup | none (we trusted webhooks) | `GET /transaction/verify/{tx_ref}` |
| Webhook signature | HMAC(secret, timestamp + "." + body) | `x-chapa-signature` = HMAC(secret, body); `chapa-signature` = HMAC(secret, secret), the **same for every webhook**, so useless as proof |
| Replay protection | signed timestamp (5 min) + unique eventId | **none**: no timestamp, no event id |
| Settlement listing | `GET /sim/settlements?date=` | none for direct charges |
| Status words | SUCCEEDED / DECLINED | `success`, `failed/cancelled`, `pending` (+ refunded, reversed…) |
| Receipt in Fineract | provider txn id `TP…` | Chapa reference `CH…` |
| Encryption key | n/a | only for card numbers and OTPs (3DES); **not needed** for telebirr/M-Pesa |

## Verify before trusting

Chapa's webhook is authentic when its HMAC matches, but it could be a **replay** of an old genuine
webhook, and it carries no event id to dedupe on. So for Chapa a webhook is only a **hint**: "go and
look at tx_ref AMP-…". `PaymentVerifier` then asks Chapa's verify API over **our own** authenticated
HTTPS connection, and acts on that answer only:

- `success`, tx_ref matches, Chapa reference matches the one from the charge → a `ProviderPaymentEvent`
  (event id `chapa:<ref>:SUCCEEDED`) goes into **the same inbox** as simulator webhooks. The inbox
  checks amount and currency (MISMATCH otherwise), and **the same posting worker** posts it.
- `failed/cancelled` → DECLINED, nothing posted.
- `pending`, Chapa unreachable, or "unknown reference" for a charge that did start → check again later.
- A status we don't know (`refunded`, `reversed`, something new) → **NEEDS_ATTENTION, never POSTED**.
- The charge never started and Chapa has no record of it → CHARGE_FAILED.

The same event id is produced whichever way we learn the result first (webhook or poll), and the
inbox's unique index lets only the first one through. That is the "posted once" guarantee.

**Polling.** A timer checks every PENDING Chapa payment: first 10 s after the charge (to give the
webhook a head start), then 5 s, 10 s, 20 s… up to 2 min, and NEEDS_ATTENTION after 20 checks
(about 30 minutes). So a payment completes even if no webhook ever arrives, e.g. because the
dashboard URL is wrong or port 8080 is private.

**Charge errors.** If Chapa doesn't answer the charge request (timeout, 5xx), we do *not* mark it
CHARGE_FAILED as we do with the simulator. A timeout can hide a charge that did start, and the client
may already be confirming on their phone. It stays PENDING and verify finds out.

## Reconciliation without a settlement listing

Chapa has no "list of payments on day X" for direct charges. `SettlementSource` therefore asks verify
about **each of our own intents** created that day or the day before, and keeps the successful ones
paid on that day. The matcher then runs unchanged.

Trade-off: a provider's listing comes from *their* books, so it also shows money we have no record of.
Verifying our own intents can only find payments we started. For this app that is fine (every charge
starts from an intent), and Fineract-side problems (POSTED_NOT_PAID) are still found. A real MFI would
also compare Chapa's dashboard export or the bank settlement statement. It also costs one API call per
intent, which is fine for a branch's daily volume.

## Live results (Codespace, 2026-10-24, Chapa test mode)

| Test | Result |
| --- | --- |
| telebirr 0900123456, 50.00, poller | verify `success` after 16 s, POSTED, Fineract receipt `CHiEcimRtDjrb` |
| M-Pesa 0700123456, 75.00, poller | POSTED, receipt `CHuczsPz6WXe4` |
| telebirr 0911000000 (not a test number) | charge `failed` "Invalid Test Number", verify `failed/cancelled` → DECLINED, nothing posted |
| webhook path, poller off, 0900123456, 60.00 | Chapa's signed `charge.success` arrived 3.5 s later; HMAC over the raw body matched; verify `success`; POSTED, receipt `CH66dSj6e69K8` |
| replay of that exact webhook | 200 `IGNORED`, still 1 inbox row and 1 Fineract repayment |
| same webhook with amount changed, original signature | **401** `WEBHOOK_SIGNATURE_INVALID`, audited DENIED |
| reconciliation for the day | 3 MATCHED, 185.00 ETB on both sides, from 5 verify calls (2 declined skipped) |

Surprise: Chapa's docs list `0900112233` as a test number, but test mode rejected it ("Invalid Test
Number"). Only `0900123456` / `0700123456` worked.

## What a direct telebirr or Safaricom M-Pesa integration would add

Chapa is an **aggregator**: one contract and one API for many wallets and banks, at the price of its
fee (0.25 ETB on our 10 ETB probe) and one more company between us and the money. Going direct
typically means:

- **A merchant agreement with each operator**: Ethio Telecom (telebirr) and Safaricom (M-Pesa), each
  with onboarding, a merchant short code / till, KYC and settlement to the MFI's bank account,
  under National Bank of Ethiopia rules.
- **Each operator's own security scheme**: request signing with keys issued per merchant (telebirr's
  merchant API uses RSA-signed requests; M-Pesa's Daraja-style API uses an OAuth token plus a password
  derived from short code + passkey + timestamp), certificates, and often an IP allow-list for callbacks.
  One more adapter each, but the port and everything behind it stay the same.
- **Callbacks that may not be signed at all**: then the "verify before trusting" rule matters even more
  (the operator's transaction-status query API), plus allow-listing their callback IPs.
- **Real settlement files**: operators provide daily statements/settlement reports, so reconciliation
  can use the operator's books (`listSettlements`) instead of verifying our own intents.
- **More operations**: reversals/refunds, B2C (paying *out* loan disbursements to wallets), balance
  queries, and handling the USSD prompt timing out.
- **Production hardening** we skipped: live keys in a real secret store, monitoring and alerting on
  NEEDS_ATTENTION, rate limits, and a reviewed go-live (this project only accepts test keys on purpose).

## Codespaces changes

- No `ADEY_JWT_SECRET` (e.g. a visitor using the "Open in Codespaces" badge): startup generates a random
  one for that start only and logs "generated an ephemeral JWT secret" (never the value).
- `CHAPA_SECRET_KEY` present → `MOBILE_MONEY_PROVIDER=chapa`, else the simulator, with a log line either way.
- New Codespaces secret `CHAPA_WEBHOOK_SECRET` (64 random characters), entered in Chapa's dashboard.
