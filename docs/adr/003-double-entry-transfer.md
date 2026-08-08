# ADR 003: Double-Entry Transfer Ledger

## Status
Accepted

## Context
A transfer moves money between two accounts. The ledger must reflect both sides for audit and statement rendering.

## Decision
One transfer = **two transaction rows** in the same DB transaction:
- **Debit leg** (source): `type=TRANSFER`, `credit=false`, `destination_account_id = dest.id`, amount negative in statement.
- **Credit leg** (destination): `type=TRANSFER`, `credit=true`, `destination_account_id = src.id`, amount positive in statement.
- `created_at` identical for both legs (same `LocalDateTime.now()`).
- Aggregate endpoint (`/accounts/transactions/summary`) counts TRANSFER **once** (debit leg only) via query filter `WHERE (type <> TRANSFER OR credit = false)`.

## Consequences
- Statement shows "Para conta #X" (debit) and "De conta #X" (credit).
- Balance derived from sum of signed amounts matches double-entry accounting.
- No orphan legs: both written in same unit, or neither.

## References
- `TransactionServiceImpl.doTransfer()`
- `TransactionRepository.summarizeByType()`
- `Transaction.credit` column (V7)