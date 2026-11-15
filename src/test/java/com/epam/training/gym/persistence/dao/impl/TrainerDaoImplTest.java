package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.persistence.dao.QueryCounter;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import com.epam.training.gym.persistence.entity.UserEntity;
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

import static com.epam.training.gym.persistence.dao.TestEntities.newTrainee;
import static com.epam.training.gym.persistence.dao.TestEntities.newTrainer;
import static com.epam.training.gym.persistence.dao.TestModels.newTrainerModel;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
public class TrainerDaoImplTest {

    private static final Long YOGA_ID = 2L;

    @Autowired
    private TrainerDao trainerDao;

    @PersistenceContext
    private EntityManager entityManager;

    private TrainingTypeEntity yoga;

    @BeforeEach
    void loadTrainingType() {
        yoga = entityManager.find(TrainingTypeEntity.class, YOGA_ID);
    }

    @Test
    void save_assignsIdsToTrainerAndUser() {
        Trainer saved = trainerDao.save(newTrainerModel("Anna.Lee", YOGA_ID, TrainingTypeName.YOGA));

        assertNotNull(saved.getId());
        assertNotNull(saved.getUser().getId());
        assertNull(saved.getUser().getPassword());
        assertEquals(TrainingTypeName.YOGA, saved.getSpecialization().getTrainingTypeName());
    }

    @Test
    void findByUsername_existing_returnsTrainerWithSpecialization() {
        entityManager.persist(newTrainer("Anna.Lee", yoga));
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
        persist(newTrainer("Anna.Lee", yoga));
        persist(newTrainer("Mark.Hill", yoga));
        persist(newTrainer("Bob.Ross", yoga));
        flushAndClear();

        List<Trainer> found = trainerDao.findByUsernames(List.of("Anna.Lee", "Bob.Ross"));

        assertEquals(Set.of("Anna.Lee", "Bob.Ross"), usernames(found));
    }

    @Test
    void findByUsernames_emptyList_returnsEmptyList() {
        assertTrue(trainerDao.findByUsernames(List.of()).isEmpty());
    }

    @Test
    void findNotAssignedToTrainee_returnsTrainersMissingFromTraineesList() {
        TrainerEntity anna = persist(newTrainer("Anna", "Lee", "Anna.Lee", yoga));
        persist(newTrainer("Mark", "Hill", "Mark.Hill", yoga));
        persist(newTrainer("Bob", "Ross", "Bob.Ross", yoga));
        TraineeEntity john = newTrainee("John.Smith");
        john.getTrainers().add(anna);
        entityManager.persist(john);
        flushAndClear();

        List<Trainer> found = trainerDao.findNotAssignedToTrainee("John.Smith");

        assertEquals(Set.of("Mark.Hill", "Bob.Ross"), usernames(found));
    }

    @Test
    void findNotAssignedToTrainee_traineeWithoutTrainers_returnsAllTrainers() {
        persist(newTrainer("Anna", "Lee", "Anna.Lee", yoga));
        persist(newTrainer("Mark", "Hill", "Mark.Hill", yoga));
        entityManager.persist(newTrainee("John.Smith"));
        flushAndClear();

        List<Trainer> found = trainerDao.findNotAssignedToTrainee("John.Smith");

        assertEquals(Set.of("Anna.Lee", "Mark.Hill"), usernames(found));
    }

    @Test
    void findByUsernameWithTrainees_loadsTraineesInSameQuery() {
        TrainerEntity anna = persist(newTrainer("Anna.Lee", yoga));
        TraineeEntity john = newTrainee("John.Smith");
        john.getTrainers().add(anna);
        entityManager.persist(john);
        flushAndClear();
        QueryCounter queries = new QueryCounter(entityManager);

        Trainer found = trainerDao.findByUsernameWithTrainees("Anna.Lee").orElseThrow();

        assertEquals("John.Smith", found.getTrainees().iterator().next().getUser().getUsername());
        assertEquals(1, found.getTrainees().size());
        assertEquals(1, queries.count());
    }

    @Test
    void findByUsernameWithTrainees_trainerWithoutTrainees_isFound() {
        persist(newTrainer("Anna.Lee", yoga));
        flushAndClear();

        assertTrue(trainerDao.findByUsernameWithTrainees("Anna.Lee").orElseThrow().getTrainees().isEmpty());
    }

    @Test
    void update_savesFirstAndLastNameOnly() {
        TrainerEntity anna = persist(newTrainer("Anna.Lee", yoga));
        flushAndClear();
        Trainer trainer = trainerDao.findByUsername("Anna.Lee").orElseThrow();
        trainer.getUser().setFirstName("Annie");
        trainer.getUser().setLastName("Leeson");

        Trainer updated = trainerDao.update(trainer);
        flushAndClear();

        assertEquals("Annie", updated.getUser().getFirstName());
        UserEntity saved = entityManager.find(TrainerEntity.class, anna.getId()).getUser();
        assertEquals("Annie", saved.getFirstName());
        assertEquals("Leeson", saved.getLastName());
        assertEquals("Anna.Lee", saved.getUsername());
        assertEquals("abcde12345", saved.getPassword());
    }

    private TrainerEntity persist(TrainerEntity trainer) {
        entityManager.persist(trainer);
        return trainer;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static Set<String> usernames(List<Trainer> trainers) {
        return trainers.stream()
                .map(trainer -> trainer.getUser().getUsername())
                .collect(Collectors.toSet());
    }

}
