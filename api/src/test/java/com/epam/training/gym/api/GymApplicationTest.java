package com.epam.training.gym.api;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Starts the whole application on a free port and calls it over HTTP. */
public class GymApplicationTest {

    @Test
    void start_serverAnswersHealthCheck() throws Exception {
        GymApplication application = GymApplication.start(0);
        try {
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("http://localhost:" + application.port() + "/api/v1/health")).build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("UP"));
            // TransactionIdFilter is registered: every response carries the id
            assertTrue(response.headers().firstValue("X-Transaction-Id").isPresent());
        } finally {
            application.stop();
        }
    }

    @Test
    void start_servesOpenApiDocumentation() throws Exception {
        GymApplication application = GymApplication.start(0);
        try {
            HttpRequest request = HttpRequest.newBuilder(
                    URI.create("http://localhost:" + application.port() + "/v3/api-docs")).build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // springdoc read our annotations: the title and the endpoints are in the OpenAPI JSON
            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Gym CRM API"));
            assertTrue(response.body().contains("/api/v1/trainees/{username}"));
        } finally {
            application.stop();
        }
    }
}
