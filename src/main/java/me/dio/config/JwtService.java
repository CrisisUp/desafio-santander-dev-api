package me.dio.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT generation/validation. HS256 with a configurable secret
 * (auth.jwt-secret), defaulted for dev; override in production.
 *
 * The access token is short-lived (auth.access-token-ms, default 15min); the
 * refresh token is a separate opaque string handled by AuthService, so a
 * leaked access token has a small window of validity.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMillis;

    public JwtService(@Value("${auth.jwt-secret}") String secret,
                      @Value("${auth.access-token-ms:900000}") long expirationMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(Long authUserId, String username, String role, Long domainUserId) {
        Date now = new Date();
        return Jwts.builder()
                .subject(username)
                .claim("authUserId", authUserId)
                .claim("role", role)
                .claim("userId", domainUserId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
