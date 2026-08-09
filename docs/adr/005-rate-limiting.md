# ADR 005: Rate Limiting

## Status
Accepted (evolved: distributed via Redis)

## Context
Write endpoints (POST) must be protected against abuse, including across
multiple API instances.

## Decision
- **Distributed limiter** (`RedisRateLimiter`) backed by Redis: fixed-window,
  atomic INCR+EXPIRE via a Lua script. The counter lives in Redis, so all
  instances share a single budget.
- Default `max-requests: 20` per `window-seconds: 60` per IP.
- Applied only to POST; GET unlimited.
- `X-Forwarded-For` respected for proxy deployments.
- **Fallback**: when Redis is unreachable (`redis-enabled=false` in dev/test,
  or a Redis outage in prod), the limiter degrades to a per-instance in-memory
  budget (fail-open) rather than rejecting requests.
- Enabled via `rate-limit.redis-enabled` (prod sets `true` + `spring.data.redis.*`).

## Consequences
- Multi-pod deployments enforce a single, shared rate limit.
- A Redis outage does not take the API down; it temporarily falls back to
  per-instance limits (documented trade-off: slightly weaker limiting, never a
  full failure).

## References
- `RedisRateLimiter` (Lua fixed window)
- `RateLimitInterceptor`
- `WebConfig.addInterceptors()`
- `docker-compose.prd.yml` (redis service)
- `application.yml` / `application-prd.yml` rate-limit + spring.data.redis