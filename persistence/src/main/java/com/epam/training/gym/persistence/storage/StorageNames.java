package com.epam.training.gym.persistence.storage;

/**
 * Names of the storage beans (one java.util.Map per namespace).
 * The beans themselves are declared in the config module;
 * the names live here because StorageInitializer and the DAOs (@Qualifier) need them,
 * and persistence cannot depend on config.
 */
public class StorageNames {
    public static final String TRAINEE_STORAGE = "traineeStorage";
    public static final String TRAINER_STORAGE = "trainerStorage";
    public static final String TRAINING_STORAGE = "trainingStorage";
    public static final String TRAINING_TYPE_STORAGE = "trainingTypeStorage";

    private StorageNames() {
    }
}
