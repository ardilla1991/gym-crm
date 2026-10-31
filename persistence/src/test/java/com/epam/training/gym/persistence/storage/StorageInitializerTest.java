package com.epam.training.gym.persistence.storage;

import com.epam.training.gym.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.epam.training.gym.persistence.storage.StorageNames.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Uses small test files from src/test/resources/test-data,
 * not the real application data.
 */
public class StorageInitializerTest {

    private StorageInitializer initializer;

    @BeforeEach
    void setUp() {
        initializer = new StorageInitializer();
        initializer.setTraineeFile(new ClassPathResource("test-data/trainees.json"));
        initializer.setTrainerFile(new ClassPathResource("test-data/trainers.json"));
        initializer.setTrainingFile(new ClassPathResource("test-data/trainings.json"));
        initializer.setTrainingTypeFile(new ClassPathResource("test-data/training-types.json"));
    }

    @Test
    void traineeStorage_isFilledFromFile() {
        Map<Long, Trainee> storage = new ConcurrentHashMap<>();

        Object result = initializer.postProcessAfterInitialization(storage, TRAINEE_STORAGE);

        assertSame(storage, result);
        assertEquals(2, storage.size());
        Trainee john = storage.get(1L);
        assertEquals("John", john.getFirstName());
        assertEquals("John.Doe", john.getUsername());
        assertEquals(LocalDate.of(1990, 5, 20), john.getDateOfBirth());
        assertNull(storage.get(2L).getAddress());
    }

    @Test
    void trainerStorage_isFilledFromFile() {
        Map<Long, Trainer> storage = new ConcurrentHashMap<>();

        initializer.postProcessAfterInitialization(storage, TRAINER_STORAGE);

        assertEquals(1, storage.size());
        assertEquals(TrainingTypeName.YOGA, storage.get(1L).getSpecialization().getTrainingTypeName());
    }

    @Test
    void trainingStorage_isFilledFromFile() {
        Map<Long, Training> storage = new ConcurrentHashMap<>();

        initializer.postProcessAfterInitialization(storage, TRAINING_STORAGE);

        Training training = storage.get(1L);
        assertEquals("Test yoga", training.getTrainingName());
        assertEquals(LocalDate.of(2026, 1, 15), training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());
    }

    @Test
    void trainingTypeStorage_isFilledFromFile() {
        Map<Long, TrainingType> storage = new ConcurrentHashMap<>();

        initializer.postProcessAfterInitialization(storage, TRAINING_TYPE_STORAGE);

        assertEquals(2, storage.size());
        assertEquals(TrainingTypeName.FITNESS, storage.get(1L).getTrainingTypeName());
    }

    @Test
    void otherBean_isReturnedUnchanged() {
        Object someBean = new Object();

        assertSame(someBean, initializer.postProcessAfterInitialization(someBean, "someService"));
    }

    @Test
    void missingFile_throwsException() {
        initializer.setTraineeFile(new ClassPathResource("test-data/missing.json"));
        Map<Long, Trainee> storage = new ConcurrentHashMap<>();

        assertThrows(BeanInitializationException.class,
                () -> initializer.postProcessAfterInitialization(storage, TRAINEE_STORAGE));
    }
}
