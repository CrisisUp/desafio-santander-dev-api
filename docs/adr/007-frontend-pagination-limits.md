# ADR 007: Frontend Pagination Limits (100 Users)

## Status
Accepted (known ceiling)

## Context
Two frontend components fetch users for dropdowns/KPIs:
- `TransactionListComponent.loadAccountOptions()` — transfer destination dropdown.
- `DashboardComponent` — KPI cards + balance distribution chart.

Both call `UserService.list(0, 100)` (first page, size 100).

## Decision
- Current seed has 41 users → 100 is sufficient today.
- **Ceiling**: if users > 100, dropdown and charts silently miss data.
- No pagination loop implemented (would require multiple requests and client-side merge).

## Upgrade Path
- Add dedicated `GET /accounts/options` endpoint returning all accounts (id + number + holder) for the transfer dropdown.
- For dashboard: move aggregation to backend (`GET /accounts/stats`) or implement paginated fetch until exhausted.

## References
- `TransactionListComponent.loadAccountOptions()` (ponytail)
- `DashboardComponent` reload pipeline (ponytail)
- `UserService.list(page, size)`