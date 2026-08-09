package me.dio.service.exception;

/**
 * Thrown when an authenticated user tries to access a resource they do not own
 * (or a role they lack). Mapped to HTTP 403 by GlobalExceptionHandler.
 */
public class ForbiddenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ForbiddenException() {
        super("You do not have permission to access this resource.");
    }

    public ForbiddenException(String message) {
        super(message);
    }
}
