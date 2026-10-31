package com.epam.training.gym.persistence.util;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdGeneratorTest {

    @Test
    void maxId_emptyStorage_returnsZero() {
        Map<Long, String> storage = new HashMap<>();

        assertEquals(0L, IdGenerator.maxId(storage));
    }

    @Test
    void maxId_returnsBiggestId() {
        Map<Long, String> storage = new HashMap<>();
        storage.put(1L, "a");
        storage.put(5L, "b");
        storage.put(3L, "c");

        assertEquals(5L, IdGenerator.maxId(storage));
    }
}
