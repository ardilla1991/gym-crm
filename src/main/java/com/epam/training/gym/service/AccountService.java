package com.epam.training.gym.service;

import com.epam.training.gym.model.UserProfile;
import com.epam.training.gym.model.auth.Credentials;

/**
 * Account operations shared by trainees and trainers.
 * T is Trainee or Trainer.
 */
public interface AccountService<T extends UserProfile>  {

    /** True if a user with this username exists and the password matches. */
    boolean matchCredentials(Credentials credentials);

    /**
     * Loads the caller's own profile; every operation except create starts with it.
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the credentials are wrong
     */
    T authenticate(Credentials credentials);

    /**
     * Any signed-in user, trainee or trainer, may read a profile by username.
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the caller's credentials are wrong
     * @throws com.epam.training.gym.service.exception.EntityNotFoundException if there is no such profile
     */
    T getProfile(Credentials credentials, String username);

    /**
     * Activates or deactivates the user.
     * Not idempotent: setting the status the user already has is an error.
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the credentials are wrong
     * @throws com.epam.training.gym.service.exception.InvalidStateException if the user already has this status
     */
    void changeActiveStatus(Credentials credentials, boolean active);
}
