package com.epam.training.gym.api.rest.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/** Swagger documentation of HealthController: kept apart so that the controller shows only routing and logic. */
@Tag(name = "Health", description = "Application status")
public interface HealthApiDocs {

    @Operation(summary = "Health check", description = "No authentication. Returns {\"status\": \"UP\"}.")
    @ApiResponse(responseCode = "200", description = "The application is up")
    Map<String, String> health();
}
