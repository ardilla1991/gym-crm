package com.epam.training.gym.api.rest.mapper;

import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.User;

import java.util.Comparator;
import java.util.List;

/** Converts trainee DTOs to entities and back. */
public final  class TraineeMapper {

    private TraineeMapper() {
    }

    /** Trainee Registration. */
    public static Trainee toEntity(TraineeRegistrationRequest request) {
        return Trainee.builder()
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .user(User.builder().firstName(request.firstName()).lastName(request.lastName()).build())
                .build();
    }

    /** Update Trainee Profile ; the service copies only the allowed fields. */
    public static Trainee toEntity(UpdateTraineeRequest request) {
        return Trainee.builder()
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .user(User.builder()
                        .firstName(request.firstName())
                        .lastName(request.lastName())
                        .isActive(request.isActive())
                        .build())
                .build();
    }

    /** Update/Get Trainee Profile : the trainers list must be loaded (the service fetches it). */
    public static TraineeProfileResponse toProfileResponse(Trainee trainee) {
        User user = trainee.getUser();
        List<TrainerSummary> trainers = trainee.getTrainers().stream()
                .map(TrainerMapper::toSummary)
                .sorted(Comparator.comparing(com.epam.training.gym.api.rest.dto.TrainerSummary::username))
                .toList();
        return new TraineeProfileResponse(user.getUsername(), user.getFirstName(), user.getLastName(),
                trainee.getDateOfBirth(), trainee.getAddress(), user.isActive(), trainers);
    }

    /** A trainee in the trainer's profile. */
    public static TraineeSummary toSummary(Trainee trainee) {
        User user = trainee.getUser();
        return new TraineeSummary(user.getUsername(), user.getFirstName(), user.getLastName());
    }
}
