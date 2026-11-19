package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.controller.api.HealthApiDocs;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Shows that the application is up; does not require authentication. Swagger docs: HealthApiDocs. */
@RestController
@RequestMapping(ApiPaths.HEALTH)
public class HealthController implements HealthApiDocs {

    @Override
    @GetMapping
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
