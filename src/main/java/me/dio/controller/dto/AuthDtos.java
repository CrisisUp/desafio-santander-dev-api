package me.dio.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request/response payloads for the auth endpoints.
 */
public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {}

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Size(min = 6, max = 100) String password) {}

    public record RefreshRequest(
            @NotBlank String refreshToken) {}

    public record LogoutRequest(
            @NotBlank String refreshToken) {}

    public record AuthResponse(
            String token,
            String refreshToken,
            String username,
            String role,
            Long userId) {}
}
