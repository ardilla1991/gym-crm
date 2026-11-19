package com.epam.training.gym.api.rest.controller.api;

import com.epam.training.gym.api.rest.ApiDocumentation;
import com.epam.training.gym.api.rest.dto.ChangePasswordRequest;
import com.epam.training.gym.model.auth.Credentials;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Swagger documentation of AuthController: kept apart so that the controller shows only routing and logic. */
@Tag(name = "Login", description = "Login and password change for a trainee or a trainer")
@SecurityRequirement(name = ApiDocumentation.BASIC_AUTH)
public interface AuthApiDocs {

    @Operation(summary = "Login (item 3)",
            description = "Checks that the username and password match a trainee or a trainer.")
    @ApiResponse(responseCode = "200", description = "Username and password are correct")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or password")
    void login(Credentials credentials);

    @Operation(summary = "Change password (item 4)",
            description = "The username and the old password go in the Authorization header, "
                    + "the new password in the body. The new password must contain exactly 10 characters.")
    @ApiResponse(responseCode = "200", description = "Password changed")
    @ApiResponse(responseCode = "400", description = "The new password is blank or is not exactly 10 characters long")
    @ApiResponse(responseCode = "401", description = "Missing or wrong username or old password")
    void changePassword(Credentials credentials, ChangePasswordRequest request);
}
