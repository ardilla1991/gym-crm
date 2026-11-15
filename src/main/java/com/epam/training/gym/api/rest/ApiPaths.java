package com.epam.training.gym.api.rest;

/**
 * Base paths of the REST API, shared by the controllers.
 */
public final class ApiPaths {
    public static final String API_V1 = "/api/v1";
    public static final String TRAINEES = API_V1 + "/trainees";
    public static final String TRAINERS = API_V1 + "/trainers";
    public static final String TRAININGS = API_V1 + "/trainings";
    public static final String TRAINING_TYPES = API_V1 + "/training-types";
    public static final String LOGIN = API_V1 + "/login";
    public static final String HEALTH = API_V1 + "/health";

    private ApiPaths() {
    }
}
