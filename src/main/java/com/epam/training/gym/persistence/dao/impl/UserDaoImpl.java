package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<String> findUsernamesStartingWith(String prefix) {
        return entityManager
                .createQuery("SELECT u.username FROM User u WHERE u.username LIKE :prefix", String.class)
                .setParameter("prefix", prefix + "%")
                .getResultList();
    }

    @Override
    public Optional<UserEntity> findByUsername(String username) {
        return entityManager
                .createQuery("SELECT u FROM User u WHERE u.username = :username", UserEntity.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }
}