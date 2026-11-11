package com.epam.training.gym.api.rest.mapper;

import com.epam.training.gym.api.rest.dto.TrainerProfileResponse;
import com.epam.training.gym.api.rest.dto.TrainerRegistrationRequest;
import com.epam.training.gym.api.rest.dto.TrainerSummary;
import com.epam.training.gym.api.rest.dto.UpdateTrainerRequest;
import com.epam.training.gym.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrainerMapperTest {

    @Test
    void toEntity_registration_setsSpecializationByNameOnly() {
        Trainer trainer = TrainerMapper.toEntity(
                new TrainerRegistrationRequest("Anna", "Lee", TrainingTypeName.ZUMBA));

        assertEquals("Anna", trainer.getUser().getFirstName());
        assertEquals(TrainingTypeName.ZUMBA, trainer.getSpecialization().getTrainingTypeName());
        assertNull(trainer.getSpecialization().getId());     // the service loads the real type
    }

    @Test
    void toEntity_update_hasNoSpecialization() {
        Trainer trainer = TrainerMapper.toEntity(new UpdateTrainerRequest("Annie", "Lee", true));

        assertEquals("Annie", trainer.getUser().getFirstName());
        assertTrue(trainer.getUser().isActive());
        assertNull(trainer.getSpecialization());
    }

    @Test
    void toProfileResponse_containsTraineesSortedByUsername() {
        Trainer trainer = Trainer.builder()
                .specialization(new TrainingType(2L, TrainingTypeName.YOGA))
                .user(user("Anna", "Lee", "Anna.Lee"))
                .build();
        trainer.getTrainees().add(Trainee.builder().user(user("Mary", "Jones", "Mary.Jones")).build());
        trainer.getTrainees().add(Trainee.builder().user(user("John", "Smith", "John.Smith")).build());

        TrainerProfileResponse response = TrainerMapper.toProfileResponse(trainer);

        assertEquals(TrainingTypeName.YOGA, response.specialization());
        assertEquals(List.of("John.Smith", "Mary.Jones"),
                response.trainees().stream().map(t -> t.username()).toList());
    }

    @Test
    void toSummaries_sortsByUsername() {
        TrainingType yoga = new TrainingType(2L, TrainingTypeName.YOGA);
        List<TrainerSummary> summaries = TrainerMapper.toSummaries(List.of(
                Trainer.builder().specialization(yoga).user(user("Mark", "Hill", "Mark.Hill")).build(),
                Trainer.builder().specialization(yoga).user(user("Anna", "Lee", "Anna.Lee")).build()));

        assertEquals(List.of("Anna.Lee", "Mark.Hill"), summaries.stream().map(TrainerSummary::username).toList());
    }

    private static User user(String firstName, String lastName, String username) {
        return User.builder().firstName(firstName).lastName(lastName).username(username).isActive(true).build();
    }
}
