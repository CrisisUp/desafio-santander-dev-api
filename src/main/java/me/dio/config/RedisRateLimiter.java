package me.dio.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Distributed fixed-window rate limiter backed by Redis. The check+increment is
 * atomic (Lua script), so concurrent instances share one counter — unlike the
 * old in-memory interceptor, a multi-pod deploy now enforces a single budget.
 *
 * Falls back to an in-memory limiter when Redis is unavailable (dev without
 * Redis, or a transient outage), so the API never fails closed.
 */
@Component
public class RedisRateLimiter {

    private final StringRedisTemplate redis;
    private final int maxRequests;
    private final long windowSeconds;
    private final boolean redisAvailable;
    private final InMemoryLimiter fallback;

    /** Atomic: increment the counter, set TTL on first hit, return the new count. */
    private static final String LUA_FIXED_WINDOW = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local ttl = tonumber(ARGV[2])
            local current = redis.call('INCR', key)
            if current == 1 then
                redis.call('EXPIRE', key, ttl)
            end
            return current
            """;

    private final DefaultRedisScript<Long> incrScript = new DefaultRedisScript<>(LUA_FIXED_WINDOW, Long.class);

    public RedisRateLimiter(StringRedisTemplate redis,
                            @org.springframework.beans.factory.annotation.Value("${rate-limit.max-requests:20}") int maxRequests,
                            @org.springframework.beans.factory.annotation.Value("${rate-limit.window-seconds:60}") long windowSeconds,
                            @org.springframework.beans.factory.annotation.Value("${rate-limit.redis-enabled:false}") boolean redisEnabled) {
        this.redis = redis;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
        this.redisAvailable = redisEnabled && redis != null;
        this.fallback = new InMemoryLimiter(maxRequests, windowSeconds);
    }

    /** True when the key may proceed; false when the limit is exceeded. */
    public boolean isAllowed(String key) {
        if (!redisAvailable) {
            return fallback.isAllowed(key);
        }
        try {
            Long count = redis.execute(incrScript, List.of("rl:" + key),
                    String.valueOf(maxRequests), String.valueOf(windowSeconds));
            return count == null || count <= maxRequests;
        } catch (Exception e) {
            // Redis down/outage: degrade to the in-memory limiter (fail open,
            // per-instance budget) rather than rejecting requests.
            return fallback.isAllowed(key);
        }
    }

    /**
     * Per-instance sliding-window fallback (the previous interceptor logic),
     * used only when Redis is unreachable.
     */
    private static class InMemoryLimiter {
        private final int max;
        private final long windowMillis;
        private final java.util.Map<String, java.util.ArrayDeque<Long>> hits =
                new java.util.concurrent.ConcurrentHashMap<>();

        InMemoryLimiter(int max, long windowSeconds) {
            this.max = max;
            this.windowMillis = windowSeconds * 1000L;
        }

        boolean isAllowed(String key) {
            long now = System.currentTimeMillis();
            java.util.ArrayDeque<Long> window = hits.computeIfAbsent(key, k -> new java.util.ArrayDeque<>());
            synchronized (window) {
                while (!window.isEmpty() && now - window.peekFirst() >= windowMillis) {
                    window.pollFirst();
                }
                if (window.size() >= max) {
                    return false;
                }
                window.addLast(now);
                return true;
            }
        }
    }
}
