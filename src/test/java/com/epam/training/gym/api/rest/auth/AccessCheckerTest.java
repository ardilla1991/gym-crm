package com.epam.training.gym.api.rest.auth;

import com.epam.training.gym.api.rest.exception.AccessDeniedException;
import com.epam.training.gym.model.auth.Credentials;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AccessCheckerTest {

    private static final Credentials EMILY = new Credentials("Emily.Johnson", "secret");

    @Test
    void requireOwner_sameUser_passes() {
        assertDoesNotThrow(() -> AccessChecker.requireOwner(EMILY, "Emily.Johnson"));
    }

    @Test
    void requireOwner_anotherUser_throws() {
        assertThrows(AccessDeniedException.class, () -> AccessChecker.requireOwner(EMILY, "John.Smith"));
    }
}
