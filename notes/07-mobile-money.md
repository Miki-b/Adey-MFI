# 07 — Mobile money: webhooks, signatures, retries, reconciliation (Phase 5)

Date: 2026-10-24

A client pays a loan installment from their phone wallet instead of handing cash to the officer.
Money now moves through a **third system** (the mobile money provider, "TelePay"). The provider,
our backend and Fineract can each fail or disagree, and the patterns below exist to deal with that.

## The flow

```
 Officer app             Adey backend                                   TelePay (provider)
 ───────────             ────────────                                   ──────────────────
 "Collect 1833.60" ───▶ check access, amount ≤ outstanding
                        store intent PENDING  (ref AMP-BB54…)
                        POST /charges {ref, phone, amount, callbackUrl} ──▶ charge PENDING
 ◀── intent (PENDING)                                                    client's phone: "Pay 1833.60?"
 poll GET /api/mobile-payments/{id}                                      client presses Pay
                                                                           │
                        POST /api/webhooks/mobile-money  ◀──────────────────┘ signed webhook
                        ① signature ok?  ② timestamp < 5 min?  ③ eventId new?
                        ④ INBOX: store event + intent RECEIVED (one DB transaction)
                        ⑤ answer 200 ─────────────────────────────────────▶ (stops re-sending)

                        POSTING WORKER (every 5 s, separately):
                        receipt TP… already on the loan in Fineract? ── yes ─▶ POSTED (no second post)
                                     │ no
                        POST repayment "Mobile Money", receipt = TP… ──▶ Fineract
                             ok ─▶ POSTED
                             Fineract down ─▶ RETRYING, wait 10s, 20s, 40s, 80s, 2m … ─▶ POSTED
                                              after 8 failures ─▶ NEEDS_ATTENTION (a person looks)

 NIGHTLY (00:30) or a manager: RECONCILIATION
   provider settlements  ⟷  our intents  ⟷  Fineract Mobile Money repayments (by receipt number)
   → MATCHED / PAID_NOT_POSTED / POSTED_NOT_PAID / AMOUNT_MISMATCH → manager resolves
```

## The patterns, in plain words

### Webhook
Instead of us asking the provider every few seconds "has the client paid yet?" (polling), the
provider **calls us** when something happens: an HTTP POST to a URL we gave it (the callback URL).
That's faster and cheaper, but it means a public URL on our side that anyone on the internet can call.

### Signature (HMAC)
So how do we know a webhook really comes from TelePay? We share a **secret** with the provider
(an environment variable on both sides, never sent over the wire). For every webhook the provider computes

```
signature = HMAC-SHA256(secret, timestamp + "." + body)
```

and sends it in a header. We compute the same thing over the **exact bytes** we received. If even one
character of the body or the timestamp was changed, or the sender doesn't know the secret, the
two values differ and we answer **401**. The comparison is *constant-time*: an ordinary string
comparison stops at the first difference, and measuring that time could leak the signature one
character at a time.

