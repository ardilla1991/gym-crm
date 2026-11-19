package com.epam.training.gym.api.rest.auth;

import com.epam.training.gym.api.rest.exception.AccessDeniedException;
import com.epam.training.gym.model.auth.Credentials;

public final class AccessChecker {

    private AccessChecker() {
    }

    /**
     * @param username the user whose data the request changes: from the path or the request body
     * @throws AccessDeniedException if it is not the signed-in user
     */
    public static void requireOwner(Credentials credentials, String username) {
        if (!credentials.username().equals(username)) {
            throw new AccessDeniedException(
                    "User " + credentials.username() + " can not change data of " + username);
        }
    }
}
