package com.epam.training.gym.api.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) description of the whole API: its title and the HTTP Basic authentication.
 * Endpoints that need a signed-in user refer to the scheme by BASIC_AUTH.
 * It is a Spring bean on purpose: springdoc reads these annotations only from beans.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Gym CRM API",
        version = "v1",
        description = "Trainees, trainers and trainings. Errors are returned as ApiError: status, error, message."))
@SecurityScheme(
        name = ApiDocumentation.BASIC_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Username and password of a trainee or a trainer")
public class ApiDocumentation {

    public static final String BASIC_AUTH = "basicAuth";
}