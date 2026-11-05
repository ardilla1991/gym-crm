package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.*;
import com.epam.training.gym.domain.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.domain.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.TrainingDao;
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

    private TrainingType fitness;
    private TrainingType yoga;

    /**
     * trainingName | trainee + trainer | trainingType | trainingDate
     * Yoga Jan    John + Anna  YOGA     2026-01-10
     * Fitness Feb John + Mark  FITNESS  2026-02-11
     * Yoga Mar    John + Anna  YOGA     2026-03-12
     * Mary yoga   Mary + Anna  YOGA     2026-04-13
     */
    @BeforeEach
    void createTrainings() {
        fitness = entityManager.find(TrainingType.class, FITNESS_ID);
        yoga = entityManager.find(TrainingType.class, YOGA_ID);

        Trainee john = persist(newTrainee("John", "Smith", "John.Smith"));
        Trainee mary = persist(newTrainee("Mary", "Jones", "Mary.Jones"));
        Trainer anna = persist(newTrainer("Anna", "Lee", "Anna.Lee", yoga));
        Trainer mark = persist(newTrainer("Mark", "Hill", "Mark.Hill", fitness));

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
        Trainee trainee = persist(newTrainee("Tom", "Brown", "Tom.Brown"));
        Trainer trainer = persist(newTrainer("Kate", "Moss", "Kate.Moss", yoga));

        Training saved = trainingDao.save(
                training("Morning yoga", trainee, trainer, yoga, LocalDate.of(2026, 2, 1)));
        entityManager.flush();
        entityManager.clear();

        assertNotNull(saved.getId());
        Training found = entityManager.find(Training.class, saved.getId());
        assertEquals("Tom.Brown", found.getTrainee().getUser().getUsername());
        assertEquals("Kate.Moss", found.getTrainer().getUser().getUsername());
        assertEquals(TrainingTypeName.YOGA, found.getTrainingType().getTrainingTypeName());
        assertEquals(LocalDate.of(2026, 2, 1), found.getTrainingDate());
        assertEquals(60, found.getTrainingDuration());
    }

    // ---------- Trainee trainings ----------

    @Test
    void findTraineeTrainings_noFilters_returnsOnlyThisTraineesTrainingsByDate() {
        List<Training> found = trainingDao.findTraineeTrainings("John.Smith", NO_TRAINEE_FILTERS);

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
        List<Training> found = trainingDao.findTrainerTrainings("Anna.Lee", NO_TRAINER_FILTERS);

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

    private static Training training(String name, Trainee trainee, Trainer trainer,
                                     TrainingType type, LocalDate date) {
        return Training.builder()
                .trainingName(name)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(type)
                .trainingDate(date)
                .trainingDuration(60)
                .build();
    }

    /** Names of the trainings in the same order. */
    private static List<String> names(List<Training> trainings) {
        return trainings.stream()
                .map(Training::getTrainingName)
                .toList();
    }

}
