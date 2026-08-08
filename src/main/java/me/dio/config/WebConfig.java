package me.dio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global web configuration: CORS (allowed origin configurable) and the write
 * rate limiter (see rate-limit.* in application.yml).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String allowedOrigins;
    private final int rateLimitMax;
    private final long rateLimitWindowSeconds;

    public WebConfig(
            @Value("${cors.allowed-origins:http://localhost:4200}") String allowedOrigins,
            @Value("${rate-limit.max-requests:20}") int rateLimitMax,
            @Value("${rate-limit.window-seconds:60}") long rateLimitWindowSeconds) {
        this.allowedOrigins = allowedOrigins;
        this.rateLimitMax = rateLimitMax;
        this.rateLimitWindowSeconds = rateLimitWindowSeconds;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimitMax, rateLimitWindowSeconds));
    }
}
