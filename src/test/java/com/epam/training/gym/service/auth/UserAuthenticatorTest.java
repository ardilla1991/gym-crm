package com.epam.training.gym.service.auth;

import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.service.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void matches_asksDatabase() {
        when(userDao.matchesPassword(USERNAME, CURRENT_PASSWORD)).thenReturn(true);

        assertTrue(userAuthenticator.matches(new Credentials(USERNAME, CURRENT_PASSWORD)));
    }

    @Test
    void authenticate_rightPassword_passes() {
        when(userDao.matchesPassword(USERNAME, CURRENT_PASSWORD)).thenReturn(true);

        assertDoesNotThrow(() -> userAuthenticator.authenticate(new Credentials(USERNAME, CURRENT_PASSWORD)));
    }

    @Test
    void authenticate_wrongUsernameOrPassword_throws() {
        when(userDao.matchesPassword(USERNAME, "wrong")).thenReturn(false);

        assertThrows(AuthenticationException.class,
                () -> userAuthenticator.authenticate(new Credentials(USERNAME, "wrong")));
    }
}
