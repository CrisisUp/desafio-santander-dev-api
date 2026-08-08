# ADR 004: Seed Data Strategy

## Status
Accepted

## Context
The API needs realistic data for demos and tests. Seeds must be reproducible and versioned.

## Decision
- All seeds via Flyway migrations (V2–V6):
  - V2: protected user "Devweekerson" (ID 1) + account + card + 5 features + 2 news.
  - V4: 2 historical transactions for account 1.
  - V5: 40 complete users (IDs 2–41) with accounts, cards, features, news.
  - V6: 2–3 transactions per account (41 deposits, 40 payments, 21 withdrawals).
- Seeds are **immutable**; corrections happen via new migrations (V10, V11).
- V6 had 8 accounts with out-of-order dates (WITHDRAWAL backdated); fixed by V11.

## Consequences
- `ddl-auto: validate` passes on H2 and PostgreSQL.
- Tests assert exact seed totals (41 deposits, 40 payments, 21 withdrawals).
- Dev database persists across restarts (`./data/sdw2023.mv.db`).

## References
- `src/main/resources/db/migration/V2__seed_user_1.sql` through `V6__seed_transactions_accounts.sql`
- `V10__fix_etl_news_icon.sql`, `V11__fix_seed_transaction_dates.sql`