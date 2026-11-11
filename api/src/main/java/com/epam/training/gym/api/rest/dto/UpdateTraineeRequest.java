package com.epam.training.gym.api.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

/** Update Trainee Profile: the username is in the request path. */
public record UpdateTraineeRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Past LocalDate dateOfBirth,
        String address,
        @NotNull Boolean isActive) {
}
