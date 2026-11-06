package com.epam.training.gym.service;

import com.epam.training.gym.domain.auth.Credentials;

/** Account operations shared by trainees and trainers. */
public interface AccountService {

    /** True if a user with this username exists and the password matches. */
    boolean matchCredentials(Credentials credentials);

    /**
     * Changes the password for a user with the given credentials.
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the credentials are wrong
     * @throws IllegalArgumentException if the new password is blank
     */
    void changePassword(Credentials credentials, String newPassword);

    /**
     * Activates an inactive user and deactivates an active one.
     * Not idempotent: calling it twice returns the user to the original status.
     * @return the new status: true if the user is active now
     * @throws com.epam.training.gym.service.exception.AuthenticationException if the credentials are wrong
     */
    boolean toggleActive(Credentials credentials);
}
