-- The ETL (etl-pipeline/main.py) appends a personalized marketing message to a
-- user's `news`, historically WITHOUT an icon. The API never enforced icon on
-- tb_news, so a message created with icon=NULL rendered with a broken image.
-- Fix the data: backfill the null icons of the seeded rows with the brand icon
-- that matches their description, and give the ETL-generated messages a valid
-- icon (the marketing/Seguro brand). Rows created by the ETL in existing dev
-- databases carry icon=NULL and a description of the shape "Olá ... seu saldo...".
-- This applies cleanly: a fresh DB runs V2/V5 first, so every non-ETL row here
-- has a known description; V6/V7/V8/V9 leave tb_news untouched.

UPDATE tb_news
SET icon = 'https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg'
WHERE icon IS NULL AND description LIKE 'O Santander tem solu%';

UPDATE tb_news
SET icon = 'https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg'
WHERE icon IS NULL AND description LIKE 'Santander Seguro%';

UPDATE tb_news
SET icon = 'https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/insurance.svg'
WHERE icon IS NULL AND description LIKE 'Seguro de vida%';

-- The ETL's marketing message ("Olá <nome>, seu saldo de R$ X é excelente!") —
-- match by prefix so a real user-typed news item (description containing "Olá")
-- is not misattributed.
UPDATE tb_news
SET icon = 'https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg'
WHERE icon IS NULL AND description LIKE 'Olá % é excelente!%';
