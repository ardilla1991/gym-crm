package com.epam.training.gym.service.exception;

/** Wrong username or password. */
public class AuthenticationException extends RuntimeException {

    /** Wrong username or password: the same message for both cases on purpose. */
    public AuthenticationException() {
        super("Invalid username or password");
    }

    /** For other authentication problems, for example a missing Authorization header. */
    public AuthenticationException(String message) {
        super(message);
    }
}
