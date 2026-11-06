package com.epam.training.gym.service.exception;

/** Wrong username or password. The message is the same for both cases. */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super("Invalid username or password");
    }
}
