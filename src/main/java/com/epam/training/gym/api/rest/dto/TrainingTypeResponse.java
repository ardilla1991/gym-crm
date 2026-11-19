package com.epam.training.gym.api.rest.dto;

import com.epam.training.gym.model.TrainingTypeName;

/** Get Training types. */
public record TrainingTypeResponse(Long id, TrainingTypeName trainingType) {
}
