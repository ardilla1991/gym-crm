package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.api.rest.dto.RegistrationResponse;
import com.epam.training.gym.api.rest.dto.TrainerProfileResponse;
import com.epam.training.gym.api.rest.exception.GlobalExceptionHandler;
import com.epam.training.gym.api.rest.mapper.TrainerMapper;
import com.epam.training.gym.model.Trainer;
import com.epam.training.gym.model.TrainingType;
import com.epam.training.gym.model.TrainingTypeName;
import com.epam.training.gym.model.User;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.service.exception.InvalidStateException;
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
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TrainerControllerTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** Any existing constant of the enum, so the test does not depend on its exact values. */
    private static final TrainingType YOGA = new TrainingType(2L, TrainingTypeName.YOGA);

    private static final Credentials ANNA = new Credentials("Anna.Lee", "secret");
    private static final String ANNA_HEADER = TestAuth.basic("Anna.Lee", "secret");
    private static final Credentials EMILY = new Credentials("Emily.Johnson", "secret");
    private static final String EMILY_HEADER = TestAuth.basic("Emily.Johnson", "secret");

    private static final String UPDATE_JSON = """
            {"firstName": "Hanna", "lastName": "Lee", "isActive": false}
            """;

    private static final String DEACTIVATE_JSON = """
            {"isActive": false}
            """;

    @Mock
    private GymFacade facade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TrainerController(facade))
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void register_returnsCreatedWithUsernameAndPassword() throws Exception {
        when(facade.createTrainer(any())).thenAnswer(invocation -> {
            Trainer trainer = invocation.getArgument(0);
            trainer.getUser().setUsername("Anna.Lee");
            trainer.getUser().setPassword("0987654321");
            return trainer;
        });

        String body = mockMvc.perform(post(TestPaths.TRAINERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "Anna", "lastName": "Lee", "specialization": "YOGA"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertEquals(new RegistrationResponse("Anna.Lee", "0987654321"),
                JSON.readValue(body, RegistrationResponse.class));

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(facade).createTrainer(captor.capture());
        assertEquals(TrainingTypeName.YOGA, captor.getValue().getSpecialization().getTrainingTypeName());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"firstName\": \"Anna\", \"lastName\": \"Lee\"}",                              // no specialization
            "{\"firstName\": \"\", \"lastName\": \"Lee\", \"specialization\": \"YOGA\"}"      // blank first name
    })
    void register_invalidRequest_returnsBadRequest(String json) throws Exception {
        mockMvc.perform(post(TestPaths.TRAINERS).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void register_unknownSpecialization_returnsBadRequest() throws Exception {
        mockMvc.perform(post(TestPaths.TRAINERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Anna\", \"lastName\": \"Lee\", \"specialization\": \"yoga\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void getProfile_anySignedInUser_canReadTrainerProfile() throws Exception {
        // Emily, a trainee, reads Anna's trainer profile
        Trainer anna = trainer("Anna.Lee", "Anna", "Lee");
        when(facade.getTrainerProfile(EMILY, "Anna.Lee")).thenReturn(anna);

        String body = mockMvc.perform(get(TestPaths.TRAINERS + "/Anna.Lee")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(TrainerMapper.toProfileResponse(anna), JSON.readValue(body, TrainerProfileResponse.class));
    }

    @Test
    void update_ownProfile_returnsUpdatedProfile() throws Exception {
        Trainer updated = trainer("Anna.Lee", "Hanna", "Lee");
        when(facade.updateTrainer(eq(ANNA), any())).thenReturn(updated);

        String body = mockMvc.perform(put(TestPaths.TRAINERS + "/Anna.Lee")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(facade).updateTrainer(eq(ANNA), captor.capture());
        Trainer newData = captor.getValue();
        assertEquals("Hanna", newData.getUser().getFirstName());
        assertFalse(newData.getUser().isActive());
        assertNull(newData.getSpecialization());     // read only: never taken from the request

        assertEquals(TrainerMapper.toProfileResponse(updated), JSON.readValue(body, TrainerProfileResponse.class));
    }

    @Test
    void update_anotherTrainerProfile_returnsForbidden() throws Exception {
        mockMvc.perform(put(TestPaths.TRAINERS + "/Mark.Brown")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(facade);
    }

    @Test
    void update_noIsActive_returnsBadRequest() throws Exception {
        mockMvc.perform(put(TestPaths.TRAINERS + "/Anna.Lee")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Hanna\", \"lastName\": \"Lee\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void getTrainings_allCriteria_passedToFacade() throws Exception {
        var expected = new TrainerTrainingCriteria(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), "Emily");
        when(facade.getTrainerTrainings(ANNA, expected)).thenReturn(List.of());

        String body = mockMvc.perform(get(TestPaths.TRAINERS + "/Anna.Lee/trainings")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .param("periodFrom", "2026-01-01")
                        .param("periodTo", "2026-03-31")
                        .param("traineeName", "Emily"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("[]", body);
    }

    @Test
    void getTrainings_noCriteria_passesEmptyCriteria() throws Exception {
        var empty = new TrainerTrainingCriteria(null, null, null);
        when(facade.getTrainerTrainings(ANNA, empty)).thenReturn(List.of());

        mockMvc.perform(get(TestPaths.TRAINERS + "/Anna.Lee/trainings")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER))
                .andExpect(status().isOk());

        verify(facade).getTrainerTrainings(ANNA, empty);
    }

    @Test
    void getTrainings_anotherTrainer_returnsForbidden() throws Exception {
        mockMvc.perform(get(TestPaths.TRAINERS + "/Mark.Brown/trainings")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(facade);
    }

    @Test
    void changeActiveStatus_ownProfile_passesNewStatus() throws Exception {
        mockMvc.perform(patch(TestPaths.TRAINERS + "/Anna.Lee/active")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON))
                .andExpect(status().isOk());

        verify(facade).changeTrainerActiveStatus(ANNA, false);
    }

    @Test
    void changeActiveStatus_sameStatus_returnsConflict() throws Exception {
        doThrow(new InvalidStateException("Trainer is already inactive"))
                .when(facade).changeTrainerActiveStatus(ANNA, false);

        mockMvc.perform(patch(TestPaths.TRAINERS + "/Anna.Lee/active")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void changeActiveStatus_anotherTrainer_returnsForbidden() throws Exception {
        mockMvc.perform(patch(TestPaths.TRAINERS + "/Mark.Brown/active")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(facade);
    }

    @Test
    void changeActiveStatus_noIsActive_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(TestPaths.TRAINERS + "/Anna.Lee/active")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    /** A saved trainer as the facade would return it. */
    private static Trainer trainer(String username, String firstName, String lastName) {
        return Trainer.builder()
                .user(user(username, firstName, lastName))
                .specialization(YOGA)
                .build();
    }

    private static User user(String username, String firstName, String lastName) {
        return User.builder()
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .isActive(true)
                .build();
    }
}
