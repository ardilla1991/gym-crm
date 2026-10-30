package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TrainerDaoImplTest {
    private Map<Long, Trainer> storage;
    private TrainerDaoImpl trainerDao;

    @BeforeEach
    void setUp() {
        storage = new ConcurrentHashMap<>();
        trainerDao = new TrainerDaoImpl();
        trainerDao.setStorage(storage);
    }

    @Test
    void save_emptyStorage_assignsIdOne() {
        Trainer saved = trainerDao.save(trainer(null, "David"));

        assertEquals(1L, saved.getId());
        assertSame(saved, storage.get(1L));
    }

    @Test
    void update_existingTrainer_replacesIt() {
        storage.put(1L, trainer(1L, "David"));

        trainerDao.update(trainer(1L, "Dave"));

        assertEquals("Dave", storage.get(1L).getFirstName());
    }

    @Test
    void update_unknownId_throwsException() {
        Trainer unknown = trainer(99L, "Nobody");

        assertThrows(IllegalArgumentException.class, () -> trainerDao.update(unknown));
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(trainerDao.findById(99L).isEmpty());
    }

    @Test
    void findAll_returnsAllTrainers() {
        storage.put(1L, trainer(1L, "David"));
        storage.put(2L, trainer(2L, "Jessica"));

        assertEquals(2, trainerDao.findAll().size());
    }

    private static Trainer trainer(Long id, String firstName) {
        return Trainer.builder().id(id).firstName(firstName).build();
    }
}
