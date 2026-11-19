package com.epam.training.gym.api.rest.dto;

import com.epam.training.gym.model.auth.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Change Login: username and old password come in the Authorization header. */
public record ChangePasswordRequest(
        @NotBlank
        @Size(min = PasswordPolicy.LENGTH, max = PasswordPolicy.LENGTH,
                message = "must contain exactly {min} characters")
        String newPassword) {
}
