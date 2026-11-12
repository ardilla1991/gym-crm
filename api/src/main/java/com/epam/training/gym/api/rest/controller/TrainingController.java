package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.auth.AccessChecker;
import com.epam.training.gym.api.rest.dto.AddTrainingRequest;
import com.epam.training.gym.api.rest.exception.AccessDeniedException;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Training endpoints. */
@RestController
@RequestMapping(ApiPaths.TRAININGS)
public class TrainingController {

    private final GymFacade facade;

    public TrainingController(GymFacade facade) {
        this.facade = facade;
    }

    /**
     * Task item 14. Add Training: a trainee adds a training for themselves with any trainer.
     * The training type is the trainer's specialization, so it is not in the request.
     *
     * @param credentials the signed-in trainee, from the Authorization header
     * @param request     trainee and trainer usernames, training name, date and duration
     * @throws AccessDeniedException   if the trainee in the request is not the signed-in user
     * @throws AuthenticationException if the username or password is wrong
     * @throws EntityNotFoundException if the trainer does not exist
     */
    @PostMapping
    public void add(Credentials credentials, @Valid @RequestBody AddTrainingRequest request) {
        AccessChecker.requireOwner(credentials, request.traineeUsername());
        facade.addTraining(credentials, request.trainerUsername(), TrainingMapper.toEntity(request));
    }
}
