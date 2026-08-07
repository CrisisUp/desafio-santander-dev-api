-- Generated V5: 40 complete accounts, each with dedicated features/news.
-- (Schema enforces UNIQUE on features_id/news_id, so each row is owned by one user.)

INSERT INTO tb_user (name) VALUES
    ('Ana Souza'),  -- id 2
    ('Bruno Lima'),  -- id 3
    ('Carla Mendes'),  -- id 4
    ('Diego Rocha'),  -- id 5
    ('Elisa Campos'),  -- id 6
    ('Fábio Nunes'),  -- id 7
    ('Gisele Prado'),  -- id 8
    ('Hugo Tavares'),  -- id 9
    ('Isabela Cruz'),  -- id 10
    ('João Pereira'),  -- id 11
    ('Karen Alves'),  -- id 12
    ('Lucas Ferreira'),  -- id 13
    ('Mariana Dias'),  -- id 14
    ('Nelson Braga'),  -- id 15
    ('Olívia Reis'),  -- id 16
    ('Paulo Castro'),  -- id 17
    ('Quintino Sales'),  -- id 18
    ('Renata Moura'),  -- id 19
    ('Sérgio Pinto'),  -- id 20
    ('Tatiane Lopes'),  -- id 21
    ('Ulisses Ramos'),  -- id 22
    ('Vanessa Silva'),  -- id 23
    ('William Torres'),  -- id 24
    ('Ximena Ruiz'),  -- id 25
    ('Yuri Azevedo'),  -- id 26
    ('Zélia Cardoso'),  -- id 27
    ('André Nogueira'),  -- id 28
    ('Beatriz Faria'),  -- id 29
    ('Caio Rangel'),  -- id 30
    ('Débora Vilela'),  -- id 31
    ('Eduardo Sá'),  -- id 32
    ('Fernanda Costa'),  -- id 33
    ('Gabriel Melo'),  -- id 34
    ('Helena Porto'),  -- id 35
    ('Igor Santana'),  -- id 36
    ('Júlia Neves'),  -- id 37
    ('Kleber Fonseca'),  -- id 38
    ('Larissa Ramos'),  -- id 39
    ('Mauro Dantas'),  -- id 40
    ('Nina Barros');  -- id 41

INSERT INTO tb_account (number, agency, balance, additional_limit) VALUES
    ('0002', '0001', 780.50, 1000.00),
    ('0003', '0001', 1450.00, 1500.00),
    ('0004', '0001', 2230.75, 2000.00),
    ('0005', '0001', 3120.40, 2500.00),
    ('0006', '0001', 4820.90, 3000.00),
    ('0007', '0001', 620.15, 1000.00),
    ('0008', '0001', 1980.00, 1500.00),
    ('0009', '0001', 2760.35, 2000.00),
    ('0010', '0001', 3350.80, 2500.00),
    ('0011', '0001', 5120.45, 3000.00),
    ('0012', '0001', 890.60, 1000.00),
    ('0013', '0001', 1560.25, 1500.00),
    ('0014', '0001', 2410.90, 2000.00),
    ('0015', '0001', 3890.15, 2500.00),
    ('0016', '0001', 5420.30, 3000.00),
    ('0017', '0001', 740.10, 1000.00),
    ('0018', '0001', 1740.75, 1500.00),
    ('0019', '0001', 2580.45, 2000.00),
    ('0020', '0001', 4120.60, 2500.00),
    ('0021', '0001', 5980.25, 3000.00),
    ('0022', '0001', 980.90, 1000.00),
    ('0023', '0001', 1320.40, 1500.00),
    ('0024', '0001', 2050.15, 2000.00),
    ('0025', '0001', 3670.30, 2500.00),
    ('0026', '0001', 4580.75, 3000.00),
    ('0027', '0001', 840.55, 1000.00),
    ('0028', '0001', 1680.20, 1500.00),
    ('0029', '0001', 2230.80, 2000.00),
    ('0030', '0001', 3490.45, 2500.00),
    ('0031', '0001', 5020.10, 3000.00),
    ('0032', '0001', 780.25, 1000.00),
    ('0033', '0001', 1920.60, 1500.00),
    ('0034', '0001', 2840.15, 2000.00),
    ('0035', '0001', 3560.90, 2500.00),
    ('0036', '0001', 4780.35, 3000.00),
    ('0037', '0001', 950.75, 1000.00),
    ('0038', '0001', 1480.30, 1500.00),
    ('0039', '0001', 2190.45, 2000.00),
    ('0040', '0001', 3240.80, 2500.00),
    ('0041', '0001', 4640.20, 3000.00);

