package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.controller.api.AuthApiDocs;
import com.epam.training.gym.api.rest.dto.ChangePasswordRequest;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Login and password change for a trainee or a trainer. Swagger docs: AuthApiDocs. */
@RestController
@RequestMapping(ApiPaths.LOGIN)
public class AuthController implements AuthApiDocs {

    private final GymFacade facade;

    public AuthController(GymFacade facade) {
        this.facade = facade;
    }

    @Override
    @GetMapping
    public void login(Credentials credentials) {
        // A user is either a trainee or a trainer, so one successful check is enough
        if (!facade.matchTraineeCredentials(credentials) && !facade.matchTrainerCredentials(credentials)) {
            throw new AuthenticationException();
        }
    }

    @Override
    @PutMapping("/password")
    public void changePassword(Credentials credentials, @Valid @RequestBody ChangePasswordRequest request) {
        facade.changePassword(credentials, request.newPassword());
    }
}
