# ADR 010: FK on destination_account_id Not Validated by Hibernate

## Status
Accepted

## Context
`Transaction.destinationAccountId` is mapped as a plain `Long` (no `@ManyToOne`). V9 added DB-level FK `fk_tb_transaction_destination`.

## Decision
- Hibernate `ddl-auto: validate` checks **columns and types**, not FKs on non-association fields.
- The migration (V9) is the single source of truth for this FK.
- Documented in `application-prd.yml` and `Transaction.java` comment.

## Consequences
- On PostgreSQL (prod), `validate` passes even if FK missing — rely on Flyway history.
- If `@ManyToOne` is desired later, create new migration (V12+) to add the association mapping.

## References
- `Transaction.destinationAccountId` (comment updated)
- `V9__add_transaction_destination_fk.sql`
- `application-prd.yml` (note on validate FK gap)