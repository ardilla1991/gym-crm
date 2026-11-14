package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.*;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.persistence.entity.*;
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
import org.mockito.ArgumentCaptor;
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

    @InjectMocks
    private TrainingServiceImpl trainingService;

    /** What the trainee service returns after the password check. */
    private Trainee traineeModel;
    private TraineeEntity trainee;
    private TrainerEntity trainer;
    private TrainingTypeEntity yogaFromDb;

    @BeforeEach
    void setUp() {
        // Only objects here, no when(...): with strict stubs every stub must be used by every test
        traineeModel = Trainee.builder().user(User.builder().username("John.Smith").build()).build();
        trainee = TraineeEntity.builder().user(UserEntity.builder().username("John.Smith").build()).build();
        yogaFromDb = new TrainingTypeEntity(2L, TrainingTypeName.YOGA);
        trainer = TrainerEntity.builder()
                .user(UserEntity.builder().username(TRAINER_USERNAME).isActive(true).build())
                .specialization(yogaFromDb)
                .build();
    }

    @Test
    void addTraining_typeIsTrainersSpecialization_linksRowsFromDatabase() {
        stubTraineeAndTrainer();
        when(trainingDao.save(any(TrainingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Training training = newTraining();

        Training result = trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, training);

        // The saved row refers to the rows loaded from the database
        ArgumentCaptor<TrainingEntity> saved = ArgumentCaptor.forClass(TrainingEntity.class);
        verify(trainingDao).save(saved.capture());
        assertSame(trainee, saved.getValue().getTrainee());
        assertSame(trainer, saved.getValue().getTrainer());
        assertSame(yogaFromDb, saved.getValue().getTrainingType());
        // The caller gets the saved training as a model
        assertEquals("John.Smith", result.getTrainee().getUser().getUsername());
        assertEquals(TRAINER_USERNAME, result.getTrainer().getUser().getUsername());
        assertEquals(2L, result.getTrainingType().getId());
        verify(entityValidator).validate(training);
    }

    @Test
    void addTraining_trainerNotInList_isAddedToTraineesTrainers() {
        stubTraineeAndTrainer();
        when(trainingDao.save(any(TrainingEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining());

        assertTrue(trainee.getTrainers().contains(trainer));
    }

    @Test
    void addTraining_wrongTraineeCredentials_savesNothing() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining()));

        verify(traineeDao, never()).findByUsername(any());
        verify(trainerDao, never()).findByUsername(any());
        verify(trainingDao, never()).save(any());
    }

    @Test
    void addTraining_unknownTrainer_savesNothing() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenReturn(traineeModel);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining()));

        verify(trainingDao, never()).save(any());
    }

    @Test
    void addTraining_inactiveTrainer_savesNothing() {
        trainer.getUser().setActive(false);
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenReturn(traineeModel);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));

        assertThrows(InvalidStateException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, newTraining()));

        verify(trainingDao, never()).save(any());
        assertTrue(trainee.getTrainers().isEmpty());
    }

    @Test
    void addTraining_invalidTraining_savesNothingAndKeepsTrainersList() {
        stubTraineeAndTrainer();
        Training training = newTraining();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(training);

        assertThrows(ConstraintViolationException.class,
                () -> trainingService.addTraining(TRAINEE_CREDENTIALS, TRAINER_USERNAME, training));

        verify(trainingDao, never()).save(any());
        assertTrue(trainee.getTrainers().isEmpty());
    }

    @Test
    void getTrainingTypes_returnsAllTypesAsModels() {
        when(trainingTypeDao.findAll()).thenReturn(List.of(yogaFromDb));

        List<TrainingType> result = trainingService.getTrainingTypes(TRAINEE_CREDENTIALS);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
        assertEquals(TrainingTypeName.YOGA, result.get(0).getTrainingTypeName());
        verify(userAuthenticator).authenticate(TRAINEE_CREDENTIALS);
    }

    @Test
    void getTrainingTypes_wrongCredentials_returnsNothing() {
        when(userAuthenticator.authenticate(TRAINEE_CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class, () -> trainingService.getTrainingTypes(TRAINEE_CREDENTIALS));

        verify(trainingTypeDao, never()).findAll();
    }

    private void stubTraineeAndTrainer() {
        when(traineeService.authenticate(TRAINEE_CREDENTIALS)).thenReturn(traineeModel);
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));
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
