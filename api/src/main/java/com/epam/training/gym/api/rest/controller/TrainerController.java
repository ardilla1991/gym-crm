package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.auth.AccessChecker;
import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.api.rest.exception.AccessDeniedException;
import com.epam.training.gym.api.rest.mapper.TrainerMapper;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidStateException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Trainer endpoints: registration, profile and trainings of a trainer. */
@RestController
@RequestMapping(ApiPaths.TRAINERS)
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
    public RegistrationResponse register(@RequestBody TrainerRegistrationRequest request) {
        Trainer created = facade.createTrainer(TrainerMapper.toEntity(request));

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
    public TrainerProfileResponse update(Credentials credentials,
                                         @PathVariable("username") String username,
                                         @RequestBody UpdateTrainerRequest request) {
        AccessChecker.requireOwner(credentials, username);
        Trainer updated = facade.updateTrainer(credentials, TrainerMapper.toEntity(request));

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
    public void changeActiveStatus(Credentials credentials,
                                   @PathVariable("username") String username,
                                   @RequestBody ActivationRequest request) {
        AccessChecker.requireOwner(credentials, username);
        facade.changeTrainerActiveStatus(credentials, request.isActive());
    }

}
