package com.epam.training.gym.api.rest.controller.api;

import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.auth.Credentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.List;

/** Swagger documentation of TraineeController: kept apart so that the controller shows only routing and logic. */
@Tag(name = "Trainees", description = "Registration, profile, trainers and trainings of a trainee")
public interface TraineeApiDocs {

    @Operation(summary = "Register a trainee (item 1)",
            description = "No authentication. Returns the generated username and password.")
    @ApiResponse(responseCode = "201", description = "Trainee registered")
    @ApiResponse(responseCode = "400", description = "First or last name is missing, or the date of birth is not in the past")
    @ApiResponse(responseCode = "409", description = "Another registration took the same username at the same moment: repeat the request")
    RegistrationResponse register(TraineeRegistrationRequest request);

    @Operation(summary = "Get a trainee profile (item 5)",
            description = "Any signed-in user can read any trainee profile.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The profile with the trainee's trainers")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "404", description = "No trainee with this username")
    TraineeProfileResponse getProfile(Credentials credentials, String username);

    @Operation(summary = "Update own trainee profile (item 6)",
            description = "Only the trainee can update their own profile. The username does not change.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The updated profile with the trainee's trainers")
    @ApiResponse(responseCode = "400", description = "Invalid request: a required field is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    TraineeProfileResponse update(Credentials credentials, String username, UpdateTraineeRequest request);

    @Operation(summary = "Delete own trainee profile (item 7)",
            description = "Only the trainee can delete their own profile. The trainee's trainings are deleted too.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Profile deleted")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    void delete(Credentials credentials, String username);

    @Operation(summary = "Active trainers not assigned to the trainee (item 10)",
            description = "Only the trainee can get their own list.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Active trainers the trainee does not have yet")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    List<TrainerSummary> getUnassignedTrainers(Credentials credentials, String username);

    @Operation(summary = "Replace the trainee's trainers list (item 11)",
            description = "Only the trainee can change their own list. An empty list removes all trainers.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The trainee's new list of trainers")
    @ApiResponse(responseCode = "400", description = "The list is missing or has a blank username")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    @ApiResponse(responseCode = "404", description = "One of the trainers does not exist")
    List<TrainerSummary> updateTrainers(Credentials credentials, String username, UpdateTraineeTrainersRequest request);

    @Operation(summary = "Trainee trainings by criteria (item 12)",
            description = "Only the trainee can get their own trainings. All criteria are optional; dates are inclusive.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Trainings that match all given criteria")
    @ApiResponse(responseCode = "400", description = "Invalid date or training type")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    List<TraineeTrainingResponse> getTrainings(
            Credentials credentials,
            String username,
            @Parameter(description = "Trainings on or after this date, yyyy-MM-dd") LocalDate periodFrom,
            @Parameter(description = "Trainings on or before this date, yyyy-MM-dd") LocalDate periodTo,
            @Parameter(description = "First name of the trainer, case-insensitive", example = "David") String trainerName,
            @Parameter(description = "Training type of the training") TrainingTypeName trainingType);

    @Operation(summary = "Activate or deactivate own trainee profile (item 15)",
            description = "Only the trainee can change their own status. "
                    + "Not idempotent: sending the current status again returns 409.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "isActive is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    @ApiResponse(responseCode = "409", description = "The trainee already has this status")
    void changeActiveStatus(Credentials credentials, String username, ActivationRequest request);
}
