package com.epam.training.gym.api.rest.dto;

import java.time.LocalDate;
import java.util.List;

/** Get and Update trainee profile. */
public record TraineeProfileResponse(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive,
        List<TrainerSummary> trainers) {
}
