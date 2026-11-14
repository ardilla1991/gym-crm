package com.epam.training.gym.api.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/** Add Training: the training type is the trainer's specialization. */
public record AddTrainingRequest(
        @NotBlank String traineeUsername,
        @NotBlank String trainerUsername,
        @NotBlank String trainingName,
        @NotNull LocalDate trainingDate,
        @NotNull @Positive Integer trainingDuration) {
}
