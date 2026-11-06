package com.epam.training.gym.domain.auth;

/**
 * Username and password for authentication.
 * toString() hides the password, so it can never end up in logs.
 */
public record Credentials(String username, String password) {

    @Override
    public String toString() {
        return "Credentials[username=" + username + "]";
    }
}
