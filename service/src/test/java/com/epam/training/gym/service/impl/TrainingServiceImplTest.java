package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.persistence.dao.TrainingDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceImplTest {
    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    private TrainingServiceImpl trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl();
        trainingService.setTrainingDao(trainingDao);
        trainingService.setTraineeDao(traineeDao);
        trainingService.setTrainerDao(trainerDao);
    }

    @Test
    void create_existingTraineeAndTrainer_savesTraining() {
        Training training = Training.builder().traineeId(1L).trainerId(2L).build();
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(new Trainer()));
        when(trainingDao.save(training)).thenReturn(training);

        assertSame(training, trainingService.create(training));
    }

    @Test
    void create_unknownTrainee_savesNothing() {
        Training training = Training.builder().traineeId(99L).trainerId(2L).build();
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainingService.create(training));

        verify(trainingDao, never()).save(any());
    }

    @Test
    void create_unknownTrainer_savesNothing() {
        Training training = Training.builder().traineeId(1L).trainerId(99L).build();
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainingService.create(training));

        verify(trainingDao, never()).save(any());
    }

    @Test
    void findById_existingId_returnsTraining() {
        Training yoga = Training.builder().id(1L).build();
        when(trainingDao.findById(1L)).thenReturn(Optional.of(yoga));

        assertSame(yoga, trainingService.findById(1L));
    }

    @Test
    void findById_unknownId_throwsException() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainingService.findById(99L));
    }

    @Test
    void findAll_returnsTrainingsFromDao() {
        List<Training> trainings = List.of(Training.builder().id(1L).build());
        when(trainingDao.findAll()).thenReturn(trainings);

        assertEquals(trainings, trainingService.findAll());
    }
}
