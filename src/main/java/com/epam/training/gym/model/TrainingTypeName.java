package com.epam.training.gym.model;

/**
 * The fixed list of allowed training type names.
 * An enum makes it impossible to store a name outside this list (e.g. "boxing").
 */
public enum TrainingTypeName {
    FITNESS,
    YOGA,
    ZUMBA,
    STRETCHING,
    RESISTANCE
}
