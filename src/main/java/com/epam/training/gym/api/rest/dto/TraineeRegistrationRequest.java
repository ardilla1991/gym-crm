package com.epam.training.gym.api.rest.dto;

import com.epam.training.gym.model.validation.FieldLimits;
import com.epam.training.gym.model.validation.PersonName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Trainee registration request. */
public record TraineeRegistrationRequest(
        // @PersonName validates; @Schema shows the same rule in Swagger, which does not read composed annotations
        @PersonName
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = FieldLimits.NAME_MAX,
                pattern = FieldLimits.NAME_PATTERN, example = "Anna")
        String firstName,
        @PersonName
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = FieldLimits.NAME_MAX,
                pattern = FieldLimits.NAME_PATTERN, example = "Lee")
        String lastName,
        @Past @Schema(example = "1998-06-21", description = "Must be in the past") LocalDate dateOfBirth,
        @Size(max = FieldLimits.ADDRESS_MAX) @Schema(example = "7 Elm Street, Denver") String address) {
}
