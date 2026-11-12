package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.api.rest.exception.ApiError;
import com.epam.training.gym.api.rest.exception.GlobalExceptionHandler;
import com.epam.training.gym.domain.auth.Credentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private static final Credentials EMILY = new Credentials("Emily.Johnson", "secret");
    private static final String EMILY_HEADER = TestAuth.basic("Emily.Johnson", "secret");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Mock
    private GymFacade facade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(facade))
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
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
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        when(facade.matchTraineeCredentials(EMILY)).thenReturn(false);
        when(facade.matchTrainerCredentials(EMILY)).thenReturn(false);

        MockHttpServletResponse response = mockMvc.perform(get(TestPaths.LOGIN)
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse();

        assertEquals("Basic realm=\"Gym CRM\"", response.getHeader(HttpHeaders.WWW_AUTHENTICATE));
        assertEquals(new ApiError(401, "Unauthorized", "Invalid username or password"),
                JSON.readValue(response.getContentAsString(), ApiError.class));
    }

    @Test
    void login_noAuthorizationHeader_doesNotCallFacade() throws Exception {
        mockMvc.perform(get(TestPaths.LOGIN))
                .andExpect(status().isUnauthorized());

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
    void changePassword_noAuthorizationHeader_doesNotCallFacade() throws Exception {
        mockMvc.perform(put(TestPaths.CHANGE_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"newPassword": "newSecret1"}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(facade);
    }

    @Test
    void changePassword_blankNewPassword_returnsBadRequest() throws Exception {
        mockMvc.perform(put(TestPaths.CHANGE_PASSWORD)
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\": \" \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

}
