package com.epam.training.gym.api.rest.auth;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Builds HTTP Basic Authorization header values for controller tests. */
public final class TestAuth {

    private TestAuth() {
    }

    /** @return {@code Basic base64(username:password)} */
    public static String basic(String username, String password) {
        String pair = username + ":" + password;

        return "Basic " + Base64.getEncoder().encodeToString(pair.getBytes(StandardCharsets.UTF_8));
    }
}
