package com.epam.training.gym.service.exception;

/**
 * The input breaks a service rule that the request format can not express,
 * for example a repeated trainer in the list or a new password of a wrong length.
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }
}
