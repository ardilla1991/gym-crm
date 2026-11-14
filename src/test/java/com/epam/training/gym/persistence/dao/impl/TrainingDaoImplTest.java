package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.persistence.dao.QueryCounter;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.epam.training.gym.persistence.dao.TestEntities.newTrainee;
import static com.epam.training.gym.persistence.dao.TestEntities.newTrainer;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
public class TrainingDaoImplTest {

    private static final Long FITNESS_ID = 1L;
    private static final Long YOGA_ID = 2L;

    private static final TraineeTrainingCriteria NO_TRAINEE_FILTERS = TraineeTrainingCriteria.builder().build();
    private static final TrainerTrainingCriteria NO_TRAINER_FILTERS = TrainerTrainingCriteria.builder().build();

    @Autowired
    private TrainingDao trainingDao;

    @PersistenceContext
    private EntityManager entityManager;

    private TrainingTypeEntity fitness;
    private TrainingTypeEntity yoga;

    /**
     * trainingName | trainee + trainer | trainingType | trainingDate
     * Yoga Jan    John + Anna  YOGA     2026-01-10
     * Fitness Feb John + Mark  FITNESS  2026-02-11
     * Yoga Mar    John + Anna  YOGA     2026-03-12
     * Mary yoga   Mary + Anna  YOGA     2026-04-13
     */
    @BeforeEach
    void createTrainings() {
        fitness = entityManager.find(TrainingTypeEntity.class, FITNESS_ID);
        yoga = entityManager.find(TrainingTypeEntity.class, YOGA_ID);

        TraineeEntity john = persist(newTrainee("John", "Smith", "John.Smith"));
        TraineeEntity mary = persist(newTrainee("Mary", "Jones", "Mary.Jones"));
        TrainerEntity anna = persist(newTrainer("Anna", "Lee", "Anna.Lee", yoga));
        TrainerEntity mark = persist(newTrainer("Mark", "Hill", "Mark.Hill", fitness));

        persist(training("Yoga Jan", john, anna, yoga, LocalDate.of(2026, 1, 10)));
        persist(training("Fitness Feb", john, mark, fitness, LocalDate.of(2026, 2, 11)));
        persist(training("Yoga Mar", john, anna, yoga, LocalDate.of(2026, 3, 12)));
        persist(training("Mary yoga", mary, anna, yoga, LocalDate.of(2026, 4, 13)));

        entityManager.flush();
        entityManager.clear();
    }

    // ************************************ save ***********

    @Test
    void save_storesTrainingWithAllLinks() {
        TraineeEntity trainee = persist(newTrainee("Tom", "Brown", "Tom.Brown"));
        TrainerEntity trainer = persist(newTrainer("Kate", "Moss", "Kate.Moss", yoga));

        TrainingEntity saved = trainingDao.save(
                training("Morning yoga", trainee, trainer, yoga, LocalDate.of(2026, 2, 1)));
        entityManager.flush();
        entityManager.clear();

        assertNotNull(saved.getId());
        TrainingEntity found = entityManager.find(TrainingEntity.class, saved.getId());
        assertEquals("Tom.Brown", found.getTrainee().getUser().getUsername());
        assertEquals("Kate.Moss", found.getTrainer().getUser().getUsername());
        assertEquals(TrainingTypeName.YOGA, found.getTrainingType().getTrainingTypeName());
        assertEquals(LocalDate.of(2026, 2, 1), found.getTrainingDate());
        assertEquals(60, found.getTrainingDuration());
    }

    // ---------- Trainee trainings ----------

    @Test
    void findTraineeTrainings_noFilters_returnsOnlyThisTraineesTrainingsByDate() {
        List<TrainingEntity> found = trainingDao.findTraineeTrainings("John.Smith", NO_TRAINEE_FILTERS);

        assertEquals(List.of("Yoga Jan", "Fitness Feb", "Yoga Mar"), names(found));
    }

    @Test
    void findTraineeTrainings_fromDate_isInclusive() {
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder()
                .fromDate(LocalDate.of(2026, 2, 11))
                .build();

        assertEquals(List.of("Fitness Feb", "Yoga Mar"),
                names(trainingDao.findTraineeTrainings("John.Smith", criteria)));
    }

