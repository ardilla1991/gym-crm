package com.epam.training.gym.service;

import com.epam.training.gym.model.auth.Credentials;

/** Operations for any user, trainee or trainer. */
public interface UserService {
    /**
     * Changes the password of a trainee or a trainer.
     * The username can not be changed.
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the credentials are wrong
     * @throws com.epam.training.gym.service.exception.InvalidInputException
     *         if the new password is blank or does not contain exactly 10 characters
     */
    void changePassword(Credentials credentials, String newPassword);
}
