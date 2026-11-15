package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.User;
import com.epam.training.gym.model.UserProfile;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.service.AccountService;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Account operations shared by trainee and trainer services (Template Method).
 * Subclasses say where to find a profile by username.
 */
public abstract class AbstractProfileService<M extends UserProfile> implements AccountService<M> {

    /** Named after the real class: TraineeServiceImpl or TrainerServiceImpl. */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final String profileName;

    private UserAuthenticator userAuthenticator;
    private UserDao userDao;

    protected AbstractProfileService(String profileName) {
        this.profileName = profileName;
    }

    @Autowired
    public void setUserAuthenticator(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /** Trainee or trainer with this username: the only lookup that differs. */
    protected abstract Optional<M> findByUsername(String username);

    /** Profile with its linked list loaded: trainee's trainers or trainer's trainees. */
    protected abstract Optional<M> findProfileByUsername(String username);

    @Override
    @Transactional(readOnly = true)
    public M getProfile(Credentials credentials, String username) {
        userAuthenticator.authenticate(credentials);
        M profile = findProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(profileName, username));
        log.info("{} profile read: username={}, by={}", profileName, username, credentials.username());
        return profile;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean matchCredentials(Credentials credentials) {
        boolean matches = findWithCredentials(credentials).isPresent();
        log.info("{} credentials check: username={}, matches={}", profileName, credentials.username(), matches);
        return matches;
    }

    @Override
    @Transactional
    public void changeActiveStatus(Credentials credentials, boolean active) {
        User user = authenticate(credentials).getUser();
        if (user.isActive() == active) {
            throw new InvalidStateException(profileName + " is already " + statusName(active));
        }
        user.setActive(active);
        userDao.updateActive(user);
        log.info("{} {} is now {}", profileName, user.getUsername(), statusName(active));
    }

    /** The caller's own profile, if the credentials are right; every operation except create starts with it. */
    @Override
    @Transactional(readOnly = true)
    public M authenticate(Credentials credentials) {
        return findWithCredentials(credentials).orElseThrow(() -> {
            log.warn("{} authentication failed: username={}", profileName, credentials.username());
            return new AuthenticationException();
        });
    }

    private static String statusName(boolean active) {
        return active ? "active" : "inactive";
    }

    private Optional<M> findWithCredentials(Credentials credentials) {
        return userAuthenticator.matches(credentials)
                ? findByUsername(credentials.username())
                : Optional.empty();
    }

    /** Like authenticate, but loads the profile with its linked list: for operations that return the profile. */
    protected M authenticateWithDetails(Credentials credentials) {
        authenticate(credentials);
        return findProfileByUsername(credentials.username())
                .orElseThrow(() -> new EntityNotFoundException(profileName, credentials.username()));
    }
}
