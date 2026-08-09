package me.dio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global web configuration: CORS (allowed origin configurable) and the write
 * rate limiter (see rate-limit.* in application.yml). The limiter is backed by
 * Redis (shared across instances) with an in-memory fallback.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String allowedOrigins;
    private final boolean rateLimitEnabled;
    private final RedisRateLimiter rateLimiter;

    public WebConfig(
            @Value("${cors.allowed-origins:http://localhost:4200}") String allowedOrigins,
            @Value("${rate-limit.enabled:true}") boolean rateLimitEnabled,
            RedisRateLimiter rateLimiter) {
        this.allowedOrigins = allowedOrigins;
        this.rateLimitEnabled = rateLimitEnabled;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Disabled on the test profile (rate-limit.enabled=false) so MockMvc
        // suites are not throttled.
        if (rateLimitEnabled) {
            registry.addInterceptor(new RateLimitInterceptor(rateLimiter));
        }
    }
}
