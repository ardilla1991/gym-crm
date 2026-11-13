package com.epam.training.gym.service.auth;

import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.persistence.entity.UserEntity;
import com.epam.training.gym.service.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserAuthenticatorTest {
    private static final String USERNAME = "John.Smith";
    private static final String CURRENT_PASSWORD = "abcdef1234";

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserAuthenticator userAuthenticator;

    @Test
    void authenticate_rightPassword_passes() {
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser()));

        assertDoesNotThrow(() -> userAuthenticator.authenticate(new Credentials(USERNAME, CURRENT_PASSWORD)));
    }

    @Test
    void authenticate_wrongPassword_throws() {
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser()));

        assertThrows(AuthenticationException.class,
                () -> userAuthenticator.authenticate(new Credentials(USERNAME, "wrong")));
    }

    @Test
    void authenticate_unknownUser_throws() {
        when(userDao.findByUsername("User.Name")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> userAuthenticator.authenticate(new Credentials("User.Name", CURRENT_PASSWORD)));
    }

    private static UserEntity existingUser() {
        return UserEntity.builder().username(USERNAME).password(CURRENT_PASSWORD).build();
    }
}
