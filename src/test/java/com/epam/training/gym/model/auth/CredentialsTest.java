package com.epam.training.gym.model.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CredentialsTest {

    @Test
    void toString_showsUsernameButHidesPassword() {
        String text = new Credentials("John.Smith", "secret123").toString();

        assertTrue(text.contains("John.Smith"), text);
        assertFalse(text.contains("secret123"), text);
    }

}
