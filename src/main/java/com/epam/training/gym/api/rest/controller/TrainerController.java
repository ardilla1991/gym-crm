package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.auth.AccessChecker;
import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.api.rest.exception.AccessDeniedException;
import com.epam.training.gym.api.rest.mapper.TrainerMapper;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
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

/** Trainer endpoints: registration, profile and trainings of a trainer. */
@RestController
@RequestMapping(ApiPaths.TRAINERS)
@Tag(name = "Trainers", description = "Registration, profile and trainings of a trainer")
public class TrainerController {

    private final GymFacade facade;

    public TrainerController(GymFacade facade) {
        this.facade = facade;
    }

    /**
     * Task item 2. Trainer registration; no authentication required.
     *
     * @param request first name, last name and specialization (all required)
     * @return the generated username and password of the new trainer
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a trainer (item 2)",
            description = "No authentication. Returns the generated username and password.")
    @ApiResponse(responseCode = "201", description = "Trainer registered")
    @ApiResponse(responseCode = "400", description = "A name or the specialization is missing, or the specialization is unknown")
    @ApiResponse(responseCode = "409", description = "Another registration took the same username at the same moment: repeat the request")
    public RegistrationResponse register(@Valid @RequestBody TrainerRegistrationRequest request) {
        Trainer created = facade.createTrainer(TrainerMapper.toModel(request));

        return new RegistrationResponse(created.getUser().getUsername(), created.getUser().getPassword());
    }

    /**
     * Task item 8. Get Trainer Profile: any signed-in user can read any trainer profile.
     *
     * @param credentials the signed-in user, from the Authorization header
     * @param username    the trainer whose profile is read
     * @return the profile with the list of the trainer's trainees
     * @throws AuthenticationException if the username or password is wrong
     * @throws EntityNotFoundException if there is no trainer with this username
     */
    @GetMapping("/{username}")
    @Operation(summary = "Get a trainer profile (item 8)",
            description = "Any signed-in user can read any trainer profile.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The profile with the trainer's trainees")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "404", description = "No trainer with this username")
    public TrainerProfileResponse getProfile(Credentials credentials,
                                             @PathVariable("username") String username) {
        return TrainerMapper.toProfileResponse(facade.getTrainerProfile(credentials, username));
    }

    /**
     * Task item 9. Update Trainer Profile: only the trainer can update their own profile.
     * The username and the specialization do not change.
     *
     * @param credentials the signed-in trainer, from the Authorization header
     * @param username    the trainer whose profile is updated; must be the signed-in user
     * @param request     new names and active status
     * @return the updated profile with the list of the trainer's trainees
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     */
    @PutMapping("/{username}")
    @Operation(summary = "Update own trainer profile (item 9)",
            description = "Only the trainer can update their own profile. "
                    + "The username and the specialization (read only) do not change.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "The updated profile with the trainer's trainees")
    @ApiResponse(responseCode = "400", description = "Invalid request: a required field is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    public TrainerProfileResponse update(Credentials credentials,
                                         @PathVariable("username") String username,
                                         @Valid @RequestBody UpdateTrainerRequest request) {
        AccessChecker.requireOwner(credentials, username);
        Trainer updated = facade.updateTrainer(credentials, TrainerMapper.toModel(request));

        return TrainerMapper.toProfileResponse(updated);
    }

    /**
     * Task item 13. Get Trainer Trainings List: all criteria are optional query parameters;
     * only the trainer can get their own trainings.
     *
     * @param credentials the signed-in trainer, from the Authorization header
     * @param username    the trainer; must be the signed-in user
     * @param periodFrom  trainings on or after this date, ISO format: 2026-01-31
     * @param periodTo    trainings on or before this date, ISO format: 2026-01-31
     * @param traineeName the trainee's first name
     * @return the trainer's trainings that match all given criteria
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     */
    @GetMapping("/{username}/trainings")
    @Operation(summary = "Trainer trainings by criteria (item 13)",
            description = "Only the trainer can get their own trainings. All criteria are optional; dates are inclusive.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Trainings that match all given criteria")
    @ApiResponse(responseCode = "400", description = "Invalid date")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    public List<TrainerTrainingResponse> getTrainings(
            Credentials credentials,
            @PathVariable("username") String username,
            @RequestParam(name = "periodFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodFrom,
            @RequestParam(name = "periodTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodTo,
            @RequestParam(name = "traineeName", required = false) String traineeName) {
        AccessChecker.requireOwner(credentials, username);
        var criteria = new TrainerTrainingCriteria(periodFrom, periodTo, traineeName);
        return TrainingMapper.toTrainerTrainingResponses(facade.getTrainerTrainings(credentials, criteria));
    }

    /**
     * Task item 16. Activate/De-Activate Trainer: only the trainer can change their own status.
     * Not idempotent: setting the status the trainer already has is an error.
     *
     * @param credentials the signed-in trainer, from the Authorization header
     * @param username    the trainer; must be the signed-in user
     * @param request     the new active status
     * @throws AccessDeniedException   if the username is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     * @throws InvalidStateException   if the trainer already has this status
     */
    @PatchMapping("/{username}/active")
    @Operation(summary = "Activate or deactivate own trainer profile (item 16)",
            description = "Only the trainer can change their own status. "
                    + "Not idempotent: sending the current status again returns 409.")
    @SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
    @ApiResponse(responseCode = "200", description = "Status changed")
    @ApiResponse(responseCode = "400", description = "isActive is missing")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    @ApiResponse(responseCode = "403", description = "The username is not the signed-in user")
    @ApiResponse(responseCode = "409", description = "The trainer already has this status")
    public void changeActiveStatus(Credentials credentials,
                                   @PathVariable("username") String username,
                                   @Valid @RequestBody ActivationRequest request) {
        AccessChecker.requireOwner(credentials, username);
        facade.changeTrainerActiveStatus(credentials, request.isActive());
    }

}
