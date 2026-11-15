package com.epam.training.gym.api.rest.mapper;

import com.epam.training.gym.api.rest.dto.TraineeProfileResponse;
import com.epam.training.gym.api.rest.dto.TraineeRegistrationRequest;
import com.epam.training.gym.api.rest.dto.UpdateTraineeRequest;
import com.epam.training.gym.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TraineeMapperTest {

    private static final LocalDate BIRTH_DATE = LocalDate.of(1995, 4, 12);

    @Test
    void toModel_registration_copiesNamesDateAndAddress() {
        Trainee trainee = TraineeMapper.toModel(
                new TraineeRegistrationRequest("Emily", "Johnson", BIRTH_DATE, "12 Oak Street"));

        assertEquals("Emily", trainee.getUser().getFirstName());
        assertEquals("Johnson", trainee.getUser().getLastName());
        assertEquals(BIRTH_DATE, trainee.getDateOfBirth());
        assertEquals("12 Oak Street", trainee.getAddress());
        assertNull(trainee.getUser().getUsername());        // generated later by the service
    }

    @Test
    void toModel_update_copiesActiveStatus() {
        Trainee trainee = TraineeMapper.toModel(
                new UpdateTraineeRequest("Emily", "Brown", BIRTH_DATE, "1 Pine Road", false));

        assertEquals("Brown", trainee.getUser().getLastName());
        assertFalse(trainee.getUser().isActive());
    }

    @Test
    void toProfileResponse_containsTrainersSortedByUsername() {
        Trainee trainee = Trainee.builder()
                .dateOfBirth(BIRTH_DATE)
                .address("12 Oak Street")
                .user(user("Emily", "Johnson", "Emily.Johnson"))
                .build();
        trainee.getTrainers().add(trainer("Mark", "Hill", "Mark.Hill"));
        trainee.getTrainers().add(trainer("Anna", "Lee", "Anna.Lee"));

        TraineeProfileResponse response = TraineeMapper.toProfileResponse(trainee);

        assertEquals("Emily.Johnson", response.username());
        assertEquals(BIRTH_DATE, response.dateOfBirth());
        assertEquals(List.of("Anna.Lee", "Mark.Hill"),
                response.trainers().stream().map(t -> t.username()).toList());
        assertEquals(TrainingTypeName.YOGA, response.trainers().get(0).specialization());
    }

    private static User user(String firstName, String lastName, String username) {
        return User.builder().firstName(firstName).lastName(lastName).username(username).isActive(true).build();
    }

    private static Trainer trainer(String firstName, String lastName, String username) {
        return Trainer.builder()
                .specialization(new TrainingType(2L, TrainingTypeName.YOGA))
                .user(user(firstName, lastName, username))
                .build();
    }
}
