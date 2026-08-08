-- Idempotency: the client sends an Idempotency-Key header per intent; the server
-- executes a transaction once and returns the original result on retries.
-- The UNIQUE is per account: two different accounts may reuse the same key, but
-- the same account cannot "win" twice with the same key (duplicate-click guard).
-- Nullable: legacy seed rows (V4/V6) carry no key.

ALTER TABLE tb_transaction ADD COLUMN idempotency_key VARCHAR(64);
ALTER TABLE tb_transaction ADD CONSTRAINT uq_tb_transaction_account_idempotency UNIQUE (account_id, idempotency_key);
