package com.epam.training.gym.model.criteria;

import lombok.Builder;

import java.time.LocalDate;

/**
 * Optional filters for a trainer's trainings. A null field means "do not filter by it"; a blank name is the same as null.
 * Dates are inclusive. traineeName matches the trainee's first name or full name ("Emily" or "Emily Johnson"), case-insensitive.
 */
@Builder
public record TrainerTrainingCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName) {

    public TrainerTrainingCriteria {
        traineeName = traineeName == null || traineeName.isBlank() ? null : traineeName.strip();
    }
}