INSERT INTO tb_card (number, available_limit) VALUES
    ('**** **** **** 2201', 3000.00),
    ('**** **** **** 2202', 4000.00),
    ('**** **** **** 2203', 5000.00),
    ('**** **** **** 2204', 6000.00),
    ('**** **** **** 2205', 7000.00),
    ('**** **** **** 2206', 3000.00),
    ('**** **** **** 2207', 4000.00),
    ('**** **** **** 2208', 5000.00),
    ('**** **** **** 2209', 6000.00),
    ('**** **** **** 2210', 7000.00),
    ('**** **** **** 2211', 3000.00),
    ('**** **** **** 2212', 4000.00),
    ('**** **** **** 2213', 5000.00),
    ('**** **** **** 2214', 6000.00),
    ('**** **** **** 2215', 7000.00),
    ('**** **** **** 2216', 3000.00),
    ('**** **** **** 2217', 4000.00),
    ('**** **** **** 2218', 5000.00),
    ('**** **** **** 2219', 6000.00),
    ('**** **** **** 2220', 7000.00),
    ('**** **** **** 2221', 3000.00),
    ('**** **** **** 2222', 4000.00),
    ('**** **** **** 2223', 5000.00),
    ('**** **** **** 2224', 6000.00),
    ('**** **** **** 2225', 7000.00),
    ('**** **** **** 2226', 3000.00),
    ('**** **** **** 2227', 4000.00),
    ('**** **** **** 2228', 5000.00),
    ('**** **** **** 2229', 6000.00),
    ('**** **** **** 2230', 7000.00),
    ('**** **** **** 2231', 3000.00),
    ('**** **** **** 2232', 4000.00),
    ('**** **** **** 2233', 5000.00),
    ('**** **** **** 2234', 6000.00),
    ('**** **** **** 2235', 7000.00),
    ('**** **** **** 2236', 3000.00),
    ('**** **** **** 2237', 4000.00),
    ('**** **** **** 2238', 5000.00),
    ('**** **** **** 2239', 6000.00),
    ('**** **** **** 2240', 7000.00);

-- Link users to their account+card (user N <-> account N <-> card N).
UPDATE tb_user SET account_id = 2, card_id = 2 WHERE id = 2;
UPDATE tb_user SET account_id = 3, card_id = 3 WHERE id = 3;
UPDATE tb_user SET account_id = 4, card_id = 4 WHERE id = 4;
UPDATE tb_user SET account_id = 5, card_id = 5 WHERE id = 5;
UPDATE tb_user SET account_id = 6, card_id = 6 WHERE id = 6;
UPDATE tb_user SET account_id = 7, card_id = 7 WHERE id = 7;
UPDATE tb_user SET account_id = 8, card_id = 8 WHERE id = 8;
UPDATE tb_user SET account_id = 9, card_id = 9 WHERE id = 9;
UPDATE tb_user SET account_id = 10, card_id = 10 WHERE id = 10;
UPDATE tb_user SET account_id = 11, card_id = 11 WHERE id = 11;
UPDATE tb_user SET account_id = 12, card_id = 12 WHERE id = 12;
UPDATE tb_user SET account_id = 13, card_id = 13 WHERE id = 13;
UPDATE tb_user SET account_id = 14, card_id = 14 WHERE id = 14;
UPDATE tb_user SET account_id = 15, card_id = 15 WHERE id = 15;
UPDATE tb_user SET account_id = 16, card_id = 16 WHERE id = 16;
UPDATE tb_user SET account_id = 17, card_id = 17 WHERE id = 17;
UPDATE tb_user SET account_id = 18, card_id = 18 WHERE id = 18;
UPDATE tb_user SET account_id = 19, card_id = 19 WHERE id = 19;
UPDATE tb_user SET account_id = 20, card_id = 20 WHERE id = 20;
UPDATE tb_user SET account_id = 21, card_id = 21 WHERE id = 21;
UPDATE tb_user SET account_id = 22, card_id = 22 WHERE id = 22;
UPDATE tb_user SET account_id = 23, card_id = 23 WHERE id = 23;
UPDATE tb_user SET account_id = 24, card_id = 24 WHERE id = 24;
UPDATE tb_user SET account_id = 25, card_id = 25 WHERE id = 25;
UPDATE tb_user SET account_id = 26, card_id = 26 WHERE id = 26;
UPDATE tb_user SET account_id = 27, card_id = 27 WHERE id = 27;
UPDATE tb_user SET account_id = 28, card_id = 28 WHERE id = 28;
UPDATE tb_user SET account_id = 29, card_id = 29 WHERE id = 29;
UPDATE tb_user SET account_id = 30, card_id = 30 WHERE id = 30;
UPDATE tb_user SET account_id = 31, card_id = 31 WHERE id = 31;
UPDATE tb_user SET account_id = 32, card_id = 32 WHERE id = 32;
UPDATE tb_user SET account_id = 33, card_id = 33 WHERE id = 33;
UPDATE tb_user SET account_id = 34, card_id = 34 WHERE id = 34;
UPDATE tb_user SET account_id = 35, card_id = 35 WHERE id = 35;
UPDATE tb_user SET account_id = 36, card_id = 36 WHERE id = 36;
UPDATE tb_user SET account_id = 37, card_id = 37 WHERE id = 37;
UPDATE tb_user SET account_id = 38, card_id = 38 WHERE id = 38;
UPDATE tb_user SET account_id = 39, card_id = 39 WHERE id = 39;
UPDATE tb_user SET account_id = 40, card_id = 40 WHERE id = 40;
UPDATE tb_user SET account_id = 41, card_id = 41 WHERE id = 41;

