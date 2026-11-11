package com.epam.training.gym.api.rest.dto;

/**
 * The generated credentials for trainee/trainer registration.
 * The only response that contains a password: the user needs it to sign in.
 */
public record RegistrationResponse(String username, String password) {
}
