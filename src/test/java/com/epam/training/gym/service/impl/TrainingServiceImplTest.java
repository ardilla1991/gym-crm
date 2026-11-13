package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.*;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.service.validation.EntityValidator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceImplTest {

    private static final Credentials TRAINEE_CREDENTIALS = new Credentials("John.Smith", "abcdefghij");
    private static final String TRAINER_USERNAME = "Anna.Lee";

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private EntityValidator entityValidator;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    private Trainee trainee;
    private Trainer trainer;
    private TrainingType yogaFromDb;

    @BeforeEach
    void setUp() {
        // Only objects here, no when(...): with strict stubs every stub must be used by every test
        trainee = Trainee.builder().user(User.builder().username("John.Smith").build()).build();
        trainer = Trainer.builder().user(User.builder().username(TRAINER_USERNAME).build()).build();
        yogaFromDb = new TrainingType(2L, TrainingTypeName.YOGA);
    }

    @Test
    void addTraining_linksTraineeTrainerAndTypeFromDatabase() {
        stubTraineeTrainerAndType();
        Training training = newTraining();
        when(trainingDao.save(training)).thenReturn(training);

        Training result = trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, training);

        assertSame(trainee, result.getTrainee());
        assertSame(trainer, result.getTrainer());
        assertSame(yogaFromDb, result.getTrainingType());
        verify(entityValidator).validate(training);
    }

    @Test
    void addTraining_trainerNotInList_isAddedToTraineesTrainers() {
        stubTraineeTrainerAndType();
        Training training = newTraining();
        when(trainingDao.save(training)).thenReturn(training);

        trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, training);

        assertTrue(trainee.getTrainers().contains(trainer));
    }

    @Test
    void addTraining_wrongTraineeCredentials_savesNothing() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining()));

        verify(trainerDao, never()).findByUsername(any());
        verify(trainingDao, never()).save(any());
    }

    @Test
    void addTraining_unknownTrainer_savesNothing() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenReturn(trainee);
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining()));

        verify(trainingDao, never()).save(any());
    }

    @Test
    void addTraining_unknownTrainingType_savesNothing() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenReturn(trainee);
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName(TrainingTypeName.YOGA)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining()));

        verify(trainingDao, never()).save(any());
    }

    @Test
    void addTraining_invalidTraining_savesNothingAndKeepsTrainersList() {
        stubTraineeTrainerAndType();
        Training training = newTraining();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(training);

        assertThrows(ConstraintViolationException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, training));

        verify(trainingDao, never()).save(any());
        assertTrue(trainee.getTrainers().isEmpty());
    }

    private void stubTraineeTrainerAndType() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenReturn(trainee);
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingTypeDao.findByName(TrainingTypeName.YOGA)).thenReturn(Optional.of(yogaFromDb));
    }

    /** A training as a caller passes it: the type is built by hand, without id. */
    private static Training newTraining() {
        return Training.builder()
                .trainingName("Morning yoga")
                .trainingType(new TrainingType(null, TrainingTypeName.YOGA))
                .trainingDate(LocalDate.of(2026, 5, 1))
                .trainingDuration(60)
                .build();
    }

}
