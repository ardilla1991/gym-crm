package com.epam.training.gym.api.rest.auth;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.ApiPaths;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.controller.TraineeController;
import com.epam.training.gym.api.rest.exception.GlobalExceptionHandler;
import com.epam.training.gym.metrics.GymMetrics;
import com.epam.training.gym.model.auth.Credentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthenticationInterceptorTest {

    private static final String EMILY_WRONG_PASSWORD = TestAuth.basic("Emily.Johnson", "wrong");
    private static final String EMILY_RIGHT_PASSWORD = TestAuth.basic("Emily.Johnson", "secret");

    @Mock
    private GymFacade facade;

    @Mock
    private GymMetrics gymMetrics;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TraineeController(facade))
                .addMappedInterceptors(new String[] {ApiPaths.API_V1 + "/**"}, new AuthenticationInterceptor(facade, gymMetrics))
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // The three requests from the review: each would give 400 or 403 if the password were checked later

    @Test
    void wrongPassword_invalidBody_returnsUnauthorizedNotBadRequest() throws Exception {
        mockMvc.perform(put(TestPaths.TRAINEES + "/Emily.Johnson")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_WRONG_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        verify(gymMetrics).authenticationFailed();
    }

    @Test
    void wrongPassword_invalidQuery_returnsUnauthorizedNotBadRequest() throws Exception {
        mockMvc.perform(get(TestPaths.TRAINEES + "/Emily.Johnson/trainings")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_WRONG_PASSWORD)
                        .param("periodFrom", "nope"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPassword_anotherUsersProfile_returnsUnauthorizedNotForbidden() throws Exception {
        mockMvc.perform(delete(TestPaths.TRAINEES + "/Michael.Brown")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_WRONG_PASSWORD))
                .andExpect(status().isUnauthorized());

        verify(facade, never()).deleteTrainee(any());
    }

    @Test
    void noAuthorizationHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(get(TestPaths.TRAINEES + "/Emily.Johnson"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(facade);
    }

    @Test
    void trainerCredentials_passToController() throws Exception {
        Credentials emily = new Credentials("Emily.Johnson", "secret");
        when(facade.matchTraineeCredentials(emily)).thenReturn(false);
        when(facade.matchTrainerCredentials(emily)).thenReturn(true);

        // The right password: the request reaches the controller, which now validates the body and returns 400
        mockMvc.perform(put(TestPaths.TRAINEES + "/Emily.Johnson")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_RIGHT_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(gymMetrics, never()).authenticationFailed();
    }
}
