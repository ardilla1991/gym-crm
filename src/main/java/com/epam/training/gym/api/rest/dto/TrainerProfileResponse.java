package com.epam.training.gym.api.rest.dto;

import com.epam.training.gym.model.TrainingTypeName;

import java.util.List;

/** Get and Update trainer profile. */
public record TrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeName specialization,
        boolean isActive,
        List<TraineeSummary> trainees) {
}
