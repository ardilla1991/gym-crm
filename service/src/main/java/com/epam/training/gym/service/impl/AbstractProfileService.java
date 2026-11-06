package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.User;
import com.epam.training.gym.domain.UserProfile;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.epam.training.gym.service.exception.AuthenticationException;

import java.util.Optional;

/**
 * Account operations shared by trainee and trainer services (Template Method):
 * subclasses only say where to find a profile by username.
 */
public abstract class AbstractProfileService <T extends UserProfile> implements AccountService {

    /** Named after the real class: TraineeServiceImpl or TrainerServiceImpl. */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final String profileName;

    protected AbstractProfileService(String profileName) {
        this.profileName = profileName;
    }

    /** Trainee or trainer with this username: the only step that differs. */
    protected abstract Optional<T> findByUsername(String username);

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
        authenticate(credentials).getUser().setPassword(newPassword);
        log.info("{} password changed: username={}", profileName, credentials.username());
    }

    @Override
    @Transactional
    public boolean toggleActive(Credentials credentials) {
        User user = authenticate(credentials).getUser();
        user.setActive(!user.isActive());
        log.info("{} {} is now {}", profileName, user.getUsername(), user.isActive() ? "active" : "inactive");
        return user.isActive();
    }

    /** Loads the profile if the credentials are right; every operation except create starts with it. */
    protected T authenticate(Credentials credentials) {
        return findWithCredentials(credentials).orElseThrow(() -> {
            log.warn("{} authentication failed: username={}", profileName, credentials.username());
            return new AuthenticationException();
        });
    }

    private Optional<T> findWithCredentials(Credentials credentials) {
        return findByUsername(credentials.username())
                .filter(profile -> profile.getUser().getPassword().equals(credentials.password()));
    }
}
