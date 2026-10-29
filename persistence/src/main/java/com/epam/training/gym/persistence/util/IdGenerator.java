package com.epam.training.gym.persistence.util;

import java.util.Collections;
import java.util.Map;

public final class IdGenerator {

    private IdGenerator() {
    }

    public static Long nextId(Map<Long, ?> storage) {
        if (storage.isEmpty()) {
            return 1L;
        }
        return Collections.max(storage.keySet()) + 1;
    }

}
