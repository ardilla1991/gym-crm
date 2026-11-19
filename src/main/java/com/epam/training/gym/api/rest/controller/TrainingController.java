package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.auth.AccessChecker;
import com.epam.training.gym.api.rest.controller.api.TrainingApiDocs;
import com.epam.training.gym.api.rest.dto.AddTrainingRequest;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.model.auth.Credentials;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Training endpoints. Swagger docs: TrainingApiDocs. */
@RestController
@RequestMapping(ApiPaths.TRAININGS)
public class TrainingController implements TrainingApiDocs {

    private final GymFacade facade;

    public TrainingController(GymFacade facade) {
        this.facade = facade;
    }

    @Override
    @PostMapping
    public void add(Credentials credentials, @Valid @RequestBody AddTrainingRequest request) {
        AccessChecker.requireOwner(credentials, request.traineeUsername());
        facade.addTraining(credentials, request.trainerUsername(), TrainingMapper.toModel(request));
    }
}
