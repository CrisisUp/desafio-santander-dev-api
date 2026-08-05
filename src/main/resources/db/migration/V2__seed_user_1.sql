-- Seeds the historical user that the API protects from writes (Devweekerson).
-- IDs are implicit: on an empty database the identity columns assign
-- user=1, account=1, card=1, features=1..5, news=1..2 — matching the mock payload.

INSERT INTO tb_user (name) VALUES ('Devweekerson');
INSERT INTO tb_account (number, agency, balance, additional_limit) VALUES ('01.097954-4', '2030', 624.12, 1000.00);
INSERT INTO tb_card (number, available_limit) VALUES ('xxxx xxxx xxxx 1111', 2000.00);

INSERT INTO tb_feature (icon, description) VALUES
    ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pix.svg',      'PIX'),
    ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/pay.svg',      'Pagar'),
    ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/transfer.svg', 'Transferir'),
    ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/account.svg',  'Conta Corrente'),
    ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/cards.svg',    'Cartões');

INSERT INTO tb_news (icon, description) VALUES
    ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg',
     'O Santander tem soluções de crédito sob medida pra você. Confira!'),
    ('https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg',
     'Santander Seguro Casa, seu faz-tudo. Mais de 50 serviços pra você. Confira!');

-- user id=1, account id=1, card id=1.
UPDATE tb_user SET account_id = 1, card_id = 1 WHERE id = 1;

INSERT INTO tb_user_features (user_id, features_id, features_order) VALUES (1, 1, 0), (1, 2, 1), (1, 3, 2), (1, 4, 3), (1, 5, 4);
INSERT INTO tb_user_news (user_id, news_id, news_order) VALUES (1, 1, 0), (1, 2, 1);
