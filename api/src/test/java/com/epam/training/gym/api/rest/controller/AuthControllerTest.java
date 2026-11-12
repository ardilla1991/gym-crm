package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.service.exception.AuthenticationException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private static final Credentials EMILY = new Credentials("Emily.Johnson", "secret");
    private static final String EMILY_HEADER = TestAuth.basic("Emily.Johnson", "secret");

    @Mock
    private GymFacade facade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(facade))
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .build();
    }

    @Test
    void login_trainee_returnsOk() throws Exception {
        when(facade.matchTraineeCredentials(EMILY)).thenReturn(true);

        mockMvc.perform(get(TestPaths.LOGIN).header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isOk());
    }

    @Test
    void login_trainer_returnsOk() throws Exception {
        when(facade.matchTraineeCredentials(EMILY)).thenReturn(false);
        when(facade.matchTrainerCredentials(EMILY)).thenReturn(true);

        mockMvc.perform(get(TestPaths.LOGIN).header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isOk());
    }

    @Test
    void login_wrongPassword_throwsAuthenticationException() {
        when(facade.matchTraineeCredentials(EMILY)).thenReturn(false);
        when(facade.matchTrainerCredentials(EMILY)).thenReturn(false);

        // Until REST-7 there is no error handler: the exception comes out of MockMvc wrapped in ServletException
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(get(TestPaths.LOGIN).header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)));
        assertInstanceOf(AuthenticationException.class, e.getCause());
    }

    @Test
    void login_noAuthorizationHeader_doesNotCallFacade() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(get(TestPaths.LOGIN)));
        assertInstanceOf(AuthenticationException.class, e.getCause());
        verifyNoInteractions(facade);
    }

    @Test
    void changePassword_passesOldCredentialsAndNewPassword() throws Exception {
        mockMvc.perform(put(TestPaths.CHANGE_PASSWORD)
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword": "newSecret1"}
                                """))
                .andExpect(status().isOk());

        verify(facade).changePassword(EMILY, "newSecret1");
    }

    @Test
    void changePassword_noAuthorizationHeader_doesNotCallFacade() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(put(TestPaths.CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword": "newSecret1"}
                                """)));
        assertInstanceOf(AuthenticationException.class, e.getCause());
        verifyNoInteractions(facade);
    }

}