-- Dedicated features per user (brandIcon-safe descriptions).
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (2,6,0), (2,7,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (3,8,0), (3,9,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (4,10,0), (4,11,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (5,12,0), (5,13,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (6,14,0), (6,15,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (7,16,0), (7,17,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (8,18,0), (8,19,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (9,20,0), (9,21,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (10,22,0), (10,23,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (11,24,0), (11,25,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (12,26,0), (12,27,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (13,28,0), (13,29,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (14,30,0), (14,31,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (15,32,0), (15,33,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (16,34,0), (16,35,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (17,36,0), (17,37,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (18,38,0), (18,39,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (19,40,0), (19,41,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (20,42,0), (20,43,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (21,44,0), (21,45,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (22,46,0), (22,47,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (23,48,0), (23,49,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (24,50,0), (24,51,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (25,52,0), (25,53,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (26,54,0), (26,55,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (27,56,0), (27,57,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (28,58,0), (28,59,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (29,60,0), (29,61,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (30,62,0), (30,63,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (31,64,0), (31,65,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (32,66,0), (32,67,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (33,68,0), (33,69,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (34,70,0), (34,71,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (35,72,0), (35,73,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'Crédito');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (36,74,0), (36,75,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg', 'PIX');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (37,76,0), (37,77,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg', 'Pagar');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (38,78,0), (38,79,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (39,80,0), (39,81,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg', 'Conta Corrente');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (40,82,0), (40,83,1);
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg', 'Cartões');
INSERT INTO tb_feature (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/others.svg', 'Investimentos');
INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (41,84,0), (41,85,1);

-- Dedicated news per user (1 each, cycling credit/insurance).
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (2, 3, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (3, 4, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (4, 5, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (5, 6, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (6, 7, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (7, 8, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (8, 9, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (9, 10, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (10, 11, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (11, 12, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (12, 13, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (13, 14, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (14, 15, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (15, 16, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (16, 17, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (17, 18, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (18, 19, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (19, 20, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (20, 21, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (21, 22, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (22, 23, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (23, 24, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (24, 25, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (25, 26, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (26, 27, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (27, 28, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (28, 29, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (29, 30, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (30, 31, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (31, 32, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (32, 33, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (33, 34, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (34, 35, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (35, 36, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (36, 37, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (37, 38, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (38, 39, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (39, 40, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg', 'Seguro de vida com cobertura completa. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (40, 41, 0);
INSERT INTO tb_news (icon, description) VALUES ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg', 'O Santander tem soluções de crédito sob medida pra você. Confira!');
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (41, 42, 0);
