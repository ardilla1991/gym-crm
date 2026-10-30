package com.epam.training.gym.persistence.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdGeneratorTest {

    @Test
    void nextId_emptyStorage_returnsOne() {
        Map<Long, String> storage = new HashMap<>();

        assertEquals(1L, IdGenerator.nextId(storage));
    }

    @Test
    void nextId_returnsMaxIdPlusOne() {
        Map<Long, String> storage = new HashMap<>();
        storage.put(1L, "a");
        storage.put(5L, "b");
        storage.put(3L, "c");

        assertEquals(6L, IdGenerator.nextId(storage));
    }
}
