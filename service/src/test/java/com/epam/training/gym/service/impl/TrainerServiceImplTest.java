package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
import com.epam.training.gym.domain.User;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private EntityValidator entityValidator;

    @Mock
    private NewUserDataGenerator newUserDataGenerator;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    @Test
    void create_usesTrainingTypeFromDatabase() {
        TrainingType zumbaFromDb = new TrainingType(3L, TrainingTypeName.ZUMBA);
        Trainer trainer = newTrainer(TrainingTypeName.ZUMBA);
        when(trainingTypeDao.findByName(TrainingTypeName.ZUMBA)).thenReturn(Optional.of(zumbaFromDb));
        when(trainerDao.save(trainer)).thenReturn(trainer);

        Trainer result = trainerService.create(trainer);

        assertSame(zumbaFromDb, result.getSpecialization());
        verify(entityValidator).validate(trainer);
        verify(newUserDataGenerator).fillNewUserData(trainer.getUser());
    }

    @Test
    void create_unknownTrainingType_isNotSaved() {
        Trainer trainer = newTrainer(TrainingTypeName.ZUMBA);
        when(trainingTypeDao.findByName(TrainingTypeName.ZUMBA)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.create(trainer));

        verify(trainerDao, never()).save(any());
    }

    /** The specialization is built by hand, without id: like a caller would pass it. */
    private static Trainer newTrainer(TrainingTypeName specialization) {
        return Trainer.builder()
                .specialization(new TrainingType(null, specialization))
                .user(User.builder().firstName("Anna").lastName("Lee").build())
                .build();
    }
/*
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

     */
}
