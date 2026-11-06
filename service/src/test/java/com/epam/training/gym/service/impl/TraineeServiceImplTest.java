package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.Training;
import com.epam.training.gym.domain.User;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.domain.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TraineeServiceImplTest {

    private static final String USERNAME = "John.Smith";
    private static final String CURRENT_PASSWORD = "abcdef1234";
    private static final String NEW_PASSWORD = "newPass123";

    private static final Credentials VALID_CREDENTIALS = new Credentials(USERNAME, CURRENT_PASSWORD);
    private static final Credentials WRONG_PASSWORD_CREDENTIALS = new Credentials(USERNAME, "wrong");

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private UserAuthenticator userAuthenticator;

    @Mock
    private NewUserDataGenerator newUserDataGenerator;

    @Mock
    private EntityValidator entityValidator;

    @InjectMocks
    private TraineeServiceImpl traineeService;

    @Test
    void create_validatesThenGeneratesThenSaves() {
        Trainee trainee = newTrainee();
        when(traineeDao.save(trainee)).thenReturn(trainee);

        Trainee result = traineeService.create(trainee);

        assertSame(trainee, result);
        InOrder order = inOrder(entityValidator, newUserDataGenerator, traineeDao);
        order.verify(entityValidator).validate(trainee);
        order.verify(newUserDataGenerator).fillNewUserData(trainee.getUser());
        order.verify(traineeDao).save(trainee);
    }

    @Test
    void create_invalidTrainee_isNotSaved() {
        Trainee trainee = newTrainee();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(trainee);

        assertThrows(ConstraintViolationException.class, () -> traineeService.create(trainee));

        verify(newUserDataGenerator, never()).fillNewUserData(any());
        verify(traineeDao, never()).save(any());
    }

    private static Trainee newTrainee() {
        return Trainee.builder()
                .user(User.builder().firstName("John").lastName("Smith").build())
                .build();
    }

    // ---------- matchCredentials ----------

    @Test
    void matchCredentials_rightPassword_returnsTrue() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));

        assertTrue(traineeService.matchCredentials(VALID_CREDENTIALS));
    }

    @Test
    void matchCredentials_wrongPassword_returnsFalse() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));

        assertFalse(traineeService.matchCredentials(WRONG_PASSWORD_CREDENTIALS));
    }

    @Test
    void matchCredentials_unknownUsername_returnsFalse() {
        when(traineeDao.findByUsername("User.Name")).thenReturn(Optional.empty());

        assertFalse(traineeService.matchCredentials(new Credentials("User.Name", CURRENT_PASSWORD)));
    }

// ---------- changePassword ----------

    @Test
    void changePassword_rightCredentials_setsNewPassword() {
        Trainee trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        traineeService.changePassword(VALID_CREDENTIALS, NEW_PASSWORD );

        assertEquals(NEW_PASSWORD, trainee.getUser().getPassword());
    }

    @Test
    void changePassword_wrongPassword_throwsAndKeepsOldPassword() {
        Trainee trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class,
                () -> traineeService.changePassword(WRONG_PASSWORD_CREDENTIALS, NEW_PASSWORD));

        assertEquals(CURRENT_PASSWORD, trainee.getUser().getPassword());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void changePassword_blankNewPassword_throwsWithoutDatabaseAccess(String newPassword) {
        assertThrows(IllegalArgumentException.class, () -> traineeService.changePassword(VALID_CREDENTIALS, newPassword));

        verify(traineeDao, never()).findByUsername(any());
    }

// ---------- toggleActive ----------

    @Test
    void toggleActive_activeTrainee_becomesInactive() {
        Trainee trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertFalse(traineeService.toggleActive(VALID_CREDENTIALS));
        assertFalse(trainee.getUser().isActive());
    }

    @Test
    void toggleActive_calledTwice_returnsToOriginalStatus() {
        Trainee trainee = existingTrainee(false);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.toggleActive(VALID_CREDENTIALS));      // inactive -> active
        assertFalse(traineeService.toggleActive(VALID_CREDENTIALS));     // active -> inactive: not idempotent
    }

    @Test
    void toggleActive_wrongPassword_throwsAndKeepsStatus() {
        Trainee trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class, () -> traineeService.toggleActive(WRONG_PASSWORD_CREDENTIALS));

        assertTrue(trainee.getUser().isActive());
    }

    /** A trainee as if loaded from the database: with username and password. */
    private static Trainee existingTrainee(boolean active) {
        return Trainee.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .firstName("John")
                        .lastName("Smith")
                        .username(USERNAME)
                        .password(CURRENT_PASSWORD)
                        .isActive(active)
                        .build())
                .build();
    }

// ---------- getProfile ----------

    @Test
    void getProfile_signedInUser_readsAnotherTraineesProfile() {
        Trainee mary = traineeWithUsername("Mary.Jones");
        when(traineeDao.findByUsername("Mary.Jones")).thenReturn(Optional.of(mary));

        Trainee result = traineeService.getProfile(VALID_CREDENTIALS, "Mary.Jones");

        assertSame(mary, result);
        verify(userAuthenticator).authenticate(VALID_CREDENTIALS);
    }

    @Test
    void getProfile_unknownUsername_throwsNotFound() {
        when(traineeDao.findByUsername("User.Name")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> traineeService.getProfile(VALID_CREDENTIALS, "User.Name"));
    }

    @Test
    void getProfile_wrongCallerCredentials_doesNotReadProfile() {
        doThrow(new AuthenticationException()).when(userAuthenticator).authenticate(WRONG_PASSWORD_CREDENTIALS);

        assertThrows(AuthenticationException.class,
                () -> traineeService.getProfile(WRONG_PASSWORD_CREDENTIALS, "Mary.Jones"));

        verify(traineeDao, never()).findByUsername(any());
    }

