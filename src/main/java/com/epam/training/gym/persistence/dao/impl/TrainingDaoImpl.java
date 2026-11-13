package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TrainingDaoImpl implements TrainingDao {

    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Override
    public TrainingEntity save(TrainingEntity training) {
        entityManager.persist(training);
        log.debug("Training saved: id={}", training.getId());

        return training;
    }

    @Override
    public List<TrainingEntity> findTraineeTrainings(String traineeUsername, TraineeTrainingCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> training = query.from(TrainingEntity.class);
        fetchLinks(training);

        List<Predicate> conditions = new ArrayList<>();
        conditions.add(cb.equal(training.get("trainee").get("user").get("username"), traineeUsername));
        addDateConditions(cb, training, conditions, criteria.fromDate(), criteria.toDate());
        if (criteria.trainerName() != null) {
            conditions.add(cb.equal(
                    cb.lower(training.get("trainer").get("user").<String>get("firstName")),
                    criteria.trainerName().toLowerCase()));
        }
        if (criteria.trainingType() != null) {
            conditions.add(cb.equal(
                    training.get("trainingType").get("trainingTypeName"),
                    criteria.trainingType()));
        }

        query.select(training)
                .where(conditions.toArray(new Predicate[0]))
                .orderBy(cb.asc(training.get("trainingDate")));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<TrainingEntity> findTrainerTrainings(String trainerUsername, TrainerTrainingCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainingEntity> query = cb.createQuery(TrainingEntity.class);
        Root<TrainingEntity> training = query.from(TrainingEntity.class);
        fetchLinks(training);
        List<Predicate> conditions = new ArrayList<>();
        conditions.add(cb.equal(training.get("trainer").get("user").get("username"), trainerUsername));
        addDateConditions(cb, training, conditions, criteria.fromDate(), criteria.toDate());
        if (criteria.traineeName() != null) {
            conditions.add(cb.equal(
                    cb.lower(training.get("trainee").get("user").<String>get("firstName")),
                    criteria.traineeName().toLowerCase()));
        }

        query.select(training)
                .where(conditions.toArray(new Predicate[0]))
                .orderBy(cb.asc(training.get("trainingDate")));

        return entityManager.createQuery(query).getResultList();
    }

    /** Date filters shared by both queries; both dates are inclusive. */
    private static void addDateConditions(CriteriaBuilder cb, Root<TrainingEntity> training,
                                          List<Predicate> conditions, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null) {
            conditions.add(cb.greaterThanOrEqualTo(training.<LocalDate>get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            conditions.add(cb.lessThanOrEqualTo(training.<LocalDate>get("trainingDate"), toDate));
        }
    }

    /**
     * Loads the trainee, the trainer and the training type in the same SQL query as the trainings.
     * Without it Hibernate loads each linked object with a separate query (the N+1 problem).
     */
    private static void fetchLinks(Root<TrainingEntity> training) {
        training.fetch("trainee").fetch("user");

        Fetch<TrainingEntity, TrainerEntity> trainer = training.fetch("trainer");
        trainer.fetch("user");
        trainer.fetch("specialization");

        training.fetch("trainingType");
    }


}