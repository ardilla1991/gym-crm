package com.epam.training.gym.domain;

public enum TrainingTypeName {
    FITNESS("fitness"),
    YOGA("yoga"),
    ZUMBA("Zumba"),
    STRETCHING("stretching"),
    RESISTANCE("resistance");

    private final String name;

    TrainingTypeName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