// ---------- update ----------

    @Test
    void update_changesOnlyAllowedFields() {
        Trainee trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        Trainee newData = Trainee.builder()
                .dateOfBirth(LocalDate.of(2000, 5, 5))
                .address("9 New Road")
                .user(User.builder()
                        .firstName("Johnny")
                        .lastName("Smithers")
                        .username("Someone.Else")      // must be ignored
                        .password("hacked")            // must be ignored
                        .isActive(false)               // must be ignored
                        .build())
                .build();

        Trainee result = traineeService.update(VALID_CREDENTIALS, newData);

        assertSame(trainee, result);
        assertEquals("Johnny", trainee.getUser().getFirstName());
        assertEquals("Smithers", trainee.getUser().getLastName());
        assertEquals(LocalDate.of(2000, 5, 5), trainee.getDateOfBirth());
        assertEquals("9 New Road", trainee.getAddress());
        assertEquals(USERNAME, trainee.getUser().getUsername());
        assertEquals(CURRENT_PASSWORD, trainee.getUser().getPassword());
        assertTrue(trainee.getUser().isActive());
    }

    @Test
    void update_invalidData_isRejectedBeforeDatabaseAccess() {
        Trainee newData = Trainee.builder().build();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(newData);

        assertThrows(ConstraintViolationException.class, () -> traineeService.update(VALID_CREDENTIALS, newData));

        verify(traineeDao, never()).findByUsername(any());
    }

// ---------- delete ----------

    @Test
    void delete_rightCredentials_deletesTrainee() {
        Trainee trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        traineeService.delete(VALID_CREDENTIALS);

        verify(traineeDao).delete(trainee);
    }

    @Test
    void delete_wrongPassword_deletesNothing() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));

        assertThrows(AuthenticationException.class, () -> traineeService.delete(WRONG_PASSWORD_CREDENTIALS));

        verify(traineeDao, never()).delete(any());
    }

// ---------- getTrainings ----------

    @Test
    void getTrainings_passesUsernameAndCriteriaToDao() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));
        TraineeTrainingCriteria criteria = TraineeTrainingCriteria.builder().trainerName("Anna").build();
        List<Training> trainings = List.of(new Training());
        when(trainingDao.findTraineeTrainings(USERNAME, criteria)).thenReturn(trainings);

        assertSame(trainings, traineeService.getTrainings(VALID_CREDENTIALS, criteria));
    }

    @Test
    void getTrainings_nullCriteria_meansNoFilters() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));
        TraineeTrainingCriteria noFilters = TraineeTrainingCriteria.builder().build();
        when(trainingDao.findTraineeTrainings(USERNAME, noFilters)).thenReturn(List.of());

        assertTrue(traineeService.getTrainings(VALID_CREDENTIALS, null).isEmpty());
    }

    @Test
    void getTrainings_wrongPassword_doesNotSearch() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));

        assertThrows(AuthenticationException.class,
                () -> traineeService.getTrainings(WRONG_PASSWORD_CREDENTIALS, null));

        verify(trainingDao, never()).findTraineeTrainings(any(), any());
    }

// ---------- getUnassignedTrainers ----------

    @Test
    void getUnassignedTrainers_returnsDaoResult() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));
        List<Trainer> trainers = List.of(trainerWithUsername("Mark.Hill"));
        when(trainerDao.findNotAssignedToTrainee(USERNAME)).thenReturn(trainers);

        assertSame(trainers, traineeService.getUnassignedTrainers(VALID_CREDENTIALS));
    }

// ---------- updateTrainers ----------

    @Test
    void updateTrainers_replacesWholeList() {
        Trainee trainee = existingTrainee(true);
        trainee.getTrainers().add(trainerWithUsername("Old.Trainer"));
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        Trainer anna = trainerWithUsername("Anna.Lee");
        Trainer mark = trainerWithUsername("Mark.Hill");
        when(trainerDao.findByUsernames(Set.of("Anna.Lee", "Mark.Hill"))).thenReturn(List.of(anna, mark));

        // Anna.Lee twice: duplicates are ignored
        traineeService.updateTrainers(VALID_CREDENTIALS, List.of("Anna.Lee", "Mark.Hill", "Anna.Lee"));

        assertEquals(Set.of(anna, mark), trainee.getTrainers());
    }

    @Test
    void updateTrainers_unknownUsername_throwsAndKeepsOldList() {
        Trainee trainee = existingTrainee(true);
        Trainer oldTrainer = trainerWithUsername("Old.Trainer");
        trainee.getTrainers().add(oldTrainer);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsernames(Set.of("Anna.Lee", "No.Body")))
                .thenReturn(List.of(trainerWithUsername("Anna.Lee")));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainers(VALID_CREDENTIALS, List.of("Anna.Lee", "No.Body")));

        assertTrue(exception.getMessage().contains("No.Body"));
        assertEquals(Set.of(oldTrainer), trainee.getTrainers());
    }

    private static Trainee traineeWithUsername(String username) {
        return Trainee.builder().user(User.builder().username(username).build()).build();
    }

    private static Trainer trainerWithUsername(String username) {
        return Trainer.builder().user(User.builder().username(username).build()).build();
    }
}
