package me.dio.controller.dto;

/**
 * Result of the uniqueness check used by the user form's async validators.
 * A field is reported as "available" when the value is not in use by another
 * user (or was left blank, in which case the form doesn't call us for it).
 */
public record UniquenessCheckDto(boolean accountNumberAvailable, boolean cardNumberAvailable) {
}
