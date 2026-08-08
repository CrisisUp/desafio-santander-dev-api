package me.dio.service;

import me.dio.controller.dto.UniquenessCheckDto;
import me.dio.domain.model.User;

public interface UserService extends CrudService<Long, User> {

    /**
     * Checks whether an account/card number is still free, excluding the user
     * being edited (so an unchanged update doesn't collide with itself).
     * Blank values are reported as available (the form doesn't call for them).
     */
    UniquenessCheckDto checkUniqueness(String accountNumber, String cardNumber, Long excludeId);
}
