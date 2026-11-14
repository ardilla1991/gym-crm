package com.epam.training.gym.service;

import com.epam.training.gym.model.auth.Credentials;

/** Operations for any user, trainee or trainer. */
public interface UserService {
    /**
     * Changes the password of a trainee or a trainer.
     * The username can not be changed.
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the credentials are wrong
     * @throws IllegalArgumentException if the new password is blank
     */
    void changePassword(Credentials credentials, String newPassword);
}
