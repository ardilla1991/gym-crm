package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.rest.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Shows that the application is up; does not require authentication. */
@RestController
@RequestMapping(ApiPaths.API_V1 + "/health")
@Tag(name = "Health", description = "Application status")
public class HealthController {

    @GetMapping
    @Operation(summary = "Health check", description = "No authentication. Returns {\"status\": \"UP\"}.")
    @ApiResponse(responseCode = "200", description = "The application is up")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
