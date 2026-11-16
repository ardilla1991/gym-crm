package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.User;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

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
    public boolean matchesPassword(String username, String password) {
        return !entityManager
                .createQuery("SELECT u.id FROM User u WHERE u.username = :username AND u.password = :password",
                        Long.class)
                .setParameter("username", username)
                .setParameter("password", password)
                .getResultList()
                .isEmpty();
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        entityManager.find(UserEntity.class, user.getId()).setPassword(newPassword);
        log.debug("User password updated: id={}", user.getId());
    }

    @Override
    public void updateActive(User user) {
        entityManager.find(UserEntity.class, user.getId()).setActive(user.isActive());
        log.debug("User active status updated: id={}, active={}", user.getId(), user.isActive());
    }
}
