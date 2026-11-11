package com.epam.training.gym.service.exception;

/** The operation is not possible in the current state, for example activating an already active user. */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}
