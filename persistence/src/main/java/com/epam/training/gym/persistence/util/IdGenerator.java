package com.epam.training.gym.persistence.util;

import java.util.Collections;
import java.util.Map;

/** max id = the biggest id in the storage
 * (or 1 if the storage is empty). */
public final class IdGenerator {

    private IdGenerator() {
    }

    /** The biggest id in the storage, or 0 if the storage is empty. */
    public static long maxId(Map<Long, ?> storage) {
        if (storage.isEmpty()) {
            return 0L;
        }
        return Collections.max(storage.keySet());
    }

}
