package com.epam.training.gym.service.impl;

import com.epam.training.gym.metrics.GymMetrics;
import com.epam.training.gym.model.*;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidInputException;
import com.epam.training.gym.service.exception.InvalidStateException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceImplTest {

    private static final String USERNAME = "John.Smith";
    private static final String CURRENT_PASSWORD = "abcdef1234";

    private static final Credentials VALID_CREDENTIALS = new Credentials(USERNAME, CURRENT_PASSWORD);
    private static final Credentials WRONG_PASSWORD_CREDENTIALS = new Credentials(USERNAME, "wrong");

    private static final TrainingType YOGA = new TrainingType(2L, TrainingTypeName.YOGA);

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private UserDao userDao;

    @Mock
    private UserAuthenticator userAuthenticator;

    @Mock
    private NewUserDataGenerator newUserDataGenerator;

    @Mock
    private EntityValidator entityValidator;

    @Mock
    private GymMetrics gymMetrics;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Test
    void create_validatesThenGeneratesThenSaves() {
        Trainee trainee = newTrainee();
        Trainee saved = existingTrainee(true);
        when(traineeDao.save(trainee)).thenReturn(saved);

        Trainee result = traineeService.create(trainee);

        assertSame(saved, result);
        InOrder order = inOrder(entityValidator, newUserDataGenerator, traineeDao);
        order.verify(entityValidator).validate(trainee);
        order.verify(newUserDataGenerator).fillNewUserData(trainee.getUser());
        order.verify(traineeDao).save(trainee);
        verify(gymMetrics).traineeRegistered();
    }

    @Test
    void create_returnsGeneratedPassword() {
        Trainee trainee = newTrainee();
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUsername("John.Smith");
            user.setPassword("Gen3rated1");
            return null;
        }).when(newUserDataGenerator).fillNewUserData(trainee.getUser());
        when(traineeDao.save(trainee)).thenReturn(existingTrainee(true));

        Trainee result = traineeService.create(trainee);

        // Registration is the only result with a password: the user needs it to sign in
        assertEquals("Gen3rated1", result.getUser().getPassword());
    }

    @Test
    void create_invalidTrainee_isNotSaved() {
        Trainee trainee = newTrainee();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(trainee);

        assertThrows(ConstraintViolationException.class, () -> traineeService.create(trainee));

        verify(newUserDataGenerator, never()).fillNewUserData(any());
        verify(traineeDao, never()).save(any());
        verify(gymMetrics, never()).traineeRegistered();
    }

    /** A trainee as a caller passes it: a model without id, username and password. */
    private static Trainee newTrainee() {
        return Trainee.builder()
                .user(User.builder().firstName("John").lastName("Smith").build())
                .build();
    }

    // ---------- matchCredentials ----------

    @Test
    void matchCredentials_rightPassword_returnsTrue() {
        signIn(true);

        assertTrue(traineeService.matchCredentials(VALID_CREDENTIALS));
    }

    @Test
    void matchCredentials_wrongPassword_returnsFalse() {
        assertFalse(traineeService.matchCredentials(WRONG_PASSWORD_CREDENTIALS));

        verify(traineeDao, never()).findByUsername(any());
    }

    @Test
    void matchCredentials_trainerCredentials_returnsFalse() {
        when(userAuthenticator.matches(VALID_CREDENTIALS)).thenReturn(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(traineeService.matchCredentials(VALID_CREDENTIALS));
    }

    // ---------- authenticate ----------

    @Test
    void authenticate_rightPassword_returnsOwnProfile() {
        Trainee trainee = signIn(true);

        assertSame(trainee, traineeService.authenticate(VALID_CREDENTIALS));
    }

    @Test
    void authenticate_wrongPassword_throws() {
        assertThrows(AuthenticationException.class, () -> traineeService.authenticate(WRONG_PASSWORD_CREDENTIALS));

        verify(traineeDao, never()).findByUsername(any());
    }

    // ---------- changeActiveStatus ----------

    @Test
    void changeActiveStatus_inactiveTrainee_activates() {
        Trainee trainee = signIn(false);

        traineeService.changeActiveStatus(VALID_CREDENTIALS, true);

        assertTrue(trainee.getUser().isActive());
        verify(userDao).updateActive(trainee.getUser());
    }

    @Test
    void changeActiveStatus_activeTrainee_deactivates() {
        Trainee trainee = signIn(true);

        traineeService.changeActiveStatus(VALID_CREDENTIALS, false);

        assertFalse(trainee.getUser().isActive());
        verify(userDao).updateActive(trainee.getUser());
    }

    @Test
    void changeActiveStatus_sameStatus_throwsAndKeepsStatus() {
        signIn(true);

        assertThrows(InvalidStateException.class, () -> traineeService.changeActiveStatus(VALID_CREDENTIALS, true));

        verify(userDao, never()).updateActive(any());
    }

    @Test
    void changeActiveStatus_wrongPassword_throwsAndKeepsStatus() {
        assertThrows(AuthenticationException.class,
                () -> traineeService.changeActiveStatus(WRONG_PASSWORD_CREDENTIALS, false));

        verify(userDao, never()).updateActive(any());
    }

    /** The caller's credentials are right, and the DAO finds the caller's trainee profile. */
    private Trainee signIn(boolean active) {
        Trainee trainee = existingTrainee(active);
        when(userAuthenticator.matches(VALID_CREDENTIALS)).thenReturn(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        return trainee;
    }

    /** A trainee as the DAO returns it: with ids and username, without password. */
    private static Trainee existingTrainee(boolean active) {
        return Trainee.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .firstName("John")
                        .lastName("Smith")
                        .username(USERNAME)
                        .isActive(active)
                        .build())
                .build();
    }

    // ---------- getProfile ----------

    @Test
    void getProfile_signedInUser_readsAnotherTraineesProfile() {
        when(traineeDao.findByUsernameWithTrainers("Mary.Jones"))
                .thenReturn(Optional.of(traineeWithUsername("Mary.Jones")));

        Trainee result = traineeService.getProfile(VALID_CREDENTIALS, "Mary.Jones");

        assertEquals("Mary.Jones", result.getUser().getUsername());
        verify(userAuthenticator).authenticate(VALID_CREDENTIALS);
    }

    @Test
    void getProfile_unknownUsername_throwsNotFound() {
        when(traineeDao.findByUsernameWithTrainers("User.Name")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.getProfile(VALID_CREDENTIALS, "User.Name"));
    }

    @Test
    void getProfile_wrongCallerCredentials_doesNotReadProfile() {
        doThrow(new AuthenticationException()).when(userAuthenticator).authenticate(WRONG_PASSWORD_CREDENTIALS);

        assertThrows(AuthenticationException.class,
                () -> traineeService.getProfile(WRONG_PASSWORD_CREDENTIALS, "Mary.Jones"));

        verify(traineeDao, never()).findByUsernameWithTrainers(any());
    }

    // ---------- update ----------

    @Test
    void update_changesOnlyAllowedFields() {
        Trainee trainee = signIn(true);
        when(traineeDao.findByUsernameWithTrainers(USERNAME)).thenReturn(Optional.of(trainee));
        Trainee newData = Trainee.builder()
                .dateOfBirth(LocalDate.of(2000, 5, 5))
                .address("9 New Road")
                .user(User.builder()
                        .firstName("Johnny")
                        .lastName("Smithers")
                        .username("Someone.Else")      // must be ignored
                        .password("hacked")            // must be ignored
                        .isActive(false)               // the status is updated too
                        .build())
                .build();

        Trainee result = traineeService.update(VALID_CREDENTIALS, newData);

        // The DAO got the caller's profile with only the allowed fields changed
        verify(traineeDao).update(trainee);
        assertEquals("Johnny", trainee.getUser().getFirstName());
        assertEquals("Smithers", trainee.getUser().getLastName());
        assertEquals(LocalDate.of(2000, 5, 5), trainee.getDateOfBirth());
        assertEquals("9 New Road", trainee.getAddress());
        assertEquals(USERNAME, trainee.getUser().getUsername());
        assertNull(trainee.getUser().getPassword());
        assertFalse(trainee.getUser().isActive());
        // The caller gets the updated profile with its trainers list
        assertSame(trainee, result);
    }

    @Test
    void update_wrongPassword_isRejectedBeforeValidation() {
        assertThrows(AuthenticationException.class,
                () -> traineeService.update(WRONG_PASSWORD_CREDENTIALS, Trainee.builder().build()));

        verify(entityValidator, never()).validate(any());
    }

    @Test
    void update_invalidData_isRejectedAndChangesNothing() {
        Trainee trainee = signIn(true);
        when(traineeDao.findByUsernameWithTrainers(USERNAME)).thenReturn(Optional.of(trainee));
        Trainee newData = Trainee.builder().build();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(newData);

        assertThrows(ConstraintViolationException.class, () -> traineeService.update(VALID_CREDENTIALS, newData));

        verify(traineeDao, never()).update(any());
    }

    // ---------- delete ----------

    @Test
    void delete_rightCredentials_deletesTrainee() {
        Trainee trainee = signIn(true);

        traineeService.delete(VALID_CREDENTIALS);

        verify(traineeDao).delete(trainee);
    }

    @Test
    void delete_wrongPassword_deletesNothing() {
        assertThrows(AuthenticationException.class, () -> traineeService.delete(WRONG_PASSWORD_CREDENTIALS));

        verify(traineeDao, never()).delete(any());
    }

    // ---------- getTrainings ----------

    @Test
    void getTrainings_passesUsernameAndCriteriaToDao() {
        signIn(true);
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder().trainerName("Anna").build();
        when(trainingDao.findTraineeTrainings(USERNAME, criteria)).thenReturn(List.of(training("Morning yoga")));

        List<Training> result = traineeService.getTrainings(VALID_CREDENTIALS, criteria);

        assertEquals(List.of("Morning yoga"), result.stream().map(Training::getTrainingName).toList());
    }

    @Test
    void getTrainings_nullCriteria_meansNoFilters() {
        signIn(true);
        TraineeTrainingCriteria noFilters = TraineeTrainingCriteria.builder().build();
        when(trainingDao.findTraineeTrainings(USERNAME, noFilters)).thenReturn(List.of());

        assertTrue(traineeService.getTrainings(VALID_CREDENTIALS, null).isEmpty());
    }

    @Test
    void getTrainings_wrongPassword_doesNotSearch() {
        assertThrows(AuthenticationException.class,
                () -> traineeService.getTrainings(WRONG_PASSWORD_CREDENTIALS, null));

        verify(trainingDao, never()).findTraineeTrainings(any(), any());
    }

    @Test
    void getTrainings_fromDateAfterToDate_throwsAndDoesNotSearch() {
        signIn(true);
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder()
                .fromDate(LocalDate.of(2026, 5, 2))
                .toDate(LocalDate.of(2026, 5, 1))
                .build();

        assertThrows(InvalidInputException.class, () -> traineeService.getTrainings(VALID_CREDENTIALS, criteria));
        verify(trainingDao, never()).findTraineeTrainings(any(), any());
    }

    // ---------- getUnassignedTrainers ----------

    @Test
    void getUnassignedTrainers_returnsDaoResult() {
        signIn(true);
        when(trainerDao.findActiveNotAssignedToTrainee(USERNAME))
                .thenReturn(List.of(trainerWithUsername("Mark.Hill")));

        List<Trainer> result = traineeService.getUnassignedTrainers(VALID_CREDENTIALS);

        assertEquals(List.of("Mark.Hill"), result.stream().map(trainer -> trainer.getUser().getUsername()).toList());
    }

    @Test
    void getUnassignedTrainers_wrongPassword_doesNotSearch() {
        assertThrows(AuthenticationException.class,
                () -> traineeService.getUnassignedTrainers(WRONG_PASSWORD_CREDENTIALS));

        verify(trainerDao, never()).findActiveNotAssignedToTrainee(any());
    }

    // ---------- updateTrainers ----------

    @Test
    void updateTrainers_replacesWholeList() {
        Trainee trainee = signIn(true);
        Trainer anna = trainerWithUsername("Anna.Lee");
        Trainer mark = trainerWithUsername("Mark.Hill");
        when(trainerDao.findByUsernames(Set.of("Anna.Lee", "Mark.Hill"))).thenReturn(List.of(anna, mark));

        List<Trainer> result = traineeService.updateTrainers(VALID_CREDENTIALS, List.of("Anna.Lee", "Mark.Hill"));

        verify(traineeDao).replaceTrainers(trainee, List.of(anna, mark));
        assertEquals(List.of(anna, mark), result);
    }

    @Test
    void updateTrainers_unknownUsername_throwsAndKeepsOldList() {
        signIn(true);
        when(trainerDao.findByUsernames(Set.of("Anna.Lee", "No.Body")))
                .thenReturn(List.of(trainerWithUsername("Anna.Lee")));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainers(VALID_CREDENTIALS, List.of("Anna.Lee", "No.Body")));

        assertTrue(exception.getMessage().contains("No.Body"));
        verify(traineeDao, never()).replaceTrainers(any(), any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("listsWithBadUsernames")
    void updateTrainers_invalidList_throwsAndKeepsOldList(List<String> trainerUsernames) {
        signIn(true);

        assertThrows(InvalidInputException.class,
                () -> traineeService.updateTrainers(VALID_CREDENTIALS, trainerUsernames));

        verify(trainerDao, never()).findByUsernames(any());
        verify(traineeDao, never()).replaceTrainers(any(), any());
    }

    static Stream<List<String>> listsWithBadUsernames() {
        return Stream.of(
                List.of("Anna.Lee", " "),             // blank username
                Arrays.asList("Anna.Lee", null),      // null username
                List.of("Anna.Lee", "Anna.Lee"));     // repeated username
    }

    private static Trainee traineeWithUsername(String username) {
        return Trainee.builder().user(User.builder().username(username).build()).build();
    }

    private static Trainer trainerWithUsername(String username) {
        return Trainer.builder()
                .user(User.builder().username(username).build())
                .specialization(YOGA)
                .build();
    }

    private static Training training(String name) {
        return Training.builder()
                .trainingName(name)
                .trainee(existingTrainee(true))
                .trainer(trainerWithUsername("Anna.Lee"))
                .trainingType(YOGA)
                .trainingDate(LocalDate.of(2026, 10, 5))
                .trainingDuration(60)
                .build();
    }
}
