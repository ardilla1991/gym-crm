package com.epam.training.gym.api.rest;

/**
 * Expected REST paths for controller tests.
 * Written as literals on purpose, not taken from ApiPaths: if a path changes in the code,
 * the tests must fail, because clients depend on the paths.
 */
public final class TestPaths {

    public static final String TRAINEES = "/api/v1/trainees";
    public static final String TRAINERS = "/api/v1/trainers";
    public static final String TRAININGS = "/api/v1/trainings";
    public static final String TRAINING_TYPES = "/api/v1/training-types";
    public static final String LOGIN = "/api/v1/login";
    public static final String CHANGE_PASSWORD = "/api/v1/login/password";
    public static final String HEALTH = "/api/v1/health";
    /** OpenAPI JSON built by springdoc. */
    public static final String API_DOCS = "/v3/api-docs";
    public static final String ACTUATOR_HEALTH = "/actuator/health";

    private TestPaths() {
    }
}
