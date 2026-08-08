# ADR 002: Idempotency Key Design

## Status
Accepted

## Context
Clients may retry requests (network flakiness, double-click). A retry must not debit twice.

## Decision
- Client sends `Idempotency-Key` header (opaque string, max 64 chars; frontend generates UUID via `crypto.randomUUID()`).
- Server stores key on the transaction row; uniqueness enforced by DB constraint `UNIQUE(account_id, idempotency_key)` (V8).
- On retry with same key: return the original transaction (no new row, no balance change).
- For TRANSFER: same key written on both debit and credit legs (per-account uniqueness, so no collision).

## Consequences
- Safe retries for all transaction types.
- Key scoped per account (different accounts may reuse the same key).
- Race on concurrent retries handled by catching `DataIntegrityViolationException` and re-fetching the winner.

## References
- `TransactionServiceImpl.create()`
- `TransactionRepository.findByAccount_IdAndIdempotencyKey()`
- `V8__add_transaction_idempotency_key.sql`
- Frontend: `TransactionListComponent.currentIdempotencyKey`