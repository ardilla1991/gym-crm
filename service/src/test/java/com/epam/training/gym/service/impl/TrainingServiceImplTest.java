package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Training;
import com.epam.training.gym.persistence.dao.TrainingDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrainingServiceImplTest {
    @Mock
    private TrainingDao trainingDao;

    private TrainingServiceImpl trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl();
        trainingService.setTrainingDao(trainingDao);
    }

    @Test
    void create_savesTraining() {
        Training yoga = Training.builder().trainingName("Morning yoga").build();
        when(trainingDao.save(yoga)).thenReturn(yoga);

        assertSame(yoga, trainingService.create(yoga));
        verify(trainingDao).save(yoga);
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

        assertThrows(NoSuchElementException.class, () -> trainingService.findById(99L));
    }

    @Test
    void findAll_returnsTrainingsFromDao() {
        List<Training> trainings = List.of(Training.builder().id(1L).build());
        when(trainingDao.findAll()).thenReturn(trainings);

        assertEquals(trainings, trainingService.findAll());
    }
}
