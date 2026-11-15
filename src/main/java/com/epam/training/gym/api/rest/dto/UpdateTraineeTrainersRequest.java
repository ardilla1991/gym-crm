package com.epam.training.gym.api.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Update Trainee's Trainer List: the trainee username is in the request path. */
public record UpdateTraineeTrainersRequest(@NotEmpty List<@NotBlank String> trainerUsernames) {
}
