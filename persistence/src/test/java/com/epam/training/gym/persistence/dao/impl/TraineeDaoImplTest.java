package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TraineeDaoImplTest {

    private Map<Long, Trainee> storage;
    private TraineeDaoImpl traineeDao;

    @BeforeEach
    void setUp() {
        storage = new ConcurrentHashMap<>();
        traineeDao = new TraineeDaoImpl();
        traineeDao.setStorage(storage);
    }

    @Test
    void save_assignsNextIdAndStoresTrainee() {
        storage.put(3L, trainee(3L, "Emily"));

        Trainee saved = traineeDao.save(trainee(null, "Michael"));

        assertEquals(4L, saved.getId());
        assertSame(saved, storage.get(4L));
    }

    @Test
    void update_existingTrainee_replacesIt() {
        storage.put(1L, trainee(1L, "Emily"));
        Trainee changed = trainee(1L, "Emma");

        traineeDao.update(changed);

        assertEquals("Emma", storage.get(1L).getFirstName());
    }

    @Test
    void update_unknownId_throwsException() {
        Trainee unknown = trainee(99L, "Nobody");

        assertThrows(IllegalArgumentException.class, () -> traineeDao.update(unknown));
    }

    @Test
    void update_nullId_throwsException() {
        Trainee withoutId = trainee(null, "Nobody");

        assertThrows(IllegalArgumentException.class, () -> traineeDao.update(withoutId));
    }

    @Test
    void delete_removesTrainee() {
        storage.put(1L, trainee(1L, "Emily"));

        traineeDao.delete(1L);

        assertFalse(storage.containsKey(1L));
    }

    @Test
    void findById_existingId_returnsTrainee() {
        Trainee emily = trainee(1L, "Emily");
        storage.put(1L, emily);

        assertSame(emily, traineeDao.findById(1L).orElseThrow());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertTrue(traineeDao.findById(99L).isEmpty());
    }

    @Test
    void findAll_returnsCopyOfStoredTrainees() {
        storage.put(1L, trainee(1L, "Emily"));
        storage.put(2L, trainee(2L, "Michael"));

        List<Trainee> all = traineeDao.findAll();
        all.clear();

        assertEquals(2, storage.size());
    }

    private static Trainee trainee(Long id, String firstName) {
        return Trainee.builder().id(id).firstName(firstName).build();
    }
}
