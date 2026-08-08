-- A transferencia agora registra uma perna em cada conta (debito na origem,
-- credito no destino). A coluna credit diz a direcao do movimento, para que o
-- extrato e o agregado distingam a perna de credito (entrada) da de debito.
-- Backfill: depositos sao creditos; saques/pagamentos/transferencias sao debitos.
-- O unico ajuste para transferencias pre-existentes e no-op (todas debitadas
-- como FALSE), pois nenhuma delas tinha perna de credito.

ALTER TABLE tb_transaction ADD COLUMN credit BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE tb_transaction SET credit = (type = 'DEPOSIT');
