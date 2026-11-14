package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.dto.TrainingTypeResponse;
import com.epam.training.gym.api.rest.mapper.TrainingMapper;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Training type endpoints. */
@RestController
@RequestMapping(ApiPaths.TRAINING_TYPES)
@Tag(name = "Training types", description = "Reference list of training types")
@SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
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
    @Operation(summary = "List training types (item 17)",
            description = "Available to any signed-in user. Ordered by name.")
    @ApiResponse(responseCode = "200", description = "Id and name of every training type")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    public List<TrainingTypeResponse> getAll(Credentials credentials) {
        return TrainingMapper.toResponses(facade.getTrainingTypes(credentials));
    }
}
