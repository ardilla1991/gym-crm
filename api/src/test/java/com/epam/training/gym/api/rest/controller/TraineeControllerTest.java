package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.api.rest.dto.*;
import com.epam.training.gym.api.rest.exception.AccessDeniedException;
import com.epam.training.gym.api.rest.mapper.TraineeMapper;
import com.epam.training.gym.api.rest.mapper.TrainerMapper;
import com.epam.training.gym.domain.*;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.InvalidStateException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TraineeControllerTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final Credentials EMILY = new Credentials("Emily.Johnson", "secret");
    private static final String EMILY_HEADER = TestAuth.basic("Emily.Johnson", "secret");
    private static final String UPDATE_JSON = """
            {"firstName": "Emma", "lastName": "Johnson",
             "dateOfBirth": "1999-05-20", "address": "New St 2", "isActive": false}
            """;

    private static final String TRAINERS_JSON = """
            {"trainerUsernames": ["Anna.Lee", "Mark.Brown"]}
            """;

    private static final TrainingType YOGA = new TrainingType(2L, TrainingTypeName.YOGA);

    private static final String DEACTIVATE_JSON = """
            {"isActive": false}
            """;

    @Mock
    private GymFacade facade;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TraineeController(facade))
                .setCustomArgumentResolvers(new CredentialsArgumentResolver())
                .build();
    }

    @Test
    void register_returnsCreatedWithUsernameAndPassword() throws Exception {
        // The facade "saves" the trainee: sets the generated username and password, as the real service does
        when(facade.createTrainee(any())).thenAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(0);
            trainee.getUser().setUsername("John.Smith");
            trainee.getUser().setPassword("1234567890");
            return trainee;
        });

        String body = mockMvc.perform(post(TestPaths.TRAINEES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName": "John", "lastName": "Smith",
                                 "dateOfBirth": "2000-01-15", "address": "Main St 1"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertEquals(new RegistrationResponse("John.Smith", "1234567890"),
                JSON.readValue(body, RegistrationResponse.class));

        // The request JSON reached the facade as an entity
        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(facade).createTrainee(captor.capture());
        assertEquals("John", captor.getValue().getUser().getFirstName());
    }

    @Test
    void getProfile_anySignedInUser_canReadAnotherTraineeProfile() throws Exception {
        // Emily reads John's profile: reading is allowed to any signed-in user
        Trainee john = trainee("John.Smith", "John", "Smith");
        when(facade.getTraineeProfile(EMILY, "John.Smith")).thenReturn(john);

        String body = mockMvc.perform(get(TestPaths.TRAINEES + "/John.Smith")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(TraineeMapper.toProfileResponse(john), JSON.readValue(body, TraineeProfileResponse.class));
        // The field name is part of the contract with clients
        assertTrue(body.contains("\"isActive\":true"), body);
    }

    @Test
    void getProfile_noAuthorizationHeader_doesNotCallFacade() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(get(TestPaths.TRAINEES + "/John.Smith")));
        assertInstanceOf(AuthenticationException.class, e.getCause());
        verifyNoInteractions(facade);
    }

    @Test
    void update_ownProfile_returnsUpdatedProfile() throws Exception {
        // The facade returns the updated trainee: the new data plus the unchanged username
        when(facade.updateTrainee(eq(EMILY), any())).thenAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(1);
            trainee.getUser().setUsername("Emily.Johnson");
            return trainee;
        });

        String body = mockMvc.perform(put(TestPaths.TRAINEES + "/Emily.Johnson")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // The request JSON reached the facade as an entity, including isActive
        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(facade).updateTrainee(eq(EMILY), captor.capture());
        Trainee newData = captor.getValue();
        assertEquals("Emma", newData.getUser().getFirstName());
        assertFalse(newData.getUser().isActive());

        assertEquals(TraineeMapper.toProfileResponse(newData), JSON.readValue(body, TraineeProfileResponse.class));
    }

    @Test
    void update_anotherTraineeProfile_throwsAccessDenied() {
        // Emily tries to update John's profile
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(put(TestPaths.TRAINEES + "/John.Smith")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
        verifyNoInteractions(facade);
    }

    @Test
    void delete_ownProfile_returnsOk() throws Exception {
        mockMvc.perform(delete(TestPaths.TRAINEES + "/Emily.Johnson")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isOk());

        verify(facade).deleteTrainee(EMILY);
    }

    @Test
    void delete_anotherTraineeProfile_throwsAccessDenied() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(delete(TestPaths.TRAINEES + "/John.Smith")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
        verifyNoInteractions(facade);
    }

    @Test
    void getUnassignedTrainers_ownList_returnsTrainerSummaries() throws Exception {
        List<Trainer> trainers = List.of(trainer("Anna.Lee", "Anna", "Lee"), trainer("Mark.Brown", "Mark", "Brown"));
        when(facade.getUnassignedTrainers(EMILY)).thenReturn(trainers);

        String body = mockMvc.perform(get(TestPaths.TRAINEES + "/Emily.Johnson/unassigned-trainers")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(TrainerMapper.toSummaries(trainers), List.of(JSON.readValue(body, TrainerSummary[].class)));
    }

    @Test
    void getUnassignedTrainers_anotherTrainee_throwsAccessDenied() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(get(TestPaths.TRAINEES + "/John.Smith/unassigned-trainers")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
        verifyNoInteractions(facade);
    }

    @Test
    void updateTrainers_ownList_returnsNewTrainerList() throws Exception {
        List<Trainer> trainers = List.of(trainer("Anna.Lee", "Anna", "Lee"), trainer("Mark.Brown", "Mark", "Brown"));
        when(facade.updateTraineeTrainers(EMILY, List.of("Anna.Lee", "Mark.Brown"))).thenReturn(trainers);

        String body = mockMvc.perform(put(TestPaths.TRAINEES + "/Emily.Johnson/trainers")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRAINERS_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(TrainerMapper.toSummaries(trainers), List.of(JSON.readValue(body, TrainerSummary[].class)));
    }

    @Test
    void updateTrainers_anotherTrainee_throwsAccessDenied() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(put(TestPaths.TRAINEES + "/John.Smith/trainers")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRAINERS_JSON)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
        verifyNoInteractions(facade);
    }

    @Test
    void getTrainings_allCriteria_passedToFacade() throws Exception {
        TrainingTypeName type = TrainingTypeName.values()[0];
        var expected = new TraineeTrainingCriteria(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), "Anna", type);
        when(facade.getTraineeTrainings(EMILY, expected)).thenReturn(List.of());

        String body = mockMvc.perform(get(TestPaths.TRAINEES + "/Emily.Johnson/trainings")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .param("periodFrom", "2026-01-01")
                        .param("periodTo", "2026-03-31")
                        .param("trainerName", "Anna")
                        .param("trainingType", type.name()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("[]", body);
    }

    @Test
    void getTrainings_noCriteria_passesEmptyCriteria() throws Exception {
        when(facade.getTraineeTrainings(EMILY, new TraineeTrainingCriteria(null, null, null, null)))
                .thenReturn(List.of());

        mockMvc.perform(get(TestPaths.TRAINEES + "/Emily.Johnson/trainings")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isOk());

        verify(facade).getTraineeTrainings(EMILY, new TraineeTrainingCriteria(null, null, null, null));
    }

    @Test
    void getTrainings_anotherTrainee_throwsAccessDenied() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(get(TestPaths.TRAINEES + "/John.Smith/trainings")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
        verifyNoInteractions(facade);
    }

    @Test
    void changeActiveStatus_ownProfile_passesNewStatus() throws Exception {
        mockMvc.perform(patch(TestPaths.TRAINEES + "/Emily.Johnson/active")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON))
                .andExpect(status().isOk());

        verify(facade).changeTraineeActiveStatus(EMILY, false);
    }

    @Test
    void changeActiveStatus_sameStatus_throwsInvalidState() {
        // Not idempotent: the trainee is already inactive
        doThrow(new InvalidStateException("Trainee is already inactive"))
                .when(facade).changeTraineeActiveStatus(EMILY, false);

        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(patch(TestPaths.TRAINEES + "/Emily.Johnson/active")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON)));
        assertInstanceOf(InvalidStateException.class, e.getCause());
    }

    @Test
    void changeActiveStatus_anotherTrainee_throwsAccessDenied() {
        ServletException e = assertThrows(ServletException.class, () ->
                mockMvc.perform(patch(TestPaths.TRAINEES + "/John.Smith/active")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON)));
        assertInstanceOf(AccessDeniedException.class, e.getCause());
        verifyNoInteractions(facade);
    }

    /** A saved trainee as the facade would return it. */
    private static Trainee trainee(String username, String firstName, String lastName) {
        return Trainee.builder()
                .user(user(username, firstName, lastName))
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .address("Main St 1")
                .build();
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
