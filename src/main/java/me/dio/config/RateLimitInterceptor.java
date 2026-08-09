package me.dio.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Per-IP rate limiter for write requests (POST), backed by {@link RedisRateLimiter}
 * so the budget is shared across instances (distributed). When Redis is
 * unavailable the limiter degrades to a per-instance in-memory budget.
 * A window of N seconds keeps at most {@code maxRequests} POSTs per IP; once
 * exceeded, the request is answered with 429 directly (no exception path, so
 * the GlobalExceptionHandler stays decoupled).
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisRateLimiter rateLimiter;

    public RateLimitInterceptor(RedisRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Only writes are rate-limited; reads (GET) stay unlimited.
        if (HttpMethod.POST.matches(request.getMethod())) {
            if (!rateLimiter.isAllowed(clientIp(request))) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("Too many requests. Try again shortly.");
                return false;
            }
        }
        return true;
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
