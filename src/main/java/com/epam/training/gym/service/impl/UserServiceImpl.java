package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.User;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.auth.PasswordPolicy;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.service.UserService;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserAuthenticator userAuthenticator;
    private UserDao userDao;

    @Autowired
    public void setUserAuthenticator(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public void changePassword(Credentials credentials, String newPassword) {
        userAuthenticator.authenticate(credentials);
        if (newPassword == null || newPassword.isBlank()) {
            throw new InvalidInputException("New password must not be blank");
        }
        if (newPassword.length() != PasswordPolicy.LENGTH) {
            throw new InvalidInputException(
                    "New password must contain exactly " + PasswordPolicy.LENGTH + " characters");
        }
        // The user exists: the credentials were just checked
        User user = userDao.findByUsername(credentials.username()).orElseThrow();
        userDao.updatePassword(user, newPassword);
        log.info("Password changed: username={}", credentials.username());
    }

}
