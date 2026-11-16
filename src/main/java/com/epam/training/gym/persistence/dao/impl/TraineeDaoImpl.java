package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.Trainee;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.entity.TraineeEntity;
import com.epam.training.gym.persistence.entity.TrainerEntity;
import com.epam.training.gym.persistence.mapper.TraineeEntityMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Trainee save(Trainee trainee) {
        TraineeEntity entity = TraineeEntityMapper.toNewEntity(trainee);
        entityManager.persist(entity);       // the trainee's user is saved too: cascade = ALL
        log.debug("Trainee saved: id={}", entity.getId());

        return TraineeEntityMapper.toModel(entity);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        return entityManager
                .createQuery("SELECT t FROM Trainee t WHERE t.user.username = :username", TraineeEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .map(TraineeEntityMapper::toModel);
    }

    @Override
    public Optional<Trainee> findByUsernameWithTrainers(String username) {
        return entityManager.createQuery("""
                    SELECT t FROM Trainee t
                    JOIN FETCH t.user
                    LEFT JOIN FETCH t.trainers tr
                    LEFT JOIN FETCH tr.user
                    LEFT JOIN FETCH tr.specialization
                    WHERE t.user.username = :username
                    """, TraineeEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst()
                .map(TraineeEntityMapper::toModelWithTrainers);
    }

    @Override
    public Trainee update(Trainee trainee) {
        TraineeEntity entity = entityManager.find(TraineeEntity.class, trainee.getId());
        entity.getUser().setFirstName(trainee.getUser().getFirstName());
        entity.getUser().setLastName(trainee.getUser().getLastName());
        entity.setDateOfBirth(trainee.getDateOfBirth());
        entity.setAddress(trainee.getAddress());
        log.debug("Trainee updated: id={}", entity.getId());

        return TraineeEntityMapper.toModel(entity);
    }

    @Override
    public void delete(Trainee trainee) {
        entityManager.remove(entityManager.find(TraineeEntity.class, trainee.getId()));
        log.debug("Trainee deleted: id={}", trainee.getId());
    }

    @Override
    public void replaceTrainers(Trainee trainee, Collection<Trainer> trainers) {
        Set<TrainerEntity> assigned = entityManager.find(TraineeEntity.class, trainee.getId()).getTrainers();
        assigned.clear();
        trainers.forEach(trainer -> assigned.add(entityManager.getReference(TrainerEntity.class, trainer.getId())));
        log.debug("Trainee trainers replaced: id={}, count={}", trainee.getId(), trainers.size());
    }

    @Override
    public void addTrainer(Trainee trainee, Trainer trainer) {
        entityManager.find(TraineeEntity.class, trainee.getId()).getTrainers()
                .add(entityManager.getReference(TrainerEntity.class, trainer.getId()));
        log.debug("Trainer added to trainee: traineeId={}, trainerId={}", trainee.getId(), trainer.getId());
    }
}
