# ADR 001: Pessimistic Lock Ordering for Transfers

## Status
Accepted

## Context
Account-to-account transfers must be safe under concurrency. Two concurrent transfers between the same accounts (A→B and B→A) can deadlock if locks are acquired in different orders. Additionally, two concurrent debits on the same source account must not both pass the funds check.

## Decision
- Use `SELECT ... FOR UPDATE` (`PESSIMISTIC_WRITE`) on both source and destination accounts.
- Always acquire locks in **ascending account ID order** (low ID first), then map source/destination by identity comparison.
- The funds check (`requireFunds`) runs while the lock is held, so the balance read is the latest committed value.

## Consequences
- No deadlocks on crossed transfers.
- No lost updates / overdrafts on concurrent debits.
- Slight contention on hot accounts (acceptable for this scope).

## References
- `TransactionServiceImpl.doTransfer()`
- `AccountRepository.findByIdForUpdate()`
- `ConcurrentTransferTest`