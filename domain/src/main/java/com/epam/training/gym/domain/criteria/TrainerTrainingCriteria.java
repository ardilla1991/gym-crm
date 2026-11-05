package com.epam.training.gym.domain.criteria;

import lombok.Builder;

import java.time.LocalDate;

/**
 * Optional filters for a trainer's trainings. A null field means "do not filter by it".
 * Dates are inclusive. traineeName is compared with the trainee's first name, case-insensitive.
 */
@Builder
public record TrainerTrainingCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName) {
}
