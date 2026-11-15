package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.User;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.InvalidInputException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final String USERNAME = "John.Smith";
    private static final String CURRENT_PASSWORD = "abcdef1234";
    private static final String NEW_PASSWORD = "newPass123";
    private static final Credentials CREDENTIALS = new Credentials(USERNAME, CURRENT_PASSWORD);

    @Mock
    private UserAuthenticator userAuthenticator;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void changePassword_rightCredentials_savesNewPassword() {
        User user = User.builder().id(1L).username(USERNAME).build();
        when(userDao.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        userService.changePassword(CREDENTIALS, NEW_PASSWORD);

        verify(userAuthenticator).authenticate(CREDENTIALS);
        verify(userDao).updatePassword(user, NEW_PASSWORD);
    }

    @Test
    void changePassword_wrongCredentials_throws() {
        doThrow(new AuthenticationException()).when(userAuthenticator).authenticate(CREDENTIALS);

        assertThrows(AuthenticationException.class, () -> userService.changePassword(CREDENTIALS, NEW_PASSWORD));

        verify(userDao, never()).updatePassword(any(), any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "          ", "336", "123456789", "12345678901"})
    void changePassword_invalidNewPassword_throwsAndKeepsOldPassword(String newPassword) {
        assertThrows(InvalidInputException.class, () -> userService.changePassword(CREDENTIALS, newPassword));

        verify(userDao, never()).updatePassword(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "336", "12345678901"})
    void changePassword_wrongPassword_isRejectedBeforeCheckingNewPassword(String newPassword) {
        doThrow(new AuthenticationException()).when(userAuthenticator).authenticate(CREDENTIALS);

        assertThrows(AuthenticationException.class, () -> userService.changePassword(CREDENTIALS, newPassword));
    }

}
