package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.api.rest.exception.GlobalExceptionHandler;
import com.epam.training.gym.model.Training;
import com.epam.training.gym.model.auth.Credentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainingControllerTest {

    private static final Credentials EMILY = new Credentials("Emily.Johnson", "secret");
    private static final String EMILY_HEADER = TestAuth.basic("Emily.Johnson", "secret");

    /** Emily adds a training with Anna. */
    private static final String ADD_JSON = """
            {"traineeUsername": "Emily.Johnson", "trainerUsername": "Anna.Lee",
             "trainingName": "Morning yoga", "trainingDate": "2026-10-05", "trainingDuration": 60}
            """;

    @Mock
    private GymFacade facade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TrainingController(facade))
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void add_ownTraining_passesTrainerAndTrainingToFacade() throws Exception {
        mockMvc.perform(post(TestPaths.TRAININGS)
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_JSON))
                .andExpect(status().isOk());

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(facade).addTraining(eq(EMILY), eq("Anna.Lee"), captor.capture());
        Training training = captor.getValue();
        assertEquals("Morning yoga", training.getTrainingName());
        assertEquals(LocalDate.of(2026, 10, 5), training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());
    }

    @Test
    void add_forAnotherTrainee_returnsForbidden() throws Exception {
        // Signed in as John, but the training is for Emily
        mockMvc.perform(post(TestPaths.TRAININGS)
                        .header(HttpHeaders.AUTHORIZATION, TestAuth.basic("John.Smith", "secret"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(facade);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // no training date
            "{\"traineeUsername\": \"Emily.Johnson\", \"trainerUsername\": \"Anna.Lee\","
                    + " \"trainingName\": \"Morning yoga\", \"trainingDuration\": 60}",
            // zero duration
            "{\"traineeUsername\": \"Emily.Johnson\", \"trainerUsername\": \"Anna.Lee\","
                    + " \"trainingName\": \"Morning yoga\", \"trainingDate\": \"2026-10-05\", \"trainingDuration\": 0}",
            // blank trainer username
            "{\"traineeUsername\": \"Emily.Johnson\", \"trainerUsername\": \" \","
                    + " \"trainingName\": \"Morning yoga\", \"trainingDate\": \"2026-10-05\", \"trainingDuration\": 60}"
    })
    void add_invalidRequest_returnsBadRequest(String json) throws Exception {
        mockMvc.perform(post(TestPaths.TRAININGS)
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

}
