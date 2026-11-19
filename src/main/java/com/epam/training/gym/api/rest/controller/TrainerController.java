package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.auth.AccessChecker;
import com.epam.training.gym.api.rest.controller.api.TrainerApiDocs;
import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.api.rest.mapper.TrainerMapper;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** Trainer endpoints: registration, profile and trainings of a trainer. Swagger docs: TrainerApiDocs. */
@RestController
@RequestMapping(ApiPaths.TRAINERS)
public class TrainerController implements TrainerApiDocs {

    private final GymFacade facade;

    public TrainerController(GymFacade facade) {
        this.facade = facade;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationResponse register(@Valid @RequestBody TrainerRegistrationRequest request) {
        Trainer created = facade.createTrainer(TrainerMapper.toModel(request));

        return new RegistrationResponse(created.getUser().getUsername(), created.getUser().getPassword());
    }

    @Override
    @GetMapping("/{username}")
    public TrainerProfileResponse getProfile(Credentials credentials, @PathVariable("username") String username) {
        return TrainerMapper.toProfileResponse(facade.getTrainerProfile(credentials, username));
    }

    @Override
    @PutMapping("/{username}")
    public TrainerProfileResponse update(Credentials credentials,
                                         @PathVariable("username") String username,
                                         @Valid @RequestBody UpdateTrainerRequest request) {
        AccessChecker.requireOwner(credentials, username);
        Trainer updated = facade.updateTrainer(credentials, TrainerMapper.toModel(request));

        return TrainerMapper.toProfileResponse(updated);
    }

    @Override
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

    @Override
    @PatchMapping("/{username}/active")
    public void changeActiveStatus(Credentials credentials,
                                   @PathVariable("username") String username,
                                   @Valid @RequestBody ActivationRequest request) {
        AccessChecker.requireOwner(credentials, username);
        facade.changeTrainerActiveStatus(credentials, request.isActive());
    }
}
