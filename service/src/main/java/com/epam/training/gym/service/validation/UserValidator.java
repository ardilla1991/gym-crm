package com.epam.training.gym.service.validation;

import com.epam.training.gym.domain.User;
import org.springframework.stereotype.Component;

/** Checks the mandatory profile fields before create and update. */
@Component
public class UserValidator {
    public void validate(User user) {
        requireNotBlank(user.getFirstName(), "First name");
        requireNotBlank(user.getLastName(), "Last name");
    }

    private static void requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
