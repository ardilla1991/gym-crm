package com.epam.training.gym.model.criteria;

import com.epam.training.gym.model.TrainingTypeName;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Optional filters for a trainee's trainings. A null field means "do not filter by it".
 * Dates are inclusive. trainerName is compared with the trainer's first name, case-insensitive.
 */
@Builder
public record TraineeTrainingCriteria(
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        TrainingTypeName trainingType
) {

}
