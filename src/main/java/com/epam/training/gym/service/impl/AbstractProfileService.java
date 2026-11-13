package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.UserProfile;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.entity.ProfileEntity;
import com.epam.training.gym.persistence.entity.UserEntity;
import com.epam.training.gym.service.AccountService;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Account operations shared by trainee and trainer services (Template Method).
 * Inside, the services work with entities (E); outside, they return models (M).
 * Subclasses say where to find a profile by username and how to convert it to a model.
 */
public abstract class AbstractProfileService<M extends UserProfile, E extends ProfileEntity>
        implements AccountService<M> {

    /** Named after the real class: TraineeServiceImpl or TrainerServiceImpl. */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final String profileName;

    private UserAuthenticator userAuthenticator;

    protected AbstractProfileService(String profileName) {
        this.profileName = profileName;
    }

    @Autowired
    public void setUserAuthenticator(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
    }

    /** Trainee or trainer row with this username: the only lookup that differs. */
    protected abstract Optional<E> findByUsername(String username);

    /** Profile row with its linked list loaded: trainee's trainers or trainer's trainees. */
    protected abstract Optional<E> findProfileByUsername(String username);

    /** Entity to model, without the linked list. */
    protected abstract M toModel(E entity);

    /** Entity to model with its linked list: for an entity loaded by findProfileByUsername. */
    protected abstract M toProfileModel(E entity);

    @Override
    @Transactional(readOnly = true)
    public M getProfile(Credentials credentials, String username) {
        userAuthenticator.authenticate(credentials);
        E profile = findProfileByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(profileName, username));
        log.info("{} profile read: username={}, by={}", profileName, username, credentials.username());
        return toProfileModel(profile);
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
    public void changePassword(Credentials credentials, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password must not be blank");
        }
        // A managed entity: Hibernate writes the change at commit (dirty checking)
        authenticateEntity(credentials).getUser().setPassword(newPassword);
        log.info("{} password changed: username={}", profileName, credentials.username());
    }

    @Override
    @Transactional
    public boolean toggleActive(Credentials credentials) {
        UserEntity user = authenticateEntity(credentials).getUser();
        user.setActive(!user.isActive());
        log.info("{} {} is now {}", profileName, user.getUsername(), user.isActive() ? "active" : "inactive");
        return user.isActive();
    }

    /** The caller's own profile as a model, if the credentials are right. */
    @Override
    @Transactional(readOnly = true)
    public M authenticate(Credentials credentials) {
        return toModel(authenticateEntity(credentials));
    }

    /**
     * The caller's own profile row, if the credentials are right; every operation except create starts with it.
     * Called inside the services' transactions, so the entity is managed and its changes are saved at commit.
     */
    protected E authenticateEntity(Credentials credentials) {
        return findWithCredentials(credentials).orElseThrow(() -> {
            log.warn("{} authentication failed: username={}", profileName, credentials.username());
            return new AuthenticationException();
        });
    }

    private Optional<E> findWithCredentials(Credentials credentials) {
        return findByUsername(credentials.username())
                .filter(profile -> profile.getUser().getPassword().equals(credentials.password()));
    }
}
