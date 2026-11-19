package com.epam.training.gym.model.criteria;

import com.epam.training.gym.model.TrainingTypeName;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Optional filters for a trainee's trainings. A null field means "do not filter by it"; a blank name is the same as null.
 * Dates are inclusive. trainerName matches the trainer's first name or full name ("David" or "David Miller"), case-insensitive.
 */
@Builder
public record TraineeTrainingCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingTypeName trainingType
) {

    public TraineeTrainingCriteria {
        trainerName = trainerName == null || trainerName.isBlank() ? null : trainerName.strip();
    }
}
