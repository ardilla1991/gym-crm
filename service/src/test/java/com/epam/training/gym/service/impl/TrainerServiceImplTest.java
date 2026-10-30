package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TrainerDao;
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
        when(newUserDataGenerator.nextUserId()).thenReturn(7L);
        when(newUserDataGenerator.generateUsername("Chris", "Taylor")).thenReturn("Chris.Taylor");
        when(newUserDataGenerator.generatePassword()).thenReturn("Qwe456rty1");
        when(trainerDao.save(chris)).thenReturn(chris);

        Trainer created = trainerService.create(chris);

        assertEquals(7L, created.getUserId());
        assertEquals("Chris.Taylor", created.getUsername());
        assertEquals("Qwe456rty1", created.getPassword());
        assertTrue(created.isActive());
        verify(trainerDao).save(chris);
    }

    @Test
    void update_delegatesToDao() {
        Trainer chris = Trainer.builder().id(1L).firstName("Chris").build();
        when(trainerDao.update(chris)).thenReturn(chris);

        assertSame(chris, trainerService.update(chris));
        verify(trainerDao).update(chris);
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

        assertThrows(NoSuchElementException.class, () -> trainerService.findById(99L));
    }

    @Test
    void findAll_returnsTrainersFromDao() {
        List<Trainer> trainers = List.of(Trainer.builder().id(1L).build());
        when(trainerDao.findAll()).thenReturn(trainers);

        assertEquals(trainers, trainerService.findAll());
    }
}
