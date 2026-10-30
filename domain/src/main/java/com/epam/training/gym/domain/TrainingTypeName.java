package com.epam.training.gym.domain;

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
