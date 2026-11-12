package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.dto.TrainingTypeResponse;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Training type endpoints. */
@RestController
@RequestMapping(ApiPaths.TRAINING_TYPES)
public class TrainingTypeController {
    private final GymFacade facade;

    public TrainingTypeController(GymFacade facade) {
        this.facade = facade;
    }

    /**
     * Task item 17. Get Training types: available to any signed-in user.
     *
     * @param credentials the signed-in user, from the Authorization header
     * @return id and name of every training type, ordered by name
     * @throws AuthenticationException if the username or password is wrong
     */
    @GetMapping
    public List<TrainingTypeResponse> getAll(Credentials credentials) {
        return TrainingMapper.toResponses(facade.getTrainingTypes(credentials));
    }
}
