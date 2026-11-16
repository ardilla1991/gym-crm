package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.auth.AccessChecker;
import com.epam.training.gym.api.rest.controller.api.TraineeApiDocs;
import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.api.rest.mapper.TraineeMapper;
import com.epam.training.gym.api.rest.mapper.TrainerMapper;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Trainee endpoints: registration, profile, trainers and trainings of a trainee. Swagger docs: TraineeApiDocs. */
@RestController
@RequestMapping(ApiPaths.TRAINEES)
public class TraineeController implements TraineeApiDocs {

    private final GymFacade facade;

    public TraineeController(GymFacade facade) {
        this.facade = facade;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody TraineeRegistrationRequest request) {
        Trainee created = facade.createTrainee(TraineeMapper.toModel(request));
        return new RegistrationResponse(created.getUser().getUsername(), created.getUser().getPassword());
    }

    @Override
    @GetMapping("/{username}")
    public TraineeProfileResponse getProfile(Credentials credentials, @PathVariable("username") String username) {
        return TraineeMapper.toProfileResponse(facade.getTraineeProfile(credentials, username));
    }

    @Override
    @PutMapping("/{username}")
    public TraineeProfileResponse update(Credentials credentials,
                                         @PathVariable("username") String username,
                                         @Valid @RequestBody UpdateTraineeRequest request) {
        AccessChecker.requireOwner(credentials, username);
        Trainee updated = facade.updateTrainee(credentials, TraineeMapper.toModel(request));

        return TraineeMapper.toProfileResponse(updated);
    }

    @Override
    @DeleteMapping("/{username}")
    public void delete(Credentials credentials, @PathVariable("username") String username) {
        AccessChecker.requireOwner(credentials, username);
        facade.deleteTrainee(credentials);
    }

    @Override
    @GetMapping("/{username}/unassigned-trainers")
    public List<TrainerSummary> getUnassignedTrainers(Credentials credentials,
                                                      @PathVariable("username") String username) {
        AccessChecker.requireOwner(credentials, username);
        return TrainerMapper.toSummaries(facade.getUnassignedTrainers(credentials));
    }

    @Override
    @PutMapping("/{username}/trainers")
    public List<TrainerSummary> updateTrainers(Credentials credentials,
                                               @PathVariable("username") String username,
                                               @Valid @RequestBody UpdateTraineeTrainersRequest request) {
        AccessChecker.requireOwner(credentials, username);
        return TrainerMapper.toSummaries(facade.updateTraineeTrainers(credentials, request.trainerUsernames()));
    }

    @Override
    @GetMapping("/{username}/trainings")
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

    @Override
    @PatchMapping("/{username}/active")
    public void changeActiveStatus(Credentials credentials,
                                   @PathVariable("username") String username,
                                   @Valid @RequestBody ActivationRequest request) {
        AccessChecker.requireOwner(credentials, username);
        facade.changeTraineeActiveStatus(credentials, request.isActive());
    }
}
