-- The 3-movement seed accounts (V6) accidentally backdated their WITHDRAWAL to
-- the previous year, so the statement read, e.g., PAYMENT 2026-05-13 followed
-- by WITHDRAWAL 2025-01-14. Align each with the intended +3-month cadence used
-- by the in-order accounts (verify with account 5: DEPOSIT 2025-12-10,
-- PAYMENT 2026-03-11, WITHDRAWAL 2026-06-12): WITHDRAWAL = PAYMENT month + 3,
-- day + 1.
--
-- Matching on (account_id, type, amount, created_at) keeps each UPDATE precise:
-- a real transaction created on the same account after the seed is never touched.
--
-- A fresh database runs V6 with the original dates, then this migration corrects
-- them; an existing database (V1..V9 applied) gets the same correction. Amounts
-- and types are unchanged, so the per-type aggregate totals stay identical.

UPDATE tb_transaction SET created_at = '2026-08-14 18:15:00' WHERE account_id = 7  AND type = 'WITHDRAWAL' AND amount = 559.00 AND created_at = '2025-01-14 18:15:00';
UPDATE tb_transaction SET created_at = '2026-10-16 10:29:00' WHERE account_id = 9  AND type = 'WITHDRAWAL' AND amount = 333.00 AND created_at = '2025-03-16 10:29:00';
UPDATE tb_transaction SET created_at = '2026-08-24 18:25:00' WHERE account_id = 17 AND type = 'WITHDRAWAL' AND amount = 429.00 AND created_at = '2025-01-24 18:25:00';
UPDATE tb_transaction SET created_at = '2026-10-06 10:39:00' WHERE account_id = 19 AND type = 'WITHDRAWAL' AND amount = 203.00 AND created_at = '2025-03-06 10:39:00';
UPDATE tb_transaction SET created_at = '2026-08-14 18:35:00' WHERE account_id = 27 AND type = 'WITHDRAWAL' AND amount = 299.00 AND created_at = '2025-01-14 18:35:00';
UPDATE tb_transaction SET created_at = '2026-10-16 10:49:00' WHERE account_id = 29 AND type = 'WITHDRAWAL' AND amount = 573.00 AND created_at = '2025-03-16 10:49:00';
UPDATE tb_transaction SET created_at = '2026-08-24 18:45:00' WHERE account_id = 37 AND type = 'WITHDRAWAL' AND amount = 169.00 AND created_at = '2025-01-24 18:45:00';
UPDATE tb_transaction SET created_at = '2026-10-06 10:59:00' WHERE account_id = 39 AND type = 'WITHDRAWAL' AND amount = 443.00 AND created_at = '2025-03-06 10:59:00';
