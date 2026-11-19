package com.epam.training.gym.api.rest;

import com.epam.training.gym.api.rest.exception.ApiError;
import com.epam.training.gym.model.auth.Credentials;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
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

    static {
        // Credentials come from the Authorization header; without this Swagger UI would send them in the query
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(Credentials.class);
    }

    /**
     * Every 4xx and 5xx response has an ApiError body, as GlobalExceptionHandler returns.
     * Without this springdoc shows the success type (for example TraineeProfileResponse) for errors too.
     */
    @Bean
    public OpenApiCustomizer errorResponsesHaveApiErrorBody() {
        return openApi -> {
            openApi.getComponents().getSchemas().putAll(ModelConverters.getInstance().read(ApiError.class));
            Content apiErrorBody = apiErrorBody();
            for (PathItem path : openApi.getPaths().values()) {
                for (Operation operation : path.readOperations()) {
                    operation.getResponses().forEach((code, response) -> {
                        if (isError(code)) {
                            response.setContent(apiErrorBody);
                        }
                    });
                }
            }
        };
    }

    private static Content apiErrorBody() {
        Schema<?> apiError = new Schema<>().$ref("#/components/schemas/ApiError");
        return new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType().schema(apiError));
    }

    private static boolean isError(String code) {
        return code.startsWith("4") || code.startsWith("5");
    }
}