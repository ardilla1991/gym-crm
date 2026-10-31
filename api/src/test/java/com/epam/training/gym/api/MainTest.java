package com.epam.training.gym.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class MainTest {
    @Test
    void run_completesDemoWithoutErrors() {
        assertDoesNotThrow(Main::run);
    }
}
