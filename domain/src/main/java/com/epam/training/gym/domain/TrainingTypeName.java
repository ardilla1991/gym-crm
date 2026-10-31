package com.epam.training.gym.domain;

/**
 * The fixed list of allowed training type names.
 * An enum makes it impossible to store a name outside this list (e.g. "boxing").
 */
public enum TrainingTypeName {
    FITNESS("fitness"),
    YOGA("yoga"),
    ZUMBA("Zumba"),
    STRETCHING("stretching"),
    RESISTANCE("resistance");

    private final String displayName;

    TrainingTypeName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
