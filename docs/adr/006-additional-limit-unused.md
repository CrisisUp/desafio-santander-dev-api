# ADR 006: Additional Limit (Cheque Especial) Not Enforced

## Status
Accepted (deferred)

## Context
`Account` has `additional_limit` column (DB) and `limit` field (JPA/DTO). The frontend shows it. However, the funds check only compares against `balance`.

## Decision
- Current rule: **only `balance` counts**; `additional_limit` is decorative.
- `TransactionServiceImpl.requireFunds()` throws `InsufficientFunds` if `balance < amount`.
- Frontend `balanceValidator` mirrors this (warns before submit, but backend is source of truth).

## Upgrade Path
If "cheque especial" is desired:
1. Update `requireFunds` to allow `balance + additional_limit >= amount`.
2. Update frontend `balanceValidator` to use `account.balance + account.limit`.
3. Consider whether overdraft should create a separate "OVERDRAFT" transaction type or just a negative balance.

## References
- `TransactionServiceImpl.requireFunds()` (ponytail comment)
- `TransactionListComponent.balanceValidator()`
- `Account.limit` field