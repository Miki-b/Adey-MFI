-- V4: remember the client's loan officer on each payment intent, so the app can check who may see
-- a payment WITHOUT asking Fineract (status polling must keep working while Fineract is down).
ALTER TABLE payment_intents ADD COLUMN staff_id BIGINT;

-- Intents created before V4: all were requested on loan 1, whose client is staff 1's (Samuel).
-- Rows that stay NULL fall back to the Fineract check.
UPDATE payment_intents SET staff_id = 1 WHERE staff_id IS NULL AND client_id = 1;
