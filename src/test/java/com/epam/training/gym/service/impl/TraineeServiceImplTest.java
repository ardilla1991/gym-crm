package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.*;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.entity.*;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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
    private static final String NEW_PASSWORD = "newPass123";

    private static final Credentials VALID_CREDENTIALS = new Credentials(USERNAME, CURRENT_PASSWORD);
    private static final Credentials WRONG_PASSWORD_CREDENTIALS = new Credentials(USERNAME, "wrong");

    private static final TrainingTypeEntity YOGA = new TrainingTypeEntity(2L, TrainingTypeName.YOGA);

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
        when(traineeDao.save(any(TraineeEntity.class))).thenAnswer(invocation -> {
            TraineeEntity entity = invocation.getArgument(0);
            entity.setId(1L);                       // as the database would do
            return entity;
        });

        Trainee result = traineeService.create(trainee);

        // The caller gets a model built from the saved entity
        assertEquals(1L, result.getId());
        assertEquals("John", result.getUser().getFirstName());
        InOrder order = inOrder(entityValidator, newUserDataGenerator, traineeDao);
        order.verify(entityValidator).validate(trainee);
        order.verify(newUserDataGenerator).fillNewUserData(trainee.getUser());
        order.verify(traineeDao).save(any(TraineeEntity.class));
    }

    @Test
    void create_invalidTrainee_isNotSaved() {
        Trainee trainee = newTrainee();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(trainee);

        assertThrows(ConstraintViolationException.class, () -> traineeService.create(trainee));

        verify(newUserDataGenerator, never()).fillNewUserData(any());
        verify(traineeDao, never()).save(any());
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
        TraineeEntity trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        traineeService.changePassword(VALID_CREDENTIALS, NEW_PASSWORD);

        assertEquals(NEW_PASSWORD, trainee.getUser().getPassword());
    }

    @Test
    void changePassword_wrongPassword_throwsAndKeepsOldPassword() {
        TraineeEntity trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class,
                () -> traineeService.changePassword(WRONG_PASSWORD_CREDENTIALS, NEW_PASSWORD));

        assertEquals(CURRENT_PASSWORD, trainee.getUser().getPassword());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void changePassword_blankNewPassword_throwsAndKeepsOldPassword(String newPassword) {
        TraineeEntity trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(IllegalArgumentException.class, () -> traineeService.changePassword(VALID_CREDENTIALS, newPassword));

        assertEquals(CURRENT_PASSWORD, trainee.getUser().getPassword());
    }

    @Test
    void changePassword_wrongPassword_isRejectedBeforeCheckingNewPassword() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));

        assertThrows(AuthenticationException.class,
                () -> traineeService.changePassword(WRONG_PASSWORD_CREDENTIALS, " "));
    }

    // ---------- toggleActive ----------

    @Test
    void toggleActive_activeTrainee_becomesInactive() {
        TraineeEntity trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertFalse(traineeService.toggleActive(VALID_CREDENTIALS));
        assertFalse(trainee.getUser().isActive());
    }

    @Test
    void toggleActive_calledTwice_returnsToOriginalStatus() {
        TraineeEntity trainee = existingTrainee(false);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.toggleActive(VALID_CREDENTIALS));      // inactive -> active
        assertFalse(traineeService.toggleActive(VALID_CREDENTIALS));     // active -> inactive: not idempotent
    }

    @Test
    void toggleActive_wrongPassword_throwsAndKeepsStatus() {
        TraineeEntity trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(AuthenticationException.class, () -> traineeService.toggleActive(WRONG_PASSWORD_CREDENTIALS));

        assertTrue(trainee.getUser().isActive());
    }

    /** A trainee row as if loaded from the database: with username and password. */
    private static TraineeEntity existingTrainee(boolean active) {
        return TraineeEntity.builder()
                .id(1L)
                .user(UserEntity.builder()
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
        TraineeEntity trainee = existingTrainee(true);
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

        // The managed entity got only the allowed fields
        assertEquals("Johnny", trainee.getUser().getFirstName());
        assertEquals("Smithers", trainee.getUser().getLastName());
        assertEquals(LocalDate.of(2000, 5, 5), trainee.getDateOfBirth());
        assertEquals("9 New Road", trainee.getAddress());
        assertEquals(USERNAME, trainee.getUser().getUsername());
        assertEquals(CURRENT_PASSWORD, trainee.getUser().getPassword());
        assertTrue(trainee.getUser().isActive());
        // The caller gets the updated profile as a model
        assertEquals("Johnny", result.getUser().getFirstName());
        assertEquals(USERNAME, result.getUser().getUsername());
    }

    @Test
    void update_wrongPassword_isRejectedBeforeValidation() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));

        assertThrows(AuthenticationException.class,
                () -> traineeService.update(WRONG_PASSWORD_CREDENTIALS, Trainee.builder().build()));

        verify(entityValidator, never()).validate(any());
    }

    @Test
    void update_invalidData_isRejectedAndChangesNothing() {
        TraineeEntity trainee = existingTrainee(true);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        Trainee newData = Trainee.builder().build();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(newData);

        assertThrows(ConstraintViolationException.class, () -> traineeService.update(VALID_CREDENTIALS, newData));

        assertEquals("John", trainee.getUser().getFirstName());
    }

    // ---------- delete ----------

    @Test
    void delete_rightCredentials_deletesTrainee() {
        TraineeEntity trainee = existingTrainee(true);
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
        when(trainingDao.findTraineeTrainings(USERNAME, criteria)).thenReturn(List.of(trainingRow("Morning yoga")));

        List<Training> result = traineeService.getTrainings(VALID_CREDENTIALS, criteria);

        assertEquals(List.of("Morning yoga"), result.stream().map(Training::getTrainingName).toList());
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
    void getUnassignedTrainers_returnsDaoResultAsModels() {
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(existingTrainee(true)));
        when(trainerDao.findNotAssignedToTrainee(USERNAME)).thenReturn(List.of(trainerWithUsername("Mark.Hill")));

        List<Trainer> result = traineeService.getUnassignedTrainers(VALID_CREDENTIALS);

        assertEquals(List.of("Mark.Hill"), result.stream().map(trainer -> trainer.getUser().getUsername()).toList());
    }

    // ---------- updateTrainers ----------

    @Test
    void updateTrainers_replacesWholeList() {
        TraineeEntity trainee = existingTrainee(true);
        trainee.getTrainers().add(trainerWithUsername("Old.Trainer"));
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        TrainerEntity anna = trainerWithUsername("Anna.Lee");
        TrainerEntity mark = trainerWithUsername("Mark.Hill");
        when(trainerDao.findByUsernames(Set.of("Anna.Lee", "Mark.Hill"))).thenReturn(List.of(anna, mark));

        traineeService.updateTrainers(VALID_CREDENTIALS, List.of("Anna.Lee", "Mark.Hill"));

        assertEquals(Set.of(anna, mark), trainee.getTrainers());
    }

    @Test
    void updateTrainers_unknownUsername_throwsAndKeepsOldList() {
        TraineeEntity trainee = existingTrainee(true);
        TrainerEntity oldTrainer = trainerWithUsername("Old.Trainer");
        trainee.getTrainers().add(oldTrainer);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerDao.findByUsernames(Set.of("Anna.Lee", "No.Body")))
                .thenReturn(List.of(trainerWithUsername("Anna.Lee")));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> traineeService.updateTrainers(VALID_CREDENTIALS, List.of("Anna.Lee", "No.Body")));

        assertTrue(exception.getMessage().contains("No.Body"));
        assertEquals(Set.of(oldTrainer), trainee.getTrainers());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("listsWithBadUsernames")
    void updateTrainers_invalidList_throwsAndKeepsOldList(List<String> trainerUsernames) {
        TraineeEntity trainee = existingTrainee(true);
        TrainerEntity oldTrainer = trainerWithUsername("Old.Trainer");
        trainee.getTrainers().add(oldTrainer);
        when(traineeDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainee));

        assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateTrainers(VALID_CREDENTIALS, trainerUsernames));

        verify(trainerDao, never()).findByUsernames(any());
        assertEquals(Set.of(oldTrainer), trainee.getTrainers());
    }

    static Stream<List<String>> listsWithBadUsernames() {
        return Stream.of(
                List.of("Anna.Lee", " "),             // blank username
                Arrays.asList("Anna.Lee", null),      // null username
                List.of("Anna.Lee", "Anna.Lee"));     // repeated username
    }

    private static TraineeEntity traineeWithUsername(String username) {
        return TraineeEntity.builder().user(UserEntity.builder().username(username).build()).build();
    }

    /** A trainer row: the mapper needs the user and the specialization. */
    private static TrainerEntity trainerWithUsername(String username) {
        return TrainerEntity.builder()
                .user(UserEntity.builder().username(username).build())
                .specialization(YOGA)
                .build();
    }

    /** A complete training row, as the DAO loads it with all its links. */
    private static TrainingEntity trainingRow(String name) {
        return TrainingEntity.builder()
                .trainingName(name)
                .trainee(existingTrainee(true))
                .trainer(trainerWithUsername("Anna.Lee"))
                .trainingType(YOGA)
                .trainingDate(LocalDate.of(2026, 10, 5))
                .trainingDuration(60)
                .build();
    }
}
