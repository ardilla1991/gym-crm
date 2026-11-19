package com.epam.training.gym.api.rest.controller.api;

import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.dto.TrainingTypeResponse;
import com.epam.training.gym.model.auth.Credentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/** Swagger documentation of TrainingTypeController: kept apart so that the controller shows only routing and logic. */
@Tag(name = "Training types", description = "Reference list of training types")
@SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
public interface TrainingTypeApiDocs {

    @Operation(summary = "List training types (item 17)",
            description = "Available to any signed-in user. Ordered by name.")
    @ApiResponse(responseCode = "200", description = "Id and name of every training type")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    List<TrainingTypeResponse> getAll(Credentials credentials);
}
