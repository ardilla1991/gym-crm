package com.epam.training.gym.api.rest.exception;

/** The signed-in user tries to change data of another user. */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
