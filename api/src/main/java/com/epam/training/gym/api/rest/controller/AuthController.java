package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.dto.ChangePasswordRequest;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Login and password change for any user: a trainee or a trainer. */
@RestController
@RequestMapping(ApiPaths.LOGIN)
public class AuthController {

    private final GymFacade facade;

    public AuthController(GymFacade facade) {
        this.facade = facade;
    }

    /**
     * Task item 3. Login: checks that the username and password match a trainee or a trainer.
     *
     * @param credentials username and password from the Authorization header
     * @throws AuthenticationException if they match neither a trainee nor a trainer
     */
    @GetMapping
    public void login(Credentials credentials) {
        // A user is either a trainee or a trainer, so one successful check is enough
        if (!facade.matchTraineeCredentials(credentials) && !facade.matchTrainerCredentials(credentials)) {
            throw new AuthenticationException();
        }
    }


    /**
     * Task item 4. Change Login: changes the password of a trainee or a trainer.
     *
     * @param credentials username and the old password from the Authorization header
     * @param request     the new password
     * @throws AuthenticationException if the old password is wrong
     */
    @PutMapping("/password")
    public void changePassword(Credentials credentials, @Valid @RequestBody ChangePasswordRequest request) {
        facade.changePassword(credentials, request.newPassword());
    }
}
