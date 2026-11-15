package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.auth.AccessChecker;
import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.api.rest.exception.AccessDeniedException;
import com.epam.training.gym.api.rest.mapper.TraineeMapper;
import com.epam.training.gym.api.rest.mapper.TrainerMapper;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidStateException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Trainee endpoints: registration, profile, trainers and trainings of a trainee. */
@RestController
@RequestMapping(ApiPaths.TRAINEES)
@Tag(name = "Trainees", description = "Registration, profile, trainers and trainings of a trainee")
public class TraineeController {

    private final GymFacade facade;

    public TraineeController(GymFacade facade) {
        this.facade = facade;
    }

    /**
     * Task item 1. Trainee registration; no authentication required.
     *
     * @param request first and last name (required), date of birth and address (optional)
     * @return the generated username and password of the new trainee
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a trainee (item 1)",
            description = "No authentication. Returns the generated username and password.")
    @ApiResponse(responseCode = "201", description = "Trainee registered")
    @ApiResponse(responseCode = "400", description = "First or last name is missing, or the date of birth is not in the past")
    @ApiResponse(responseCode = "409", description = "Another registration took the same username at the same moment: repeat the request")
    public RegistrationResponse register(@Valid @RequestBody TraineeRegistrationRequest request) {
        Trainee created = facade.createTrainee(TraineeMapper.toModel(request));
        return new RegistrationResponse(created.getUser().getUsername(), created.getUser().getPassword());
    }

    /**
     * Task item 5. Get Trainee Profile: any signed-in user can read any trainee profile.
     *
     * @param credentials the signed-in user, from the Authorization header
     * @param username    the trainee whose profile is read
     * @return the profile with the list of the trainee's trainers
     * @throws AuthenticationException if the username or password is wrong
     * @throws EntityNotFoundException if there is no trainee with this username
     */
    @GetMapping("/{username}")
    @Operation(summary = "Get a trainee profile (item 5)",
            description = "Any signed-in user can read any trainee profile.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The profile with the trainee's trainers")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "404", description = "No trainee with this username")

    public TraineeProfileResponse getProfile(Credentials credentials,
                                             @PathVariable("username") String username) {
        return TraineeMapper.toProfileResponse(facade.getTraineeProfile(credentials, username));
    }

    /**
     * Task item 6. Update Trainee Profile: only the trainee can update their own profile.
     * The username does not change.
     *
     * @param credentials the signed-in trainee, from the Authorization header
     * @param username    the trainee whose profile is updated; must be the signed-in user
     * @param request     new names, date of birth, address and active status
     * @return the updated profile with the list of the trainee's trainers
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     */
    @PutMapping("/{username}")
    @Operation(summary = "Update own trainee profile (item 6)",
            description = "Only the trainee can update their own profile. The username does not change.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The updated profile with the trainee's trainers")
    @ApiResponse(responseCode = "400", description = "Invalid request: a required field is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    public TraineeProfileResponse update(Credentials credentials,
                                         @PathVariable("username") String username,
                                         @Valid @RequestBody UpdateTraineeRequest request) {
        AccessChecker.requireOwner(credentials, username);
        Trainee updated = facade.updateTrainee(credentials, TraineeMapper.toModel(request));

        return TraineeMapper.toProfileResponse(updated);
    }

    /**
     * Task item 7. Delete Trainee Profile: only the trainee can delete their own profile.
     * The trainee's trainings are deleted too (cascade).
     *
     * @param credentials the signed-in trainee, from the Authorization header
     * @param username    the trainee to delete; must be the signed-in user
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     */
    @DeleteMapping("/{username}")
    @Operation(summary = "Delete own trainee profile (item 7)",
            description = "Only the trainee can delete their own profile. The trainee's trainings are deleted too.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Profile deleted")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    public void delete(Credentials credentials, @PathVariable("username") String username) {
        AccessChecker.requireOwner(credentials, username);
        facade.deleteTrainee(credentials);
    }

    /**
     * Task item 10. Active trainers that are not assigned to the trainee yet;
     * only the trainee can get their own list.
     *
     * @param credentials the signed-in trainee, from the Authorization header
     * @param username    the trainee; must be the signed-in user
     * @return username, names and specialization of each trainer
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     */
    @GetMapping("/{username}/unassigned-trainers")
    @Operation(summary = "Active trainers not assigned to the trainee (item 10)",
            description = "Only the trainee can get their own list.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Active trainers the trainee does not have yet")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    public List<TrainerSummary> getUnassignedTrainers(Credentials credentials,
                                                      @PathVariable("username") String username) {
        AccessChecker.requireOwner(credentials, username);
        return TrainerMapper.toSummaries(facade.getUnassignedTrainers(credentials));
    }

    /**
     * Task item 11. Update Trainee's Trainer List: replaces the whole list with the given trainers;
     * only the trainee can change their own list.
     *
     * @param credentials the signed-in trainee, from the Authorization header
     * @param username    the trainee; must be the signed-in user
     * @param request     usernames of all trainers the trainee should have; an empty list removes all
     * @return the trainee's new list of trainers
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     * @throws EntityNotFoundException if one of the trainers does not exist
     */
    @PutMapping("/{username}/trainers")
    @Operation(summary = "Replace the trainee's trainers list (item 11)",
            description = "Only the trainee can change their own list. An empty list removes all trainers.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The trainee's new list of trainers")
    @ApiResponse(responseCode = "400", description = "The list is missing or has a blank username")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    @ApiResponse(responseCode = "404", description = "One of the trainers does not exist")
    public List<TrainerSummary> updateTrainers(Credentials credentials,
                                               @PathVariable("username") String username,
                                               @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        AccessChecker.requireOwner(credentials, username);
        return TrainerMapper.toSummaries(facade.updateTraineeTrainers(credentials, request.trainerUsernames()));
    }

    /**
     * Task item 12. Get Trainee Trainings List: all criteria are optional query parameters;
     * only the trainee can get their own trainings.
     *
     * @param credentials  the signed-in trainee, from the Authorization header
     * @param username     the trainee; must be the signed-in user
     * @param periodFrom   trainings on or after this date, ISO format: 2026-01-31
     * @param periodTo     trainings on or before this date, ISO format: 2026-01-31
     * @param trainerName  the trainer's first name
     * @param trainingType the training type
     * @return the trainee's trainings that match all given criteria
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     */
    @GetMapping("/{username}/trainings")
    @Operation(summary = "Trainee trainings by criteria (item 12)",
            description = "Only the trainee can get their own trainings. All criteria are optional; dates are inclusive.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Trainings that match all given criteria")
    @ApiResponse(responseCode = "400", description = "Invalid date or training type")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    public List<TraineeTrainingResponse> getTrainings(
            Credentials credentials,
            @PathVariable("username") String username,
            @RequestParam(name = "periodFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(name = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(name = "trainerName", required = false) String trainerName,
            @RequestParam(name = "trainingType", required = false) TrainingTypeName trainingType) {
        AccessChecker.requireOwner(credentials, username);
        var criteria = new TraineeTrainingCriteria(periodFrom, periodTo, trainerName, trainingType);

        return TrainingMapper.toTraineeTrainingResponses(facade.getTraineeTrainings(credentials, criteria));
    }

    /**
     * Task item 15. Activate/De-Activate Trainee: only the trainee can change their own status.
     * Not idempotent: setting the status the trainee already has is an error.
     *
     * @param credentials the signed-in trainee, from the Authorization header
     * @param username    the trainee; must be the signed-in user
     * @param request     the new active status
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     * @throws InvalidStateException   if the trainee already has this status
     */
    @PatchMapping("/{username}/active")
    @Operation(summary = "Activate or deactivate own trainee profile (item 15)",
            description = "Only the trainee can change their own status. "
                    + "Not idempotent: sending the current status again returns 409.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "isActive is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    @ApiResponse(responseCode = "409", description = "The trainee already has this status")
    public void changeActiveStatus(Credentials credentials,
                                   @PathVariable("username") String username,
                                   @Valid @RequestBody ActivationRequest request) {
        AccessChecker.requireOwner(credentials, username);
        facade.changeTraineeActiveStatus(credentials, request.isActive());
    }

}
