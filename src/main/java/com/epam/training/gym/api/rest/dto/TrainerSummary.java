package com.epam.training.gym.api.rest.dto;

import com.epam.training.gym.model.TrainingTypeName;

/** A trainer in lists: trainee's trainers, not assigned trainers. */
public record TrainerSummary(
        String username,
        String firstName,
        String lastName,
        TrainingTypeName specialization) {
}
