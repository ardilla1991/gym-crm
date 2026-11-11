package com.epam.training.gym.api.rest.dto;

import com.epam.training.gym.domain.TrainingTypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Trainer registration. */
public record TrainerRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull TrainingTypeName specialization) {
}
