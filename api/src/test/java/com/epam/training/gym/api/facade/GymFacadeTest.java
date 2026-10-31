package com.epam.training.gym.api.facade;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.TrainerService;
import com.epam.training.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The facade only delegates, so every test checks that the call reaches the right service.
 */
@ExtendWith(MockitoExtension.class)
public class GymFacadeTest {
    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    private GymFacade facade;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService);
    }

    // ---------- Trainee ----------

    @Test
    void createTrainee_delegatesToService() {
        Trainee trainee = new Trainee();
        when(traineeService.create(trainee)).thenReturn(trainee);

        assertSame(trainee, facade.createTrainee(trainee));
    }

    @Test
    void updateTrainee_delegatesToService() {
        Trainee trainee = new Trainee();
        when(traineeService.update(trainee)).thenReturn(trainee);

        assertSame(trainee, facade.updateTrainee(trainee));
    }

    @Test
    void deleteTrainee_delegatesToService() {
        facade.deleteTrainee(1L);

        verify(traineeService).delete(1L);
    }

    @Test
    void findTrainee_delegatesToService() {
        Trainee trainee = new Trainee();
        when(traineeService.findById(1L)).thenReturn(trainee);

        assertSame(trainee, facade.findTrainee(1L));
    }

    @Test
    void findAllTrainees_delegatesToService() {
        List<Trainee> trainees = List.of(new Trainee());
        when(traineeService.findAll()).thenReturn(trainees);

        assertSame(trainees, facade.findAllTrainees());
    }

    // ---------- Trainer ----------

    @Test
    void createTrainer_delegatesToService() {
        Trainer trainer = new Trainer();
        when(trainerService.create(trainer)).thenReturn(trainer);

        assertSame(trainer, facade.createTrainer(trainer));
    }

    @Test
    void updateTrainer_delegatesToService() {
        Trainer trainer = new Trainer();
        when(trainerService.update(trainer)).thenReturn(trainer);

        assertSame(trainer, facade.updateTrainer(trainer));
    }

    @Test
    void findTrainer_delegatesToService() {
        Trainer trainer = new Trainer();
        when(trainerService.findById(1L)).thenReturn(trainer);

        assertSame(trainer, facade.findTrainer(1L));
    }

    @Test
    void findAllTrainers_delegatesToService() {
        List<Trainer> trainers = List.of(new Trainer());
        when(trainerService.findAll()).thenReturn(trainers);

        assertSame(trainers, facade.findAllTrainers());
    }

    // ---------- Training ----------

    @Test
    void createTraining_delegatesToService() {
        Training training = new Training();
        when(trainingService.create(training)).thenReturn(training);

        assertSame(training, facade.createTraining(training));
    }

    @Test
    void findTraining_delegatesToService() {
        Training training = new Training();
        when(trainingService.findById(1L)).thenReturn(training);

        assertSame(training, facade.findTraining(1L));
    }

    @Test
    void findAllTrainings_delegatesToService() {
        List<Training> trainings = List.of(new Training());
        when(trainingService.findAll()).thenReturn(trainings);

        assertSame(trainings, facade.findAllTrainings());
    }
}
