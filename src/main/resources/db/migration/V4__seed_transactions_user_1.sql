-- Historical statement for the seeded account (id 1), matching balance 624.12:
-- 1000.00 - 375.88 = 624.12, so the derived balance is self-consistent with V2.

INSERT INTO tb_transaction (account_id, type, amount, created_at) VALUES (1, 'DEPOSIT', 1000.00, '2024-01-05 09:00:00');
INSERT INTO tb_transaction (account_id, type, amount, created_at) VALUES (1, 'WITHDRAWAL', 375.88, '2024-01-20 14:30:00');
