package me.dio.config;

/**
 * Authenticated principal placed in the SecurityContext by JwtAuthenticationFilter.
 * {@code userId} is the banking domain user id (tb_user.id), used to populate
 * created_by on transactions and the audit actor. It may be null when the
 * auth user is not linked to a banking user.
 */
public record AuthenticatedUser(String username, Long userId) {
}
