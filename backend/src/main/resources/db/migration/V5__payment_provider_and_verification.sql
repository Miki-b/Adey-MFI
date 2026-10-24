-- V5 (Phase 5b): more than one mobile money provider (adapter pattern), and verification polling.

-- Which adapter handled the payment ("simulator" or "chapa") and which wallet (TELEBIRR, MPESA).
-- Every intent before V5 went through the TelePay simulator with a telebirr-style number.
ALTER TABLE payment_intents ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'simulator';
ALTER TABLE payment_intents ADD COLUMN method VARCHAR(20);
UPDATE payment_intents SET method = 'TELEBIRR' WHERE method IS NULL;

-- Chapa: a PENDING payment is checked with Chapa's verify API until it is decided (with backoff).
ALTER TABLE payment_intents ADD COLUMN verify_attempts INT NOT NULL DEFAULT 0;
ALTER TABLE payment_intents ADD COLUMN next_verify_at TIMESTAMP WITH TIME ZONE;
CREATE INDEX idx_payment_intents_verify ON payment_intents (status, provider, next_verify_at);
