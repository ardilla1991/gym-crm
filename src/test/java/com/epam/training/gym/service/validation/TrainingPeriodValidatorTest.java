package com.epam.training.gym.service.validation;

import com.epam.training.gym.service.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TrainingPeriodValidatorTest {

    private static final LocalDate MAY_1 = LocalDate.of(2026, 5, 1);
    private static final LocalDate MAY_2 = LocalDate.of(2026, 5, 2);

    @Test
    void validate_fromAfterTo_throws() {
        InvalidInputException exception =
                assertThrows(InvalidInputException.class, () -> TrainingPeriodValidator.validate(MAY_2, MAY_1));

        assertEquals("periodFrom must not be after periodTo", exception.getMessage());
    }

    @Test
    void validate_sameDay_passes() {
        assertDoesNotThrow(() -> TrainingPeriodValidator.validate(MAY_1, MAY_1));
    }

    @Test
    void validate_oneOrNoDate_passes() {
        assertDoesNotThrow(() -> TrainingPeriodValidator.validate(MAY_2, null));
        assertDoesNotThrow(() -> TrainingPeriodValidator.validate(null, MAY_1));
        assertDoesNotThrow(() -> TrainingPeriodValidator.validate(null, null));
    }
}
