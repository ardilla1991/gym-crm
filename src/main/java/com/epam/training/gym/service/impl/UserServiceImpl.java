package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.entity.UserEntity;
import com.epam.training.gym.service.UserService;
import com.epam.training.gym.service.auth.UserAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserAuthenticator userAuthenticator;

    @Autowired
    public void setUserAuthenticator(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    @Override
    @Transactional
    public void changePassword(Credentials credentials, String newPassword) {
        UserEntity user = userAuthenticator.authenticate(credentials);
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password must not be blank");
        }
        // A managed entity: Hibernate writes the change at commit (dirty checking)
        user.setPassword(newPassword);
        log.info("Password changed: username={}", credentials.username());
    }

}
