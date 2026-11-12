package com.epam.training.gym.api.facade;

import com.epam.training.gym.domain.*;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.domain.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.TrainerService;
import com.epam.training.gym.service.TrainingService;
import com.epam.training.gym.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The facade only delegates, so every test checks that the call reaches the right service.
 */
@ExtendWith(MockitoExtension.class)
public class GymFacadeTest {

    private static final Credentials CREDENTIALS = new Credentials("John.Smith", "abcdefghij");
    private static final String NEW_PASSWORD = "newPass123";
    private static final String OTHER_USERNAME = "Mary.Jones";

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private UserService userService;

    private GymFacade facade;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService, userService);
    }

    // ---------- Trainee ----------

    @Test
    void createTrainee_delegatesToService() {
        Trainee trainee = new Trainee();
        when(traineeService.create(trainee)).thenReturn(trainee);

        assertSame(trainee, facade.createTrainee(trainee));
    }

    @Test
    void matchTraineeCredentials_returnsServiceResult() {
        when(traineeService.matchCredentials(CREDENTIALS)).thenReturn(true);

        assertTrue(facade.matchTraineeCredentials(CREDENTIALS));
    }

    @Test
    void changeTraineeActiveStatus_delegatesToService() {
        facade.changeTraineeActiveStatus(CREDENTIALS, false);

        verify(traineeService).changeActiveStatus(CREDENTIALS, false);
    }

    // ---------- Trainer ----------

    @Test
    void createTrainer_delegatesToService() {
        Trainer trainer = new Trainer();
        when(trainerService.create(trainer)).thenReturn(trainer);

        assertSame(trainer, facade.createTrainer(trainer));
    }

    @Test
    void matchTrainerCredentials_returnsServiceResult() {
        when(trainerService.matchCredentials(CREDENTIALS)).thenReturn(false);

        assertFalse(facade.matchTrainerCredentials(CREDENTIALS));
    }

    @Test
    void changeTrainerActiveStatus_delegatesToService() {
        facade.changeTrainerActiveStatus(CREDENTIALS, true);

        verify(trainerService).changeActiveStatus(CREDENTIALS, true);
    }

    // ---------- profiles ----------

    @Test
    void getTraineeProfile_returnsServiceResult() {
        Trainee trainee = new Trainee();
        when(traineeService.getProfile(CREDENTIALS, OTHER_USERNAME)).thenReturn(trainee);

        assertSame(trainee, facade.getTraineeProfile(CREDENTIALS, OTHER_USERNAME));
    }

    @Test
    void getTrainerProfile_returnsServiceResult() {
        Trainer trainer = new Trainer();
        when(trainerService.getProfile(CREDENTIALS, OTHER_USERNAME)).thenReturn(trainer);

        assertSame(trainer, facade.getTrainerProfile(CREDENTIALS, OTHER_USERNAME));
    }

// ---------- update, delete ----------

    @Test
    void updateTrainee_returnsServiceResult() {
        Trainee newData = new Trainee();
        Trainee updated = new Trainee();
        when(traineeService.update(CREDENTIALS, newData)).thenReturn(updated);

        assertSame(updated, facade.updateTrainee(CREDENTIALS, newData));
    }

    @Test
    void updateTrainer_returnsServiceResult() {
        Trainer newData = new Trainer();
        Trainer updated = new Trainer();
        when(trainerService.update(CREDENTIALS, newData)).thenReturn(updated);

        assertSame(updated, facade.updateTrainer(CREDENTIALS, newData));
    }

    @Test
    void deleteTrainee_delegatesToService() {
        facade.deleteTrainee(CREDENTIALS);

        verify(traineeService).delete(CREDENTIALS);
    }

// ---------- trainings ----------

    @Test
    void getTraineeTrainings_returnsServiceResult() {
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder().build();
        List<Training> trainings = List.of(new Training());
        when(traineeService.getTrainings(CREDENTIALS, criteria)).thenReturn(trainings);

        assertSame(trainings, facade.getTraineeTrainings(CREDENTIALS, criteria));
    }

    @Test
    void getTrainerTrainings_returnsServiceResult() {
        TrainerTrainingCriteria criteria = TrainerTrainingCriteria.builder().build();
        List<Training> trainings = List.of(new Training());
        when(trainerService.getTrainings(CREDENTIALS, criteria)).thenReturn(trainings);

        assertSame(trainings, facade.getTrainerTrainings(CREDENTIALS, criteria));
    }

    @Test
    void addTraining_returnsServiceResult() {
        Training training = new Training();
        when(trainingService.addTraining(CREDENTIALS, "Anna.Lee", training)).thenReturn(training);

        assertSame(training, facade.addTraining(CREDENTIALS, "Anna.Lee", training));
    }

// ---------- trainee's trainers ----------

    @Test
    void getUnassignedTrainers_returnsServiceResult() {
        List<Trainer> trainers = List.of(new Trainer());
        when(traineeService.getUnassignedTrainers(CREDENTIALS)).thenReturn(trainers);

        assertSame(trainers, facade.getUnassignedTrainers(CREDENTIALS));
    }

    @Test
    void updateTraineeTrainers_returnsServiceResult() {
        List<String> usernames = List.of("Anna.Lee");
        List<Trainer> trainers = List.of(new Trainer());
        when(traineeService.updateTrainers(CREDENTIALS, usernames)).thenReturn(trainers);

        assertSame(trainers, facade.updateTraineeTrainers(CREDENTIALS, usernames));
    }

    @Test
    void changePassword_delegatesToUserService() {
        facade.changePassword(CREDENTIALS, NEW_PASSWORD);

        verify(userService).changePassword(CREDENTIALS, NEW_PASSWORD);
    }

    @Test
    void getTrainingTypes_returnsServiceResult() {
        List<TrainingType> types = List.of(new TrainingType(2L, TrainingTypeName.YOGA));
        when(trainingService.getTrainingTypes(CREDENTIALS)).thenReturn(types);

        assertSame(types, facade.getTrainingTypes(CREDENTIALS));
    }
}