### Replay protection
A signature proves who wrote a message, not *when*. An attacker who captured one genuine "paid
1833.60" webhook could send it again tomorrow. Two defences:
- **Timestamp window.** The timestamp is inside the signature (so it can't be changed), and we refuse
  anything more than **5 minutes** old (or in the future).
- **eventId.** Inside those 5 minutes, every event id is stored with a UNIQUE constraint. The
  same event a second time does nothing. This also absorbs the provider's own legitimate re-sends.

### Inbox pattern
The webhook handler does almost nothing: verify, **store the event and the intent's new status in
one transaction**, answer 200. Posting to Fineract happens later in a separate worker. Why?
- Providers wait only a few seconds and **re-send** if we don't answer 2xx. If we posted to Fineract
  inside the webhook and Fineract was slow or down, the provider would retry while our first
  attempt might still succeed, and we'd risk a double payment.
- The stored row survives crashes and restarts: nothing a provider told us can be lost.
- Live proof: with Fineract **stopped**, the webhook was still answered 200 ACCEPTED.

(The mirror image, the *outbox* pattern, is for messages *we* send: write "to send" into our DB in the
same transaction as the change, and let a worker deliver it. We used the same idea for posting.)

### Retry with exponential backoff
When Fineract is unreachable, we try again, but **waiting longer each time** (10 s, 20 s, 40 s,
80 s, then every 2 min). Retrying instantly in a loop would hammer a system that's already in
trouble; a short outage needs only a few tries anyway. After **8** failures (~8.5 minutes) we stop
and mark the payment **NEEDS_ATTENTION**, because a person must look at it. Errors that won't go away by
waiting (Fineract says 4xx, e.g. "loan is closed") go to NEEDS_ATTENTION immediately.

**Never post twice.** Two independent guards, because "did my last attempt work?" is sometimes unknowable
(a timeout after Fineract already booked it):
1. our own DB status (POSTED intents are skipped);
2. before every post, the worker looks for a repayment on the loan whose **receipt number** is the
   provider's transaction id. If it's there, it records POSTED instead of posting again.

### Reconciliation
Despite all of the above, webhooks get lost, a provider can report a wrong amount, and someone can
type a "Mobile Money" repayment into Fineract by hand. So every day we line up **three independent
sources** and explain every difference:

| Finding | Meaning | Typical cause | Resolution |
| --- | --- | --- | --- |
| MATCHED | provider paid = Fineract posted, same amount | normal | nothing |
| PAID_NOT_POSTED | provider has it, Fineract doesn't | lost or rejected webhook, worker gave up | manager clicks resolve: **posted through the same path** |
| POSTED_NOT_PAID | Fineract has a Mobile Money repayment, provider doesn't | manual entry, test, provider-side reversal | investigate, note required |
| AMOUNT_MISMATCH | amounts differ | provider error, partial payment | investigate, note required |

The match key is the **receipt number**: the provider's transaction id, which our worker writes
into every Fineract repayment. A resolved finding stays resolved in later runs.

## Live results (2026-10-24, Bishoftu loan 1 of Yohannes, officer Samuel)

1. **Happy path.** 1,833.60 requested → approved in the simulator → webhook `ACCEPTED` → worker posted
   **Fineract transaction 42**, payment type *Mobile Money*, receipt **TP880001**, split
   **1,603.65 principal + 229.95 interest** (installment 4's interest). History: PENDING → RECEIVED → POSTED.
2. **Duplicate webhook.** Delivered twice: first `ACCEPTED`, second `DUPLICATE`. One inbox row,
   **one** Fineract repayment (43).
3. **Wrong signature.** 401 `WEBHOOK_SIGNATURE_INVALID`; also for a hand-forged webhook, a missing
   signature and a body tampered with after signing. Inbox rows: 0. Audit: `WEBHOOK DENIED` per attempt.
4. **Old timestamp** (signed 10 min ago): 401 `WEBHOOK_TIMESTAMP_INVALID`.
5. **Fineract outage.** `docker compose stop fineract`, approve → webhook still 200. Worker:
   RETRYING after 10 s, 20 s, 40 s, 80 s, 2 min (5 failed attempts while Fineract was down and then
   booting), then **POSTED once** (transaction 44) on the 6th attempt.
6. **Lost webhook** (`drop=true`): intent stayed PENDING; reconciliation → **PAID_NOT_POSTED**;
   manager Tadesse resolved it → posted as transaction 46 (history: "found by reconciliation run 1").
   Adama's manager got 403; resolving again 409.
7. **Amount mismatch** (`reportAmount=1000` for 1,833.60): intent **MISMATCH**, nothing posted;
   reconciliation → **AMOUNT_MISMATCH**; resolve without a note → 400, with a note → resolved.
8. **Hana** (another officer) collecting on Samuel's client's loan → **403**; polling one of Samuel's
   payments → 403; more than outstanding → 422 `AMOUNT_EXCEEDS_OUTSTANDING`.

The second reconciliation showed MATCHED ×2, AMOUNT_MISMATCH ×1 and POSTED_NOT_PAID ×3. The three
POSTED_NOT_PAID are scenarios 1, 2 and 5: genuine payments, but the simulator keeps its books **in
memory** and had been restarted, so "the provider" no longer knew them. That is exactly what this
finding is for: Fineract says paid, the provider has no record. One was resolved with an explanation.

## Problems hit

1. **The JWT filter ate the provider's API key.** Our backend sends the provider key as
   `Authorization: Bearer …`. Spring Security's bearer filter reads *every* Bearer header, also on
   `/sim/**`, tried to verify it as our JWT, and answered 401 before the simulator saw it. Fixed with a
   bearer-token resolver that ignores `/sim/**` and `/api/webhooks/**`. (With a real, separate provider
   this can't happen; it's a side effect of hosting the simulator inside our app.)
2. **Status polling needed Fineract.** `GET /api/mobile-payments/{id}` checked access by asking Fineract
   whose client it is, so it failed with 503 during the outage, exactly when officers want to look.
   Flyway **V4** stores the client's officer on the intent; polling now never calls Fineract.
3. **The seed never exited.** Since Phase 4 scheduling is on, and the scheduler's threads keep a
   web-less app alive. The seeder now shuts the app down explicitly.
4. **Simulator transaction ids repeated after a restart** (TP880001 again). A real provider never
   reuses ids; reconciliation would have matched the wrong repayment. Ids are now time-based.
5. **`@Transactional` on a method called from the same class does nothing.** Spring adds
   transactions through a proxy around the bean, which a call from inside the class bypasses. The inbox
   uses an explicit `TransactionTemplate` instead.
6. **RAM.** One JVM: the jar via `java -Xmx512m -jar` instead of `mvnw spring-boot:run` (which keeps a
   Maven JVM around). On Windows the running jar is locked, so stop → package → start.

## Limits (fine for now)

- The simulator's state is in memory (restart = provider "forgets" its charges and settlements).
- The provider doesn't retry webhooks we answered with an error; reconciliation is our safety net.
- Fineract has no "all transactions of a day" query, so reconciliation reads every loan's transactions
  (8 in parallel). For a large portfolio, use a Fineract report or a nightly export.
- One posting worker per backend; the version column (optimistic locking) keeps two from moving the
  same payment, but rows are not explicitly claimed. Fine for one instance.

## State left behind

- Fineract: Mobile Money payment type (id 5); repayments 42, 43, 44, 45, 46 on loan 1.
- Our DB: intents 1–11 (some PENDING/CHARGE_FAILED/MISMATCH from the demos), 2 reconciliation runs.
