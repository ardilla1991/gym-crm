package com.epam.training.gym.api.rest.dto;

import jakarta.validation.constraints.NotBlank;

/** Change Login: username and old password come in the Authorization header. */
public record ChangePasswordRequest(@NotBlank String newPassword) {
}
