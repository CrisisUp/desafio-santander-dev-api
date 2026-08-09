-- Who created a transaction (for compliance/audit). Nullable: no auth exists
-- yet, so API-created rows are "system" (null). A future auth layer fills the
-- authenticated user's id here. Seed rows (V4/V6) are null by backfill default.

ALTER TABLE tb_transaction ADD COLUMN created_by BIGINT;
CREATE INDEX idx_tb_transaction_created_by ON tb_transaction (created_by);
