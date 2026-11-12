package com.epam.training.gym.api.rest.mapper;

import com.epam.training.gym.api.rest.dto.AddTrainingRequest;
import com.epam.training.gym.api.rest.dto.TraineeTrainingResponse;
import com.epam.training.gym.api.rest.dto.TrainerTrainingResponse;
import com.epam.training.gym.api.rest.dto.TrainingTypeResponse;
import com.epam.training.gym.domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TrainingMapperTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 5);
    private static final TrainingType YOGA = new TrainingType(2L, TrainingTypeName.YOGA);

    @Test
    void toEntity_copiesNameDateAndDuration() {
        Training training = TrainingMapper.toEntity(
                new AddTrainingRequest("Emily.Johnson", "Anna.Lee", "Morning yoga", DATE, 60));

        assertEquals("Morning yoga", training.getTrainingName());
        assertEquals(DATE, training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());
        assertNull(training.getTrainingType());            // set by the service from the trainer
    }

    @Test
    void toTraineeTrainingResponse_containsTrainerFullName() {
        TraineeTrainingResponse response = TrainingMapper.toTraineeTrainingResponse(training());

        assertEquals("Morning yoga", response.trainingName());
        assertEquals(TrainingTypeName.YOGA, response.trainingType());
        assertEquals("Anna Lee", response.trainerName());
    }

    @Test
    void toTrainerTrainingResponse_containsTraineeFullName() {
        TrainerTrainingResponse response = TrainingMapper.toTrainerTrainingResponse(training());

        assertEquals(60, response.trainingDuration());
        assertEquals("Emily Johnson", response.traineeName());
    }

    @Test
    void toResponse_trainingType_containsIdAndName() {
        assertEquals(new TrainingTypeResponse(2L, TrainingTypeName.YOGA), TrainingMapper.toResponse(YOGA));
    }

    @Test
    void toTraineeTrainingResponses_convertsEachTraining() {
        Training training = training();

        List<TraineeTrainingResponse> responses = TrainingMapper.toTraineeTrainingResponses(List.of(training, training));

        assertEquals(List.of(TrainingMapper.toTraineeTrainingResponse(training),
                TrainingMapper.toTraineeTrainingResponse(training)), responses);
    }

    @Test
    void toTrainerTrainingResponses_convertsEachTraining() {
        Training training = training();

        List<TrainerTrainingResponse> responses = TrainingMapper.toTrainerTrainingResponses(List.of(training, training));

        assertEquals(List.of(TrainingMapper.toTrainerTrainingResponse(training),
                TrainingMapper.toTrainerTrainingResponse(training)), responses);
    }

    @Test
    void toResponses_convertsEachTrainingType() {
        assertEquals(List.of(new TrainingTypeResponse(2L, TrainingTypeName.YOGA)),
                TrainingMapper.toResponses(List.of(YOGA)));
    }

    private static Training training() {
        return Training.builder()
                .trainingName("Morning yoga")
                .trainingDate(DATE)
                .trainingDuration(60)
                .trainingType(YOGA)
                .trainee(Trainee.builder().user(user("Emily", "Johnson")).build())
                .trainer(Trainer.builder().specialization(YOGA).user(user("Anna", "Lee")).build())
                .build();
    }

    private static User user(String firstName, String lastName) {
        return User.builder().firstName(firstName).lastName(lastName).build();
    }
}
