package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.User;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void changePassword_rightCredentials_setsNewPassword() {
        User user = User.builder().username(USERNAME).password(CURRENT_PASSWORD).build();
        when(userAuthenticator.authenticate(CREDENTIALS)).thenReturn(user);

        userService.changePassword(CREDENTIALS, NEW_PASSWORD);

        assertEquals(NEW_PASSWORD, user.getPassword());
    }

    @Test
    void changePassword_wrongCredentials_throws() {
        when(userAuthenticator.authenticate(CREDENTIALS)).thenThrow(new AuthenticationException());

        assertThrows(AuthenticationException.class, () -> userService.changePassword(CREDENTIALS, NEW_PASSWORD));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void changePassword_blankNewPassword_throwsWithoutAuthentication(String newPassword) {
        assertThrows(IllegalArgumentException.class, () -> userService.changePassword(CREDENTIALS, newPassword));

        verify(userAuthenticator, never()).authenticate(any());
    }

}
