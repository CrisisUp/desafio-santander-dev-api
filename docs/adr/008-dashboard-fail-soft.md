# ADR 008: Dashboard Fail-Soft on Stats Error

## Status
Accepted

## Context
Dashboard loads two independent streams: user list (for KPIs) + transaction stats (for chart). If stats fail, the KPIs should still render.

## Decision
- `combineLatest` + per-stream `catchError` → if stats fail, `stats` is `null`; dashboard renders zero-filled chart (`computeTransactionStats([])`).
- Alternative (stricter): fail the whole view if any stream errors.

## Rationale
- KPIs (total accounts, balance sum) are higher-value than the chart.
- Partial render is better than blank screen.

## References
- `DashboardComponent` reload pipeline (ponytail on line 47)
- `computeTransactionStats([])` returns zero-filled buckets