package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.dto.ChangePasswordRequest;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Login and password change for any user: a trainee or a trainer. */
@RestController
@RequestMapping(ApiPaths.LOGIN)
@Tag(name = "Login", description = "Login and password change for a trainee or a trainer")
@SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
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
    @Operation(summary = "Login (item 3)",
            description = "Checks that the username and password match a trainee or a trainer.")
    @ApiResponse(responseCode = "200", description = "Username and password are correct")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
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
    @Operation(summary = "Change password (item 4)",
            description = "The username and the old password go in the Authorization header, "
                    + "the new password in the body.")
    @ApiResponse(responseCode = "200", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "The new password is blank")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or old password")
    public void changePassword(Credentials credentials, @Valid @RequestBody ChangePasswordRequest request) {
        facade.changePassword(credentials, request.newPassword());
    }
}
