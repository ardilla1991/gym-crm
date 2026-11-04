package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.TrainerDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.training.gym.persistence.dao.TestEntities.newTrainer;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
public class TrainerDaoImplTest {

    private static final Long YOGA_ID = 2L;

    @Autowired
    private TrainerDao trainerDao;

    @PersistenceContext
    private EntityManager entityManager;

    private TrainingType yoga;

    @BeforeEach
    void loadTrainingType() {
        yoga = entityManager.find(TrainingType.class, YOGA_ID);
    }

    @Test
    void save_assignsIdsToTrainerAndUser() {
        Trainer saved = trainerDao.save(newTrainer("Anna.Lee", yoga));

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser().getId());
    }

    @Test
    void findByUsername_existing_returnsTrainerWithSpecialization() {
        trainerDao.save(newTrainer("Anna.Lee", yoga));
        flushAndClear();

        Trainer found = trainerDao.findByUsername("Anna.Lee").orElseThrow();

        assertEquals("Anna", found.getUser().getFirstName());
        assertEquals(TrainingTypeName.YOGA, found.getSpecialization().getTrainingTypeName());
    }

    @Test
    void findByUsername_unknown_returnsEmpty() {
        assertTrue(trainerDao.findByUsername("User.Name").isEmpty());
    }

    @Test
    void findByUsernames_returnsOnlyRequestedTrainers() {
        trainerDao.save(newTrainer("Anna.Lee", yoga));
        trainerDao.save(newTrainer("Mark.Hill", yoga));
        trainerDao.save(newTrainer("Bob.Ross", yoga));
        flushAndClear();

        List<Trainer> found = trainerDao.findByUsernames(List.of("Anna.Lee", "Bob.Ross"));

        Set<String> usernames = found.stream()
                .map(trainer -> trainer.getUser().getUsername())
                .collect(Collectors.toSet());
        assertEquals(Set.of("Anna.Lee", "Bob.Ross"), usernames);
    }

    @Test
    void findByUsernames_emptyList_returnsEmptyList() {
        assertTrue(trainerDao.findByUsernames(List.of()).isEmpty());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

}
