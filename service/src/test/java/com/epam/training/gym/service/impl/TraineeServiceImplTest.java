package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
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
        when(newUserDataGenerator.nextUserId()).thenReturn(10L);
        when(newUserDataGenerator.generateUsername("Emily", "Johnson")).thenReturn("Emily.Johnson1");
        when(newUserDataGenerator.generatePassword()).thenReturn("Abc123xyz9");
        when(traineeDao.save(emily)).thenReturn(emily);

        Trainee created = traineeService.create(emily);

        assertEquals(10L, created.getUserId());
        assertEquals("Emily.Johnson1", created.getUsername());
        assertEquals("Abc123xyz9", created.getPassword());
        assertTrue(created.isActive());
        verify(traineeDao).save(emily);
    }

    @Test
    void update_delegatesToDao() {
        Trainee emily = Trainee.builder().id(1L).firstName("Emily").build();
        when(traineeDao.update(emily)).thenReturn(emily);

        assertSame(emily, traineeService.update(emily));
        verify(traineeDao).update(emily);
    }

    @Test
    void delete_delegatesToDao() {
        traineeService.delete(1L);

        verify(traineeDao).delete(1L);
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

        assertThrows(NoSuchElementException.class, () -> traineeService.findById(99L));
    }

    @Test
    void findAll_returnsTraineesFromDao() {
        List<Trainee> trainees = List.of(Trainee.builder().id(1L).build());
        when(traineeDao.findAll()).thenReturn(trainees);

        assertEquals(trainees, traineeService.findAll());
    }
}
