package com.epam.training.gym.api.rest.dto;

import com.epam.training.gym.model.validation.FieldLimits;
import com.epam.training.gym.model.validation.PersonName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/** Update Trainer Profile: the username is in the request path; the specialization is read-only. */
public record UpdateTrainerRequest(
        // @PersonName validates; @Schema shows the same rule in Swagger, which does not read composed annotations
        @PersonName
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = FieldLimits.NAME_MAX,
                pattern = FieldLimits.NAME_PATTERN, example = "Anna")
        String firstName,
        @PersonName
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = FieldLimits.NAME_MAX,
                pattern = FieldLimits.NAME_PATTERN, example = "Lee")
        String lastName,
        @NotNull Boolean isActive) {
}
