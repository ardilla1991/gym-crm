package com.epam.training.gym.service.validation;

import com.epam.training.gym.service.exception.InvalidInputException;

import java.time.LocalDate;

/** The training search period: both dates are optional and inclusive, but from must not be after to. */
public final class TrainingPeriodValidator {

    private TrainingPeriodValidator() {
    }

    /**
     * @throws InvalidInputException if both dates are given and from is after to
     */
    public static void validate(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidInputException("periodFrom must not be after periodTo");
        }
    }
}
