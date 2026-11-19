package com.epam.training.gym;

import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.model.validation.FieldLimits;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Starts the whole application once on a free port and calls it over HTTP: no mocks, H2 with data.sql.
 * The tests only add new users and never change the existing ones, so their order does not matter.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GymApplicationTest {

    /** A trainee from data.sql. */
    private static final String EMILY = TestAuth.basic("Emily.Johnson", "Hq5sW1dF6y");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @LocalServerPort
    private int port;

    @Test
    void start_serverAnswersHealthCheck() throws Exception {
        HttpResponse<String> response = send("GET", TestPaths.HEALTH, null, null);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("UP"));
        // TransactionIdFilter is registered: every response carries the id
        assertTrue(response.headers().firstValue("X-Transaction-Id").isPresent());
    }

    @Test
    void actuatorHealth_isUpWithDetailsAndNoSignIn() throws Exception {
        HttpResponse<String> response = send("GET", TestPaths.ACTUATOR_HEALTH, null, null);

        assertEquals(200, response.statusCode());
        JsonNode health = JSON.readTree(response.body());
        assertEquals("UP", health.get("status").asString());
        assertEquals("UP", health.at("/components/db/status").asString());
        assertEquals("UP", health.at("/components/trainingTypes/status").asString());
        assertEquals("UP", health.at("/components/memory/status").asString());
    }

    @Test
    void actuatorPrometheus_showsCustomMetrics() throws Exception {
        HttpResponse<String> response = send("GET", TestPaths.ACTUATOR_PROMETHEUS, null, null);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("gym_registrations_total{role=\"trainee\"}"), response.body());
        assertTrue(response.body().contains("gym_trainings_added_total"));
        assertTrue(response.body().contains("gym_authentication_failures_total"));
    }

    @Test
    void start_servesOpenApiDocumentation() throws Exception {
        HttpResponse<String> response = send("GET", TestPaths.API_DOCS, null, null);

        // springdoc read our annotations: the title and the endpoints are in the OpenAPI JSON
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Gym CRM API"));
        assertTrue(response.body().contains("/api/v1/trainees/{username}"));

        // Errors are documented with the real error body, success with its own DTO
        JsonNode docs = JSON.readTree(response.body());
        JsonNode profile = docs.at("/paths/~1api~1v1~1trainees~1{username}/get/responses");
        assertEquals("#/components/schemas/ApiError",
                profile.at("/404/content/application~1json/schema/$ref").asString());
        assertEquals("#/components/schemas/ApiError",
                profile.at("/401/content/application~1json/schema/$ref").asString());
        assertTrue(docs.at("/components/schemas/ApiError/properties").has("message"));
        assertFalse(profile.at("/200").toString().contains("ApiError"));

        // Credentials come from the Authorization header: Login has no username or password query parameters
        JsonNode loginParameters = docs.at("/paths/~1api~1v1~1login/get/parameters");
        assertFalse(loginParameters.toString().contains("password"), loginParameters.toString());

        // Names are required and limited in the documentation too, not only in the validation
        for (String request : List.of("TraineeRegistrationRequest", "TrainerRegistrationRequest",
                "UpdateTraineeRequest", "UpdateTrainerRequest")) {
            JsonNode schema = docs.at("/components/schemas/" + request);
            assertTrue(schema.path("required").toString().contains("\"firstName\""), request + ": " + schema);
            assertTrue(schema.path("required").toString().contains("\"lastName\""), request + ": " + schema);
            assertEquals(FieldLimits.NAME_MAX, schema.at("/properties/firstName/maxLength").asInt(), request);
        }
    }

    @Test
    void wrongPassword_invalidBody_returnsUnauthorized() throws Exception {
        HttpResponse<String> response = send("PUT", TestPaths.TRAINEES + "/Emily.Johnson",
                TestAuth.basic("Emily.Johnson", "wrong"), "{}");

        assertEquals(401, response.statusCode());
    }

    @Test
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        HttpResponse<String> response = send("GET", TestPaths.LOGIN, TestAuth.basic("Emily.Johnson", "wrong"), null);

        assertEquals(401, response.statusCode());
    }

    @Test
    void registration_thenLoginAndOwnProfile_succeed() throws Exception {
        HttpResponse<String> registered = send("POST", TestPaths.TRAINEES, null,
                "{\"firstName\": \"Full\", \"lastName\": \"Stack\"}");
        assertEquals(201, registered.statusCode());
        JsonNode credentials = JSON.readTree(registered.body());

        HttpResponse<String> login = send("GET", TestPaths.LOGIN,
                TestAuth.basic(credentials.get("username").asString(), credentials.get("password").asString()), null);

        assertEquals("Full.Stack", credentials.get("username").asString());
        assertEquals(200, login.statusCode());

        // The generated username also works in the profile path
        HttpResponse<String> profile = send("GET", TestPaths.TRAINEES + "/" + credentials.get("username").asString(),
                TestAuth.basic(credentials.get("username").asString(), credentials.get("password").asString()), null);
        assertEquals(200, profile.statusCode());
    }

    @Test
    void registration_nameWithSlash_returnsBadRequest() throws Exception {
        HttpResponse<String> response = send("POST", TestPaths.TRAINEES, null,
                "{\"firstName\": \"Review/Slash\", \"lastName\": \"User\"}");

        assertEquals(400, response.statusCode());
    }

    @Test
    void registration_nameWithColon_returnsBadRequest() throws Exception {
        HttpResponse<String> response = send("POST", TestPaths.TRAINEES, null,
                "{\"firstName\": \"Review:Colon\", \"lastName\": \"User\"}");

        assertEquals(400, response.statusCode());
    }

    @Test
    void registration_tooLongName_returnsBadRequestNotServerError() throws Exception {
        HttpResponse<String> response = send("POST", TestPaths.TRAINEES, null,
                "{\"firstName\": \"" + "a".repeat(256) + "\", \"lastName\": \"User\"}");

        assertEquals(400, response.statusCode());
    }

    @Test
    void repeatedTrainers_returnsBadRequest() throws Exception {
        // Checked by the service: its exception goes through the real exception handler
        HttpResponse<String> response = send("PUT", TestPaths.TRAINEES + "/Emily.Johnson/trainers", EMILY,
                "{\"trainerUsernames\": [\"David.Miller\", \"David.Miller\"]}");

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("must not repeat"), response.body());
    }

    /** Sends a request to the running application; authorization and body may be null. */
    private HttpResponse<String> send(String method, String path, String authorization, String json)
            throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .method(method, json == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(json));
        if (authorization != null) {
            request.header("Authorization", authorization);
        }
        if (json != null) {
            request.header("Content-Type", "application/json");
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }
}
