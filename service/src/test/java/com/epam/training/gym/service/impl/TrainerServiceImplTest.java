package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TrainerDao;
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
public class TrainerServiceImplTest {
    @Mock
    private TrainerDao trainerDao;

    @Mock
    private NewUserDataGenerator newUserDataGenerator;

    private TrainerServiceImpl trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setNewUserDataGenerator(newUserDataGenerator);
    }

    @Test
    void create_fillsGeneratedDataAndSaves() {
        Trainer chris = Trainer.builder()
                .firstName("Chris")
                .lastName("Taylor")
                .specialization(new TrainingType(3L, TrainingTypeName.ZUMBA))
                .build();
        when(trainerDao.save(chris)).thenReturn(chris);

        Trainer created = trainerService.create(chris);

        assertSame(chris, created);
        verify(newUserDataGenerator).fillNewUserData(chris);
        verify(trainerDao).save(chris);
    }

    @Test
    void update_keepsGeneratedDataOfStoredTrainer() {
        Trainer stored = Trainer.builder()
                .id(1L).userId(7L).username("Chris.Taylor").password("Secret1234").firstName("Chris")
                .build();
        Trainer changes = Trainer.builder().id(1L).firstName("Christopher").build();
        when(trainerDao.findById(1L)).thenReturn(Optional.of(stored));
        when(trainerDao.update(changes)).thenReturn(changes);

        Trainer updated = trainerService.update(changes);

        assertEquals("Christopher", updated.getFirstName());
        assertEquals(7L, updated.getUserId());
        assertEquals("Chris.Taylor", updated.getUsername());
        assertEquals("Secret1234", updated.getPassword());
    }

    @Test
    void update_unknownId_throwsExceptionAndDoesNotSave() {
        Trainer unknown = Trainer.builder().id(99L).build();
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.update(unknown));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void update_withoutId_throwsException() {
        Trainer withoutId = Trainer.builder().firstName("Chris").build();

        assertThrows(NullPointerException.class, () -> trainerService.update(withoutId));
    }

    @Test
    void findById_existingId_returnsTrainer() {
        Trainer chris = Trainer.builder().id(1L).build();
        when(trainerDao.findById(1L)).thenReturn(Optional.of(chris));

        assertSame(chris, trainerService.findById(1L));
    }

    @Test
    void findById_unknownId_throwsException() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.findById(99L));
    }

    @Test
    void findAll_returnsTrainersFromDao() {
        List<Trainer> trainers = List.of(Trainer.builder().id(1L).build());
        when(trainerDao.findAll()).thenReturn(trainers);

        assertEquals(trainers, trainerService.findAll());
    }
}
