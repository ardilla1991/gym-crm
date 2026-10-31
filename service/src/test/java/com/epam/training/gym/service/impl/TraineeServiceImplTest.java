package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
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
public class TraineeServiceImplTest {
    @Mock
    private TraineeDao traineeDao;

    @Mock
    private NewUserDataGenerator newUserDataGenerator;

    private TraineeServiceImpl traineeService;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setNewUserDataGenerator(newUserDataGenerator);
    }

    @Test
    void create_fillsGeneratedDataAndSaves() {
        Trainee emily = Trainee.builder().firstName("Emily").lastName("Johnson").build();
        when(traineeDao.save(emily)).thenReturn(emily);

        Trainee created = traineeService.create(emily);

        assertSame(emily, created);
        verify(newUserDataGenerator).fillNewUserData(emily);
        verify(traineeDao).save(emily);
    }

    @Test
    void update_keepsGeneratedDataOfStoredTrainee() {
        Trainee stored = Trainee.builder()
                .id(1L).userId(5L).username("Emily.Johnson").password("Secret1234").firstName("Emily")
                .build();
        Trainee changes = Trainee.builder().id(1L).firstName("Emma").build();
        when(traineeDao.findById(1L)).thenReturn(Optional.of(stored));
        when(traineeDao.update(changes)).thenReturn(changes);

        Trainee updated = traineeService.update(changes);

        assertEquals("Emma", updated.getFirstName());
        assertEquals(5L, updated.getUserId());
        assertEquals("Emily.Johnson", updated.getUsername());
        assertEquals("Secret1234", updated.getPassword());
    }

    @Test
    void update_unknownId_throwsExceptionAndDoesNotSave() {
        Trainee unknown = Trainee.builder().id(99L).build();
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> traineeService.update(unknown));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void update_withoutId_throwsException() {
        Trainee withoutId = Trainee.builder().firstName("Emily").build();

        assertThrows(NullPointerException.class, () -> traineeService.update(withoutId));
    }

    @Test
    void delete_existingTrainee_deletesIt() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(Trainee.builder().id(1L).build()));

        traineeService.delete(1L);

        verify(traineeDao).delete(1L);
    }

    @Test
    void delete_unknownTrainee_doesNothing() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        traineeService.delete(99L);

        verify(traineeDao, never()).delete(anyLong());
    }

    @Test
    void findById_existingId_returnsTrainee() {
        Trainee emily = Trainee.builder().id(1L).build();
        when(traineeDao.findById(1L)).thenReturn(Optional.of(emily));

        assertSame(emily, traineeService.findById(1L));
    }

    @Test
    void findById_unknownId_throwsException() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> traineeService.findById(99L));
    }

    @Test
    void findAll_returnsTraineesFromDao() {
        List<Trainee> trainees = List.of(Trainee.builder().id(1L).build());
        when(traineeDao.findAll()).thenReturn(trainees);

        assertEquals(trainees, traineeService.findAll());
    }
}
