package com.epam.training.gym.persistence.dao.impl;

import com.epam.training.gym.persistence.dao.TestJpaConfig;
import com.epam.training.gym.persistence.dao.UserDao;
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

}
