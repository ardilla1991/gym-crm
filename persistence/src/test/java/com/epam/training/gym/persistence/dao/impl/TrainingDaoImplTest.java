package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.domain.*;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.TrainingDao;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.epam.training.gym.persistence.dao.TestEntities.newTrainee;
import static com.epam.training.gym.persistence.dao.TestEntities.newTrainer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
public class TrainingDaoImplTest {

    private static final Long YOGA_ID = 2L;

    @Autowired
    private TrainingDao trainingDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void save_storesTrainingWithAllLinks() {
        TrainingType yoga = entityManager.find(TrainingType.class, YOGA_ID);
        Trainee trainee = newTrainee("John.Smith");
        Trainer trainer = newTrainer("Anna.Lee", yoga);
        entityManager.persist(trainee);
        entityManager.persist(trainer);

        Training saved = trainingDao.save(Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName("Morning yoga")
                .trainingType(yoga)
                .trainingDate(LocalDate.of(2026, 1, 10))
                .trainingDuration(60)
                .build());
        entityManager.flush();
        entityManager.clear();

        assertNotNull(saved.getId());
        Training found = entityManager.find(Training.class, saved.getId());
        assertEquals("John.Smith", found.getTrainee().getUser().getUsername());
        assertEquals("Anna.Lee", found.getTrainer().getUser().getUsername());
        assertEquals(TrainingTypeName.YOGA, found.getTrainingType().getTrainingTypeName());
        assertEquals(LocalDate.of(2026, 1, 10), found.getTrainingDate());
        assertEquals(60, found.getTrainingDuration());
    }

}
