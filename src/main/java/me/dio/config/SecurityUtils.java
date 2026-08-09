package me.dio.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Read the authenticated actor (if any) from the SecurityContext. Null when the
 * request is unauthenticated.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static AuthenticatedUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        return null;
    }

    /** True when the current actor is an ADMIN. */
    public static boolean isAdmin() {
        AuthenticatedUser user = currentUser();
        return user != null && user.isAdmin();
    }

    /**
     * True when the current actor owns the given banking user id (i.e. their
     * token's userId matches), or is an ADMIN.
     */
    public static boolean isOwnerOrAdmin(Long domainUserId) {
        if (isAdmin()) {
            return true;
        }
        AuthenticatedUser user = currentUser();
        return user != null && user.userId() != null && user.userId().equals(domainUserId);
    }
}
