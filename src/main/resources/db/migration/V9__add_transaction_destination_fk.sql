-- Referential integrity for the transfer counterpart: destination_account_id
-- (the destination of a debit leg, the source of a credit leg) must point at a
-- real account. The service validates existence at runtime; this FK is the
-- database-level backstop. ddl-auto: validate ignores FKs (JPA maps the field
-- as a plain Long), so no entity change is needed.
-- Existing seed rows all reference real account ids, so this applies cleanly.

ALTER TABLE tb_transaction
    ADD CONSTRAINT fk_tb_transaction_destination
    FOREIGN KEY (destination_account_id) REFERENCES tb_account (id);
