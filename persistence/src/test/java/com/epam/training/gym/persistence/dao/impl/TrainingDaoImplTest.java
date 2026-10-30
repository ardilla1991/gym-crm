package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TrainingDaoImplTest {
    private Map<Long, Training> storage;
    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void setUp() {
        storage = new ConcurrentHashMap<>();
        trainingDao = new TrainingDaoImpl();
        trainingDao.setStorage(storage);
    }

    @Test
    void save_assignsNextIdAndStoresTraining() {
        storage.put(1L, training(1L, "Morning yoga"));

        Training saved = trainingDao.save(training(null, "Evening zumba"));

        assertEquals(2L, saved.getId());
        assertSame(saved, storage.get(2L));
    }

    @Test
    void findById_existingId_returnsTraining() {
        Training yoga = training(1L, "Morning yoga");
        storage.put(1L, yoga);

        assertSame(yoga, trainingDao.findById(1L).orElseThrow());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(trainingDao.findById(99L).isEmpty());
    }

    @Test
    void findAll_returnsAllTrainings() {
        storage.put(1L, training(1L, "Morning yoga"));
        storage.put(2L, training(2L, "Full body workout"));

        assertEquals(2, trainingDao.findAll().size());
    }

    private static Training training(Long id, String name) {
        return Training.builder().id(id).trainingName(name).build();
    }
}
