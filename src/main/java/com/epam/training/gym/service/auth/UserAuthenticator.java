package com.epam.training.gym.service.auth;

import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.service.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Checks that the caller is an existing user with the right password, trainee or trainer.
 * Used where any signed-in user may act: reading a profile by username.
 */
@Component
public class UserAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(UserAuthenticator.class);

    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /** @throws AuthenticationException if the username or the password is wrong */
    public void authenticate(Credentials credentials) {
        boolean valid = userDao.findByUsername(credentials.username())
                .filter(user -> user.getPassword().equals(credentials.password()))
                .isPresent();
        if (!valid) {
            log.warn("User authentication failed: username={}", credentials.username());
            throw new AuthenticationException();
        }
    }
}
