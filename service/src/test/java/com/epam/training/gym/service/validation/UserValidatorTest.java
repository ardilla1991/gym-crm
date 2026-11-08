package com.epam.training.gym.service.validation;

import com.epam.training.gym.domain.Trainee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserValidatorTest {
    private final UserValidator userValidator = new UserValidator();

    @Test
    void validate_namesPresent_passes() {
        assertDoesNotThrow(() -> userValidator.validate(user("John", "Smith")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validate_blankFirstName_throws(String firstName) {
        assertThrows(IllegalArgumentException.class, () -> userValidator.validate(user(firstName, "Smith")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validate_blankLastName_throws(String lastName) {
        assertThrows(IllegalArgumentException.class, () -> userValidator.validate(user("John", lastName)));
    }

    private static Trainee user(String firstName, String lastName) {
        return Trainee.builder().firstName(firstName).lastName(lastName).build();
    }
}
