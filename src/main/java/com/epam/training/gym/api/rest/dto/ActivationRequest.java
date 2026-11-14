package com.epam.training.gym.api.rest.dto;

import jakarta.validation.constraints.NotNull;

/** Activate/De-Activate Trainee/Trainer: the username is in the request path. */
public record ActivationRequest(@NotNull Boolean isActive) {
}
