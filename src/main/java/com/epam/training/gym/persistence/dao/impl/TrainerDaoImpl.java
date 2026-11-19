package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.entity.TrainingTypeEntity;
import com.epam.training.gym.persistence.mapper.TrainerEntityMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Trainer save(Trainer trainer) {
        TrainingTypeEntity specialization =
                entityManager.getReference(TrainingTypeEntity.class, trainer.getSpecialization().getId());
        TrainerEntity entity = TrainerEntityMapper.toNewEntity(trainer, specialization);
        entityManager.persist(entity);
        log.debug("Trainer saved: id={}", entity.getId());

        return TrainerEntityMapper.toModel(entity);
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        return entityManager
                .createQuery("""
                        SELECT t FROM Trainer t
                        JOIN FETCH t.user
                        JOIN FETCH t.specialization
                        WHERE t.user.username = :username
                        """, TrainerEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .map(TrainerEntityMapper::toModel);
    }

    @Override
    public List<Trainer> findByUsernames(Collection<String> usernames) {
        if (usernames.isEmpty()) {
            return List.of();   // no need to query the database
        }
        return entityManager
                .createQuery("""
                        SELECT t FROM Trainer t
                        JOIN FETCH t.user
                        JOIN FETCH t.specialization
                        WHERE t.user.username IN :usernames
                        """, TrainerEntity.class)
                .setParameter("usernames", usernames)
                .getResultStream()
                .map(TrainerEntityMapper::toModel)
                .toList();
    }

    @Override
    public List<Trainer> findActiveNotAssignedToTrainee(String traineeUsername) {
        return entityManager.createQuery("""
                    SELECT tr FROM Trainer tr
                    JOIN FETCH tr.user
                    JOIN FETCH tr.specialization
                    WHERE tr.user.isActive = true
                    AND tr NOT IN (
                        SELECT assigned FROM Trainee te JOIN te.trainers assigned
                        WHERE te.user.username = :username)
                    """, TrainerEntity.class)
                .setParameter("username", traineeUsername)
                .getResultStream()
                .map(TrainerEntityMapper::toModel)
                .toList();
    }

    @Override
    public Optional<Trainer> findByUsernameWithTrainees(String username) {
        return entityManager.createQuery("""
                    SELECT t FROM Trainer t
                    JOIN FETCH t.user
                    JOIN FETCH t.specialization
                    LEFT JOIN FETCH t.trainees te
                    LEFT JOIN FETCH te.user
                    WHERE t.user.username = :username
                    """, TrainerEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .map(TrainerEntityMapper::toModelWithTrainees);
    }

    @Override
    public Trainer update(Trainer trainer) {
        TrainerEntity entity = entityManager.find(TrainerEntity.class, trainer.getId());
        entity.getUser().setFirstName(trainer.getUser().getFirstName());
        entity.getUser().setLastName(trainer.getUser().getLastName());
        entity.getUser().setActive(trainer.getUser().isActive());
        log.debug("Trainer updated: id={}", entity.getId());

        return TrainerEntityMapper.toModel(entity);
    }
}
