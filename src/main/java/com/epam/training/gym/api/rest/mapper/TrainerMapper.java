package com.epam.training.gym.api.rest.mapper;

import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.model.User;

import java.util.Comparator;
import java.util.List;

/** Converts trainer DTOs to entities and back. */
public final class TrainerMapper {

    private TrainerMapper() {
    }

    /** Trainer Registration: only the name of the specialization is set; the service takes the type from the database. */
    public static Trainer toEntity(TrainerRegistrationRequest request) {
        return Trainer.builder()
                .specialization(new TrainingType(null, request.specialization()))
                .user(User.builder().firstName(request.firstName()).lastName(request.lastName()).build())
                .build();
    }

    /** Update Trainer Profile; the specialization is read-only and is not set. */
    public static Trainer toEntity(UpdateTrainerRequest request) {
        return Trainer.builder()
                .user(User.builder()
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .isActive(request.isActive())
                        .build())
                .build();
    }

    /** Get/Update Trainer Profile: the trainees list must be loaded (the service fetches it). */
    public static TrainerProfileResponse toProfileResponse(Trainer trainer) {
        User user = trainer.getUser();
        List<TraineeSummary> trainees = trainer.getTrainees().stream()
                .map(TraineeMapper::toSummary)
                .sorted(Comparator.comparing(TraineeSummary::username))
                .toList();
        return new TrainerProfileResponse(user.getUsername(), user.getFirstName(), user.getLastName(),
                trainer.getSpecialization().getTrainingTypeName(), user.isActive(), trainees);
    }

    /** A trainer in lists. */
    public static TrainerSummary toSummary(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerSummary(user.getUsername(), user.getFirstName(), user.getLastName(),
                trainer.getSpecialization().getTrainingTypeName());
    }

    /** A sorted list of trainers. */
    public static List<TrainerSummary> toSummaries(List<Trainer> trainers) {
        return trainers.stream()
                .map(TrainerMapper::toSummary)
                .sorted(Comparator.comparing(TrainerSummary::username))
                .toList();
    }
}
