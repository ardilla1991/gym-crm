package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.api.rest.dto.TrainingTypeResponse;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
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
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainingTypeControllerTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final Credentials EMILY = new Credentials("Emily.Johnson", "secret");
    private static final String EMILY_HEADER = TestAuth.basic("Emily.Johnson", "secret");

    @Mock
    private GymFacade facade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TrainingTypeController(facade))
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .build();
    }

    @Test
    void getAll_returnsIdAndNameOfEachType() throws Exception {
        when(facade.getTrainingTypes(EMILY)).thenReturn(List.of(new TrainingType(2L, TrainingTypeName.YOGA)));

        String body = mockMvc.perform(get(TestPaths.TRAINING_TYPES)
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(List.of(new TrainingTypeResponse(2L, TrainingTypeName.YOGA)),
                List.of(JSON.readValue(body, TrainingTypeResponse[].class)));
    }

    @Test
    void getAll_noAuthorizationHeader_doesNotCallFacade() {
        // Note 3: even the list of types requires authentication
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(get(TestPaths.TRAINING_TYPES)));
        assertInstanceOf(AuthenticationException.class, e.getCause());
        verifyNoInteractions(facade);
    }

}
