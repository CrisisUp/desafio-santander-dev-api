# ADR 005: Rate Limiting

## Status
Accepted (with known limitation)

## Context
Write endpoints (POST) must be protected against abuse.

## Decision
- In-memory sliding-window interceptor (`RateLimitInterceptor`): max 20 POSTs per 60s per IP.
- Configured via `rate-limit.max-requests` and `rate-limit.window-seconds` (default 20/60).
- Applied only to POST; GET unlimited.
- `X-Forwarded-For` respected for proxy deployments.

## Known Limitation
- **Not distributed**: each JVM instance has its own counter. In a multi-pod deployment, limits are per-instance.
- **Production upgrade path**: Redis-backed rate limiter (e.g., Bucket4j + Redis) or API Gateway (nginx rate limit, Kong, etc.).

## References
- `RateLimitInterceptor`
- `WebConfig.addInterceptors()`
- `application.yml` rate-limit section