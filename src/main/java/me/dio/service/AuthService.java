package me.dio.service;

import me.dio.config.JwtService;
import me.dio.controller.dto.AuthDtos.AuthResponse;
import me.dio.controller.dto.AuthDtos.LoginRequest;
import me.dio.controller.dto.AuthDtos.RefreshRequest;
import me.dio.controller.dto.AuthDtos.RegisterRequest;
import me.dio.domain.model.AuthUser;
import me.dio.domain.model.RefreshToken;
import me.dio.domain.repository.AuthUserRepository;
import me.dio.domain.repository.RefreshTokenRepository;
import me.dio.service.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Authentication: issues a short-lived access JWT plus an opaque rotating
 * refresh token. Each refresh rotates the token (old one is revoked); logout
 * revokes it too. The refresh token is stored hashed (SHA-256) so a DB leak
 * does not expose usable tokens.
 */
@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthUserRepository authUserRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       @Value("${auth.refresh-token-ms:604800000}") long refreshTokenMs) {
        this.authUserRepository = authUserRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenMs = refreshTokenMs;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException("Invalid username or password."));
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("Invalid username or password.");
        }
        return issuePair(user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (authUserRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already taken.");
        }
        AuthUser user = new AuthUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user = authUserRepository.save(user);
        return issuePair(user);
    }

    /**
     * Rotates a refresh token: revokes the presented one and issues a new pair.
     * Reuse of a revoked token is rejected (it was already consumed or logged out).
     */
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(request.refreshToken()))
                .orElseThrow(() -> new BusinessException("Invalid refresh token."));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Refresh token has expired or was revoked.");
        }
        stored.setRevoked(true); // rotate: consume the old token
        refreshTokenRepository.save(stored);
        return issuePair(stored.getAuthUser());
    }

    /** Revokes a refresh token (logout). Idempotent: unknown tokens are fine. */
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .ifPresent(stored -> {
                    stored.setRevoked(true);
                    refreshTokenRepository.save(stored);
                });
    }

    // ---- helpers ----

    private AuthResponse issuePair(AuthUser user) {
        String access = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getUserId());
        String refresh = generateOpaque();
        RefreshToken rt = new RefreshToken();
        rt.setAuthUser(user);
        rt.setTokenHash(hash(refresh));
        rt.setExpiresAt(LocalDateTime.now().plusNanos(refreshTokenMs * 1_000_000L));
        rt.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(rt);
        return new AuthResponse(access, refresh, user.getUsername(), user.getRole(), user.getUserId());
    }

    /** 32 random bytes, base64url-encoded (~43 chars). */
    private String generateOpaque() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 hex of a token, for storage/compare. */
    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