    @Test
    void findTraineeTrainings_toDate_isInclusive() {
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder()
                .toDate(LocalDate.of(2026, 2, 11))
                .build();

        assertEquals(List.of("Yoga Jan", "Fitness Feb"),
                names(trainingDao.findTraineeTrainings("John.Smith", criteria)));
    }

    @Test
    void findTraineeTrainings_trainerName_ignoresCase() {
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder()
                .trainerName("ANNA")
                .build();

        assertEquals(List.of("Yoga Jan", "Yoga Mar"),
                names(trainingDao.findTraineeTrainings("John.Smith", criteria)));
    }

    @Test
    void findTraineeTrainings_trainingType_filtersByType() {
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder()
                .trainingType(TrainingTypeName.FITNESS)
                .build();

        assertEquals(List.of("Fitness Feb"),
                names(trainingDao.findTraineeTrainings("John.Smith", criteria)));
    }

    @Test
    void findTraineeTrainings_allFilters_combinesThemWithAnd() {
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder()
                .fromDate(LocalDate.of(2026, 2, 11))
                .toDate(LocalDate.of(2026, 12, 31))
                .trainerName("Anna")
                .trainingType(TrainingTypeName.YOGA)
                .build();

        assertEquals(List.of("Yoga Mar"),
                names(trainingDao.findTraineeTrainings("John.Smith", criteria)));
    }

    @Test
    void findTraineeTrainings_unknownTrainee_returnsEmptyList() {
        assertTrue(trainingDao.findTraineeTrainings("No.Body", NO_TRAINEE_FILTERS).isEmpty());
    }

    // ---------- Trainer trainings ----------

    @Test
    void findTrainerTrainings_noFilters_returnsOnlyThisTrainersTrainingsByDate() {
        List<TrainingEntity> found = trainingDao.findTrainerTrainings("Anna.Lee", NO_TRAINER_FILTERS);

        assertEquals(List.of("Yoga Jan", "Yoga Mar", "Mary yoga"), names(found));
    }

    @Test
    void findTrainerTrainings_traineeName_ignoresCase() {
        TrainerTrainingCriteria criteria = TrainerTrainingCriteria.builder()
                .traineeName("MARY")
                .build();

        assertEquals(List.of("Mary yoga"),
                names(trainingDao.findTrainerTrainings("Anna.Lee", criteria)));
    }

    @Test
    void findTrainerTrainings_dateRange_returnsTrainingsInsideIt() {
        TrainerTrainingCriteria criteria = TrainerTrainingCriteria.builder()
                .fromDate(LocalDate.of(2026, 1, 11))
                .toDate(LocalDate.of(2026, 4, 12))
                .build();

        assertEquals(List.of("Yoga Mar"),
                names(trainingDao.findTrainerTrainings("Anna.Lee", criteria)));
    }

    // ****************  Helpers ****************

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    private static TrainingEntity training(String name, TraineeEntity trainee, TrainerEntity trainer,
                                     TrainingTypeEntity type, LocalDate date) {
        return TrainingEntity.builder()
                .trainingName(name)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(type)
                .trainingDate(date)
                .trainingDuration(60)
                .build();
    }

    /** Names of the trainings in the same order. */
    private static List<String> names(List<TrainingEntity> trainings) {
        return trainings.stream()
                .map(TrainingEntity::getTrainingName)
                .toList();
    }

    @Test
    void findTraineeTrainings_loadsTrainingsWithAllLinksInOneQuery() {
        QueryCounter queries = new QueryCounter(entityManager);

        trainingDao.findTraineeTrainings("John.Smith", NO_TRAINEE_FILTERS)
                .forEach(TrainingDaoImplTest::touchAllLinks);

        assertEquals(1, queries.count());
    }

    @Test
    void findTrainerTrainings_loadsTrainingsWithAllLinksInOneQuery() {
        QueryCounter queries = new QueryCounter(entityManager);

        trainingDao.findTrainerTrainings("Anna.Lee", NO_TRAINER_FILTERS)
                .forEach(TrainingDaoImplTest::touchAllLinks);

        assertEquals(1, queries.count());
    }

    /** Reads every linked object, so anything not loaded yet is loaded now and counted. */
    private static void touchAllLinks(TrainingEntity training) {
        training.getTrainee().getUser().getUsername();
        training.getTrainer().getUser().getUsername();
        training.getTrainer().getSpecialization().getTrainingTypeName();
        training.getTrainingType().getTrainingTypeName();
    }

}
