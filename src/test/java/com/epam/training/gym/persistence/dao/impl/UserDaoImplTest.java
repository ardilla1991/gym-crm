package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.model.User;
import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.persistence.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.epam.training.gym.persistence.dao.TestEntities.newUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(TestJpaConfig.class)
@Transactional
public class UserDaoImplTest {
    @Autowired
    private UserDao userDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void findUsernamesStartingWith_returnsAllUsernamesWithThisPrefix() {
        entityManager.persist(newUser("John", "Smith", "John.Smith"));
        entityManager.persist(newUser("John", "Smith", "John.Smith1"));
        entityManager.persist(newUser("John", "Smithson", "John.Smithson"));
        entityManager.persist(newUser("Anna", "Lee", "Anna.Lee"));

        List<String> usernames = userDao.findUsernamesStartingWith("John.Smith");

        // LIKE 'John.Smith%' also matches John.Smithson: the username generator must ignore it
        assertEquals(Set.of("John.Smith", "John.Smith1", "John.Smithson"), Set.copyOf(usernames));
    }

    @Test
    void findUsernamesStartingWith_noMatches_returnsEmptyList() {
        entityManager.persist(newUser("Anna", "Lee", "Anna.Lee"));

        assertTrue(userDao.findUsernamesStartingWith("John.Smith").isEmpty());
    }

    @Test
    void matchesPassword_rightPassword_true() {
        entityManager.persist(newUser("John", "Smith", "John.Smith"));

        assertTrue(userDao.matchesPassword("John.Smith", "abcde12345"));
    }

    @Test
    void matchesPassword_wrongPassword_false() {
        entityManager.persist(newUser("John", "Smith", "John.Smith"));

        assertFalse(userDao.matchesPassword("John.Smith", "wrong"));
    }

    @Test
    void matchesPassword_unknownUser_false() {
        assertFalse(userDao.matchesPassword("No.Body", "abcde12345"));
    }

    @Test
    void findByUsername_existing_returnsUserWithoutPassword() {
        entityManager.persist(newUser("John", "Smith", "John.Smith"));
        flushAndClear();

        User found = userDao.findByUsername("John.Smith").orElseThrow();

        assertEquals("John", found.getFirstName());
        assertNull(found.getPassword());
    }

    @Test
    void findByUsername_unknown_returnsEmpty() {
        assertTrue(userDao.findByUsername("No.Body").isEmpty());
    }

    @Test
    void updatePassword_savesNewPassword() {
        UserEntity user = newUser("John", "Smith", "John.Smith");
        entityManager.persist(user);
        flushAndClear();

        userDao.updatePassword(User.builder().id(user.getId()).build(), "newPass123");
        flushAndClear();

        assertEquals("newPass123", entityManager.find(UserEntity.class, user.getId()).getPassword());
    }

    @Test
    void updateActive_savesStatus() {
        UserEntity user = newUser("John", "Smith", "John.Smith");
        entityManager.persist(user);
        flushAndClear();

        userDao.updateActive(User.builder().id(user.getId()).isActive(false).build());
        flushAndClear();

        assertFalse(entityManager.find(UserEntity.class, user.getId()).isActive());
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
