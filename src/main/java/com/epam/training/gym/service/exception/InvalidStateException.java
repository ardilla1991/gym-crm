package com.epam.training.gym.service.exception;

/**
 * The operation is not allowed in the current state,
 * for example activating an already active user or adding a training with an inactive trainer.
 */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}
