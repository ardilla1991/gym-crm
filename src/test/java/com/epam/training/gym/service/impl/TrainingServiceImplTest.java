package com.epam.training.gym.service.impl;

import com.epam.training.gym.metrics.GymMetrics;
import com.epam.training.gym.model.*;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.service.TraineeService;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidStateException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceImplTest {

    private static final Credentials TRAINEE_CREDENTIALS = new Credentials("John.Smith", "abcdefghij");
    private static final String TRAINER_USERNAME = "Anna.Lee";

    @Mock
    private TraineeService traineeService;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private EntityValidator entityValidator;

    @Mock
    private UserAuthenticator userAuthenticator;

    @Mock
    private GymMetrics gymMetrics;

    @InjectMocks
    private TrainingServiceImpl trainingService;

    /** What the trainee service returns after the password check. */
    private Trainee trainee;
    private Trainer trainer;
    private TrainingType yogaFromDb;

    @BeforeEach
    void setUp() {
        // Only objects here, no when(...): with strict stubs every stub must be used by every test
        trainee = Trainee.builder().id(1L).user(User.builder().username("John.Smith").build()).build();
        yogaFromDb = new TrainingType(2L, TrainingTypeName.YOGA);
        trainer = Trainer.builder()
                .id(5L)
                .user(User.builder().username(TRAINER_USERNAME).isActive(true).build())
                .specialization(yogaFromDb)
                .build();
    }

    @Test
    void addTraining_typeIsTrainersSpecialization_linksModelsFromDatabase() {
        stubTraineeAndTrainer();
        Training training = newTraining();
        Training saved = Training.builder().id(10L).build();
        when(trainingDao.save(training)).thenReturn(saved);

        Training result = trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, training);

        // The DAO gets the training linked to the models loaded from the database
        assertSame(trainee, training.getTrainee());
        assertSame(trainer, training.getTrainer());
        assertSame(yogaFromDb, training.getTrainingType());
        assertSame(saved, result);
        verify(entityValidator).validate(training);
        verify(gymMetrics).trainingAdded();
    }

    @Test
    void addTraining_trainerNotInList_isAddedToTraineesTrainers() {
        stubTraineeAndTrainer();
        when(trainingDao.save(any())).thenReturn(Training.builder().id(10L).build());

        trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining());

        verify(traineeDao).addTrainer(trainee, trainer);
    }

    @Test
    void addTraining_wrongTraineeCredentials_savesNothing() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining()));

        verify(trainerDao, never()).findByUsername(any());
        verify(trainingDao, never()).save(any());
        verify(gymMetrics, never()).trainingAdded();
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
    void addTraining_inactiveTrainer_savesNothing() {
        trainer.getUser().setActive(false);
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenReturn(trainee);
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));

        assertThrows(InvalidStateException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining()));

        verify(trainingDao, never()).save(any());
        verify(traineeDao, never()).addTrainer(any(), any());
    }

    @Test
    void addTraining_invalidTraining_savesNothingAndKeepsTrainersList() {
        stubTraineeAndTrainer();
        Training training = newTraining();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(training);

        assertThrows(ConstraintViolationException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, training));

        verify(trainingDao, never()).save(any());
        verify(traineeDao, never()).addTrainer(any(), any());
    }

    @Test
    void getTrainingTypes_returnsAllTypes() {
        when(trainingTypeDao.findAll()).thenReturn(List.of(yogaFromDb));

        List<TrainingType> result = trainingService.getTrainingTypes(TRAINEE_CREDENTIALS);

        assertEquals(List.of(yogaFromDb), result);
        verify(userAuthenticator).authenticate(TRAINEE_CREDENTIALS);
    }

    @Test
    void getTrainingTypes_wrongCredentials_returnsNothing() {
        doThrow(new AuthenticationException()).when(userAuthenticator).authenticate(TRAINEE_CREDENTIALS);

        assertThrows(AuthenticationException.class, () -> trainingService.getTrainingTypes(TRAINEE_CREDENTIALS));

        verify(trainingTypeDao, never()).findAll();
    }

    private void stubTraineeAndTrainer() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenReturn(trainee);
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
    }

    /** A training as the caller passes it: without type, trainee and trainer. */
    private static Training newTraining() {
        return Training.builder()
                .trainingName("Morning yoga")
                .trainingDate(LocalDate.of(2026, 5, 1))
                .trainingDuration(60)
                .build();
    }

}
