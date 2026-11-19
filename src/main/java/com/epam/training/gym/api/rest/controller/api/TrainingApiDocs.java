package com.epam.training.gym.api.rest.controller.api;

import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.dto.AddTrainingRequest;
import com.epam.training.gym.model.auth.Credentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Swagger documentation of TrainingController: kept apart so that the controller shows only routing and logic. */
@Tag(name = "Trainings", description = "Adding trainings")
@SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
public interface TrainingApiDocs {

    @Operation(summary = "Add a training (item 14)",
            description = "A trainee adds a training for themselves with any trainer. "
                    + "The training type is the trainer's specialization.")
    @ApiResponse(responseCode = "200", description = "Training added")
    @ApiResponse(responseCode = "400", description = "Invalid request: a required field is missing or the duration is not positive")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "traineeUsername is not the signed-in user")
    @ApiResponse(responseCode = "404", description = "The trainer does not exist")
    @ApiResponse(responseCode = "409", description = "The trainer is not active")
    void add(Credentials credentials, AddTrainingRequest request);
}
