package me.dio.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple per-IP sliding-window rate limiter for write requests (POST).
 * A window of N seconds keeps at most {@code maxRequests} POSTs per IP; once
 * exceeded, the request is answered with 429 directly (no exception path, so
 * the GlobalExceptionHandler stays decoupled).
 *
 * In-memory only: limits reset on restart and don't survive multi-instance
 * deploys — fine for this scope. ponytail: a production setup would use a
 * shared store (Redis/bucket4j) and a real proxy/gateway limiter.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private final int maxRequests;
    private final long windowMillis;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public RateLimitInterceptor(int maxRequests, long windowSeconds) {
        this.maxRequests = maxRequests;
        this.windowMillis = windowSeconds * 1000L;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only writes are rate-limited; reads (GET) stay unlimited.
        if (HttpMethod.POST.matches(request.getMethod())) {
            if (isLimited(clientIp(request))) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Too many requests. Try again shortly.");
                return false;
            }
        }
        return true;
    }

    private boolean isLimited(String ip) {
        long now = System.currentTimeMillis();
        Deque<Long> window = this.hits.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() >= this.windowMillis) {
                window.pollFirst();
            }
            if (window.size() >= this.maxRequests) {
                return true;
            }
            window.addLast(now);
            return false;
        }
    }

    private String clientIp(HttpServletRequest request) {
        // Use X-Forwarded-For when behind a proxy (nginx), falling back to remote addr.
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
