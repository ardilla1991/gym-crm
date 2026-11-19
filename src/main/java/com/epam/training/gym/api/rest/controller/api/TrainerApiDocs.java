package com.epam.training.gym.api.rest.controller.api;

import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.model.auth.Credentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

/** Swagger documentation of TrainerController: kept apart so that the controller shows only routing and logic. */
@Tag(name = "Trainers", description = "Registration, profile and trainings of a trainer")
public interface TrainerApiDocs {

    @Operation(summary = "Register a trainer (item 2)",
            description = "No authentication. Returns the generated username and password.")
    @ApiResponse(responseCode = "201", description = "Trainer registered")
    @ApiResponse(responseCode = "400", description = "A name or the specialization is missing, or the specialization is unknown")
    @ApiResponse(responseCode = "409", description = "Another registration took the same username at the same moment: repeat the request")
    RegistrationResponse register(TrainerRegistrationRequest request);

    @Operation(summary = "Get a trainer profile (item 8)",
            description = "Any signed-in user can read any trainer profile.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The profile with the trainer's trainees")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "404", description = "No trainer with this username")
    TrainerProfileResponse getProfile(Credentials credentials, String username);

    @Operation(summary = "Update own trainer profile (item 9)",
            description = "Only the trainer can update their own profile. "
                    + "The username and the specialization (read only) do not change.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The updated profile with the trainer's trainees")
    @ApiResponse(responseCode = "400", description = "Invalid request: a required field is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    TrainerProfileResponse update(Credentials credentials, String username, UpdateTrainerRequest request);

    @Operation(summary = "Trainer trainings by criteria (item 13)",
            description = "Only the trainer can get their own trainings. All criteria are optional; dates are inclusive.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Trainings that match all given criteria")
    @ApiResponse(responseCode = "400", description = "Invalid date, or periodFrom is after periodTo")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    List<TrainerTrainingResponse> getTrainings(
            Credentials credentials,
            String username,
            @Parameter(description = "Trainings on or after this date, yyyy-MM-dd") LocalDate periodFrom,
            @Parameter(description = "Trainings on or before this date, yyyy-MM-dd") LocalDate periodTo,
            @Parameter(description = "First name or full name of the trainee, case-insensitive", example = "Emily Johnson") String traineeName);

    @Operation(summary = "Activate or deactivate own trainer profile (item 16)",
            description = "Only the trainer can change their own status. "
                    + "Not idempotent: sending the current status again returns 409.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "isActive is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    @ApiResponse(responseCode = "409", description = "The trainer already has this status")
    void changeActiveStatus(Credentials credentials, String username, ActivationRequest request);
}
