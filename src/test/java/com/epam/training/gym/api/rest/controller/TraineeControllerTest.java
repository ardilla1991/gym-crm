package com.epam.training.gym.api.rest.controller;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.api.rest.TestPaths;
import com.epam.training.gym.api.rest.auth.CredentialsArgumentResolver;
import com.epam.training.gym.api.rest.auth.TestAuth;
import com.epam.training.gym.api.rest.dto.RegistrationResponse;
import com.epam.training.gym.api.rest.dto.TraineeProfileResponse;
import com.epam.training.gym.api.rest.dto.TrainerSummary;
import com.epam.training.gym.api.rest.exception.ApiError;
import com.epam.training.gym.api.rest.exception.GlobalExceptionHandler;
import com.epam.training.gym.model.*;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.model.validation.FieldLimits;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
                .setControllerAdvice(new GlobalExceptionHandler())
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

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"lastName\": \"Smith\"}",                                         // no first name
            "{\"firstName\": \" \", \"lastName\": \"Smith\"}",                   // blank first name
            "{\"firstName\": \"John\"}",                                         // no last name
            "{\"firstName\": \"Review:Colon\", \"lastName\": \"User\"}",          // ':' breaks HTTP Basic
            "{\"firstName\": \"Review/Slash\", \"lastName\": \"User\"}",          // '/' breaks /trainees/{username}
            "{\"firstName\": \"John\", \"lastName\": \"Smith\", \"dateOfBirth\": \"2999-01-01\"}" // future date
    })
    void register_invalidRequest_returnsBadRequest(String json) throws Exception {
        mockMvc.perform(post(TestPaths.TRAINEES).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @ParameterizedTest
    @MethodSource("tooLongRegistrations")
    void register_tooLongField_returnsBadRequest(String json) throws Exception {
        mockMvc.perform(post(TestPaths.TRAINEES).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    /** Values one character longer than their columns: without @Size the database would reject them with 500. */
    static Stream<String> tooLongRegistrations() {
        return Stream.of(
                "{\"firstName\": \"" + "a".repeat(FieldLimits.NAME_MAX + 1) + "\", \"lastName\": \"Smith\"}",
                "{\"firstName\": \"John\", \"lastName\": \"Smith\", \"address\": \""
                        + "a".repeat(FieldLimits.ADDRESS_MAX + 1) + "\"}");
    }

    @Test
    void register_invalidRequest_namesEveryInvalidField() throws Exception {
        String body = mockMvc.perform(post(TestPaths.TRAINEES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \" \"}"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        String message = JSON.readValue(body, ApiError.class).message();
        assertTrue(message.contains("firstName"), message);
        assertTrue(message.contains("lastName"), message);
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

        assertEquals(new TraineeProfileResponse("John.Smith", "John", "Smith", LocalDate.of(2000, 1, 15),
                        "Main St 1", true, List.of()),
                JSON.readValue(body, TraineeProfileResponse.class));
        // The field name is part of the contract with clients
        assertTrue(body.contains("\"isActive\":true"), body);
    }

    @Test
    void getProfile_noAuthorizationHeader_doesNotCallFacade() throws Exception {
        mockMvc.perform(get(TestPaths.TRAINEES + "/John.Smith"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(facade);
    }

    @Test
    void getProfile_unknownTrainee_returnsNotFound() throws Exception {
        when(facade.getTraineeProfile(EMILY, "Nobody.Here"))
                .thenThrow(new EntityNotFoundException("Trainee", "Nobody.Here"));

        String body = mockMvc.perform(get(TestPaths.TRAINEES + "/Nobody.Here")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        ApiError error = JSON.readValue(body, ApiError.class);
        assertEquals(404, error.status());
        assertTrue(error.message().contains("Nobody.Here"), error.message());
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

        assertEquals(new TraineeProfileResponse("Emily.Johnson", "Emma", "Johnson", LocalDate.of(1999, 5, 20),
                        "New St 2", false, List.of()),
                JSON.readValue(body, TraineeProfileResponse.class));
    }

    @Test
    void update_anotherTraineeProfile_returnsForbidden() throws Exception {
        mockMvc.perform(put(TestPaths.TRAINEES + "/John.Smith")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(facade);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"firstName\": \"Emma\", \"lastName\": \"Johnson\"}",                     // no isActive
            "{\"firstName\": \"\", \"lastName\": \"Johnson\", \"isActive\": true}"      // blank first name
    })
    void update_invalidRequest_returnsBadRequest(String json) throws Exception {
        mockMvc.perform(put(TestPaths.TRAINEES + "/Emily.Johnson")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

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
    void delete_anotherTraineeProfile_returnsForbidden() throws Exception {
        mockMvc.perform(delete(TestPaths.TRAINEES + "/John.Smith")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isForbidden());

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

        assertEquals(List.of(new TrainerSummary("Anna.Lee", "Anna", "Lee", TrainingTypeName.YOGA),
                        new TrainerSummary("Mark.Brown", "Mark", "Brown", TrainingTypeName.YOGA)),
                List.of(JSON.readValue(body, TrainerSummary[].class)));
    }

    @Test
    void getUnassignedTrainers_anotherTrainee_returnsForbidden() throws Exception {
        mockMvc.perform(get(TestPaths.TRAINEES + "/John.Smith/unassigned-trainers")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isForbidden());

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

        assertEquals(List.of(new TrainerSummary("Anna.Lee", "Anna", "Lee", TrainingTypeName.YOGA),
                        new TrainerSummary("Mark.Brown", "Mark", "Brown", TrainingTypeName.YOGA)),
                List.of(JSON.readValue(body, TrainerSummary[].class)));
    }

    @Test
    void updateTrainers_anotherTrainee_returnsForbidden() throws Exception {
        mockMvc.perform(put(TestPaths.TRAINEES + "/John.Smith/trainers")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TRAINERS_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(facade);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",                                                 // no list
            "{\"trainerUsernames\": []}",                         // empty list
            "{\"trainerUsernames\": [\"Anna.Lee\", \" \"]}"       // blank username in the list
    })
    void updateTrainers_invalidRequest_returnsBadRequest(String json) throws Exception {
        mockMvc.perform(put(TestPaths.TRAINEES + "/Emily.Johnson/trainers")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

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
    void getTrainings_anotherTrainee_returnsForbidden() throws Exception {
        mockMvc.perform(get(TestPaths.TRAINEES + "/John.Smith/trainings")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER))
                .andExpect(status().isForbidden());

        verifyNoInteractions(facade);
    }

    @Test
    void getTrainings_invalidDate_returnsBadRequest() throws Exception {
        String body = mockMvc.perform(get(TestPaths.TRAINEES + "/Emily.Johnson/trainings")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .param("periodFrom", "abc"))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        assertEquals(new ApiError(400, "Bad Request", "Invalid value 'abc' of parameter 'periodFrom'"),
                JSON.readValue(body, ApiError.class));
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
    void changeActiveStatus_sameStatus_returnsConflict() throws Exception {
        // Not idempotent: the trainee is already inactive
        doThrow(new InvalidStateException("Trainee is already inactive"))
                .when(facade).changeTraineeActiveStatus(EMILY, false);

        mockMvc.perform(patch(TestPaths.TRAINEES + "/Emily.Johnson/active")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void changeActiveStatus_anotherTrainee_returnsForbidden() throws Exception {
        mockMvc.perform(patch(TestPaths.TRAINEES + "/John.Smith/active")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DEACTIVATE_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(facade);
    }

    @Test
    void changeActiveStatus_noIsActive_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(TestPaths.TRAINEES + "/Emily.Johnson/active")
                        .header(HttpHeaders.AUTHORIZATION, EMILY_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

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
