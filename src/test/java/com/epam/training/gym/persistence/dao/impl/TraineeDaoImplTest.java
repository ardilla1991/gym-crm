package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.*;
import com.epam.training.gym.persistence.dao.QueryCounter;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.TraineeDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.epam.training.gym.persistence.dao.TestEntities.*;
import static org.junit.jupiter.api.Assertions.*;

/** Each test runs in a transaction that is rolled back at the end, so tests do not affect each other. */
@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
public class TraineeDaoImplTest {

    private static final Long YOGA_ID = 2L;

    @Autowired
    private TraineeDao traineeDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void save_assignsIdsToTraineeAndUser() {
        Trainee saved = traineeDao.save(newTrainee("John.Smith"));

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser().getId());
    }

    @Test
    void findByUsername_existing_returnsTraineeFromDatabase() {
        traineeDao.save(newTrainee("John.Smith"));
        flushAndClear();

        Trainee found = traineeDao.findByUsername("John.Smith").orElseThrow();

        assertEquals("John", found.getUser().getFirstName());
        assertEquals(LocalDate.of(1990, 1, 15), found.getDateOfBirth());
    }

    @Test
    void findByUsername_unknown_returnsEmpty() {
        assertTrue(traineeDao.findByUsername("User.Name").isEmpty());
    }

    @Test
    void delete_alsoDeletesUserAndTrainings() {
        Trainee trainee = traineeDao.save(newTrainee("John.Smith"));
        Long userId = trainee.getUser().getId();
        Long trainingId = saveTrainingFor(trainee).getId();
        flushAndClear();

        Trainee loaded = traineeDao.findByUsername("John.Smith").orElseThrow();
        traineeDao.delete(loaded);
        flushAndClear();

        assertTrue(traineeDao.findByUsername("John.Smith").isEmpty());
        assertNull(entityManager.find(User.class, userId));
        assertNull(entityManager.find(Training.class, trainingId));
    }

    @Test
    void findByUsernameWithTrainers_loadsTrainersInSameQuery() {
        TrainingType yoga = entityManager.find(TrainingType.class, YOGA_ID);
        Trainer anna = newTrainer("Anna.Lee", yoga);
        entityManager.persist(anna);
        Trainee trainee = newTrainee("John.Smith");
        trainee.getTrainers().add(anna);
        traineeDao.save(trainee);
        flushAndClear();
        QueryCounter queries = new QueryCounter(entityManager);

        Trainee found = traineeDao.findByUsernameWithTrainers("John.Smith").orElseThrow();
        found.getTrainers().forEach(trainer -> trainer.getUser().getUsername());

        assertEquals(1, found.getTrainers().size());
        assertEquals(1, queries.count());
    }

    @Test
    void findByUsernameWithTrainers_traineeWithoutTrainers_isFound() {
        traineeDao.save(newTrainee("John.Smith"));
        flushAndClear();

        Trainee found = traineeDao.findByUsernameWithTrainers("John.Smith").orElseThrow();

        assertTrue(found.getTrainers().isEmpty());
    }

    /** Sends pending SQL to the database and forgets loaded objects, so the next read really queries the DB. */
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private Training saveTrainingFor(Trainee trainee) {
        TrainingType yoga = entityManager.find(TrainingType.class, YOGA_ID);
        Trainer trainer = Trainer.builder()
                .specialization(yoga)
                .user(newUser("Anna", "Lee", "Anna.Lee"))
                .build();
        entityManager.persist(trainer);

        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning yoga")
                .trainingType(yoga)
                .trainingDate(LocalDate.of(2026, 1, 10))
                .trainingDuration(60)
                .build();
        entityManager.persist(training);

        return training;
    }

}
