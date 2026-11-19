package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.controller.api.TrainingTypeApiDocs;
import com.epam.training.gym.api.rest.dto.TrainingTypeResponse;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.model.auth.Credentials;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Training type endpoints. Swagger docs: TrainingTypeApiDocs. */
@RestController
@RequestMapping(ApiPaths.TRAINING_TYPES)
public class TrainingTypeController implements TrainingTypeApiDocs {

    private final GymFacade facade;

    public TrainingTypeController(GymFacade facade) {
        this.facade = facade;
    }

    @Override
    @GetMapping
    public List<TrainingTypeResponse> getAll(Credentials credentials) {
        return TrainingMapper.toResponses(facade.getTrainingTypes(credentials));
    }
}
