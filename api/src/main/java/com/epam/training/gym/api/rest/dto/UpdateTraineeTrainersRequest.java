package com.epam.training.gym.api.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Update Trainee's Trainer List: the trainee username is in the request path. */
public record UpdateTraineeTrainersRequest(@NotNull List<@NotBlank String> trainerUsernames) {
}
