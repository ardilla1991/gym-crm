package com.epam.training.gym.api.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Update Trainer Profile: the username is in the request path; the specialization is read-only. */
public record UpdateTrainerRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Boolean isActive) {
}
