package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.persistence.dao.QueryCounter;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.training.gym.persistence.dao.TestEntities.*;
import static com.epam.training.gym.persistence.dao.TestModels.newTraineeModel;
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
        Trainee saved = traineeDao.save(newTraineeModel("John.Smith"));

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser().getId());
    }

    @Test
    void save_storesPasswordButDoesNotReturnIt() {
        Trainee saved = traineeDao.save(newTraineeModel("John.Smith"));
        flushAndClear();

        assertNull(saved.getUser().getPassword());
        assertEquals("abcde12345", entityManager.find(UserEntity.class, saved.getUser().getId()).getPassword());
    }

    @Test
    void findByUsername_existing_returnsTraineeFromDatabase() {
        entityManager.persist(newTrainee("John.Smith"));
        flushAndClear();

        Trainee found = traineeDao.findByUsername("John.Smith").orElseThrow();

        assertEquals("John", found.getUser().getFirstName());
        assertEquals(LocalDate.of(1990, 1, 15), found.getDateOfBirth());
        assertNull(found.getUser().getPassword());
    }

    @Test
    void findByUsername_unknown_returnsEmpty() {
        assertTrue(traineeDao.findByUsername("User.Name").isEmpty());
    }

    @Test
    void update_savesNamesDateOfBirthAndAddress() {
        TraineeEntity entity = newTrainee("John.Smith");
        entityManager.persist(entity);
        flushAndClear();
        Trainee trainee = traineeDao.findByUsername("John.Smith").orElseThrow();
        trainee.getUser().setFirstName("Johnny");
        trainee.getUser().setLastName("Smithson");
        trainee.setDateOfBirth(LocalDate.of(1991, 2, 20));
        trainee.setAddress("2 High Street");

        Trainee updated = traineeDao.update(trainee);
        flushAndClear();

        assertEquals("Johnny", updated.getUser().getFirstName());
        TraineeEntity saved = entityManager.find(TraineeEntity.class, entity.getId());
        assertEquals("Johnny", saved.getUser().getFirstName());
        assertEquals("Smithson", saved.getUser().getLastName());
        assertEquals(LocalDate.of(1991, 2, 20), saved.getDateOfBirth());
        assertEquals("2 High Street", saved.getAddress());
        assertEquals("John.Smith", saved.getUser().getUsername());
        assertEquals("abcde12345", saved.getUser().getPassword());
    }

    @Test
    void delete_alsoDeletesUserAndTrainings() {
        TraineeEntity trainee = newTrainee("John.Smith");
        entityManager.persist(trainee);
        Long userId = trainee.getUser().getId();
        Long trainingId = saveTrainingFor(trainee).getId();
        flushAndClear();

        traineeDao.delete(traineeDao.findByUsername("John.Smith").orElseThrow());
        flushAndClear();

        assertTrue(traineeDao.findByUsername("John.Smith").isEmpty());
        assertNull(entityManager.find(UserEntity.class, userId));
        assertNull(entityManager.find(TrainingEntity.class, trainingId));
    }

    @Test
    void findByUsernameWithTrainers_loadsTrainersInSameQuery() {
        TrainingTypeEntity yoga = entityManager.find(TrainingTypeEntity.class, YOGA_ID);
        TrainerEntity anna = newTrainer("Anna.Lee", yoga);
        entityManager.persist(anna);
        TraineeEntity trainee = newTrainee("John.Smith");
        trainee.getTrainers().add(anna);
        entityManager.persist(trainee);
        flushAndClear();
        QueryCounter queries = new QueryCounter(entityManager);

        Trainee found = traineeDao.findByUsernameWithTrainers("John.Smith").orElseThrow();

        assertEquals(Set.of("Anna.Lee"), usernames(found));
        assertEquals(1, queries.count());
    }

    @Test
    void findByUsernameWithTrainers_traineeWithoutTrainers_isFound() {
        entityManager.persist(newTrainee("John.Smith"));
        flushAndClear();

        Trainee found = traineeDao.findByUsernameWithTrainers("John.Smith").orElseThrow();

        assertTrue(found.getTrainers().isEmpty());
    }

    @Test
    void replaceTrainers_listBecomesExactlyTheGivenTrainers() {
        TrainingTypeEntity yoga = entityManager.find(TrainingTypeEntity.class, YOGA_ID);
        TrainerEntity anna = newTrainer("Anna", "Lee", "Anna.Lee", yoga);
        TrainerEntity mark = newTrainer("Mark", "Hill", "Mark.Hill", yoga);
        entityManager.persist(anna);
        entityManager.persist(mark);
        TraineeEntity trainee = newTrainee("John.Smith");
        trainee.getTrainers().add(anna);
        entityManager.persist(trainee);
        flushAndClear();

        traineeDao.replaceTrainers(traineeDao.findByUsername("John.Smith").orElseThrow(),
                List.of(Trainer.builder().id(mark.getId()).build()));
        flushAndClear();

        assertEquals(Set.of("Mark.Hill"), usernames(traineeDao.findByUsernameWithTrainers("John.Smith").orElseThrow()));
    }

    @Test
    void addTrainer_addsTrainerOnce() {
        TrainingTypeEntity yoga = entityManager.find(TrainingTypeEntity.class, YOGA_ID);
        TrainerEntity anna = newTrainer("Anna.Lee", yoga);
        entityManager.persist(anna);
        entityManager.persist(newTrainee("John.Smith"));
        flushAndClear();
        Trainee trainee = traineeDao.findByUsername("John.Smith").orElseThrow();
        Trainer trainer = Trainer.builder().id(anna.getId()).build();

        traineeDao.addTrainer(trainee, trainer);
        traineeDao.addTrainer(trainee, trainer);
        flushAndClear();

        assertEquals(Set.of("Anna.Lee"), usernames(traineeDao.findByUsernameWithTrainers("John.Smith").orElseThrow()));
    }

    /** Sends pending SQL to the database and forgets loaded objects, so the next read really queries the DB. */
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static Set<String> usernames(Trainee trainee) {
        return trainee.getTrainers().stream()
                .map(trainer -> trainer.getUser().getUsername())
                .collect(Collectors.toSet());
    }

    private TrainingEntity saveTrainingFor(TraineeEntity trainee) {
        TrainingTypeEntity yoga = entityManager.find(TrainingTypeEntity.class, YOGA_ID);
        TrainerEntity trainer = TrainerEntity.builder()
                .specialization(yoga)
                .user(newUser("Anna", "Lee", "Anna.Lee"))
                .build();
        entityManager.persist(trainer);

        TrainingEntity training = TrainingEntity.builder()
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
