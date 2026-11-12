package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.rest.ApiPaths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Shows that the application is up; does not require authentication. */
@RestController
@RequestMapping(ApiPaths.API_V1 + "/health")
public class HealthController {

    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
