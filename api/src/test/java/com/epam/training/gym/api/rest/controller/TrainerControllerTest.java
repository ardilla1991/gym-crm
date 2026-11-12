package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.api.rest.dto.RegistrationResponse;
import com.epam.training.gym.api.rest.dto.TrainerProfileResponse;
import com.epam.training.gym.api.rest.exception.AccessDeniedException;
import com.epam.training.gym.api.rest.mapper.TrainerMapper;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.TrainingType;
import com.epam.training.gym.domain.TrainingTypeName;
import com.epam.training.gym.domain.User;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.service.exception.InvalidStateException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void update_anotherTrainerProfile_throwsAccessDenied() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(put(TestPaths.TRAINERS + "/Mark.Brown")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
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
    void getTrainings_anotherTrainer_throwsAccessDenied() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(get(TestPaths.TRAINERS + "/Mark.Brown/trainings")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
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
    void changeActiveStatus_sameStatus_throwsInvalidState() {
        doThrow(new InvalidStateException("Trainer is already inactive"))
                .when(facade).changeTrainerActiveStatus(ANNA, false);

        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(patch(TestPaths.TRAINERS + "/Anna.Lee/active")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON)));
        assertInstanceOf(InvalidStateException.class, e.getCause());
    }

    @Test
    void changeActiveStatus_anotherTrainer_throwsAccessDenied() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(patch(TestPaths.TRAINERS + "/Mark.Brown/active")
                        .header(HttpHeaders.AUTHORIZATION, ANNA_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
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
