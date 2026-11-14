package com.epam.training.gym.api.rest.dto;

import com.epam.training.gym.model.TrainingTypeName;

import java.time.LocalDate;

/**  Trainer's Trainings List.
 * Search criteria (Period From, Period To, Trainer Name, Training Type) come as query parameters:
 * ?periodFrom=2026-01-01&periodTo=2026-03-31&trainerName=Anna&trainingType=YOGA
 * The controller builds TrainerTrainingCriteria from them, so no separate request DTO is needed.
 */
public record TrainerTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeName trainingType,
        int trainingDuration,
        String traineeName
) {
}
