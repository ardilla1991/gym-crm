package com.epam.training.gym.api.rest.mapper;

import com.epam.training.gym.api.rest.dto.AddTrainingRequest;
import com.epam.training.gym.api.rest.dto.TraineeTrainingResponse;
import com.epam.training.gym.api.rest.dto.TrainerTrainingResponse;
import com.epam.training.gym.api.rest.dto.TrainingTypeResponse;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.User;

import java.util.List;

/** Converts training DTOs to entities and back. */
public class TrainingMapper {

    private TrainingMapper() {
    }

    /** Get Trainee Trainings List. */
    public static TraineeTrainingResponse toTraineeTrainingResponse(Training training) {
        return new TraineeTrainingResponse(training.getTrainingName(), training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(), training.getTrainingDuration(),
                fullName(training.getTrainer().getUser()));
    }

    /** Converts each training to the trainee's view of it. */
    public static List<TraineeTrainingResponse> toTraineeTrainingResponses(List<Training> trainings) {
        return trainings.stream()
                .map(TrainingMapper::toTraineeTrainingResponse)
                .toList();
    }

    /** Get Trainer Trainings List. */
    public static TrainerTrainingResponse toTrainerTrainingResponse(Training training) {
        return new TrainerTrainingResponse(training.getTrainingName(), training.getTrainingDate(),
                training.getTrainingType().getTrainingTypeName(), training.getTrainingDuration(),
                fullName(training.getTrainee().getUser()));
    }

    /** Converts each training to the trainer's view of it. */
    public static List<TrainerTrainingResponse> toTrainerTrainingResponses(List<Training> trainings) {
        return trainings.stream()
                .map(TrainingMapper::toTrainerTrainingResponse)
                .toList();
    }

    /** Add Training: the trainee, the trainer and the type are set by the service. */
    public static Training toEntity(AddTrainingRequest request) {
        return Training.builder()
                .trainingName(request.trainingName())
                .trainingDate(request.trainingDate())
                .trainingDuration(request.trainingDuration())
                .build();
    }

    /** Get Training types. */
    public static TrainingTypeResponse toResponse(TrainingType trainingType) {
        return new TrainingTypeResponse(trainingType.getId(), trainingType.getTrainingTypeName());
    }

    /** Converts each training type to its response. */
    public static List<TrainingTypeResponse> toResponses(List<TrainingType> types) {
        return types.stream()
                .map(TrainingMapper::toResponse)
                .toList();
    }

    private static String fullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

}
