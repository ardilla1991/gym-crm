package com.epam.training.gym.service.impl;

import com.epam.training.gym.model.*;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.TrainingDao;
import com.epam.training.gym.persistence.dao.TrainingTypeDao;
import com.epam.training.gym.persistence.dao.UserDao;
import com.epam.training.gym.service.auth.UserAuthenticator;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.exception.InvalidStateException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
public class TrainerServiceImplTest {

    private static final String USERNAME = "Anna.Lee";
    private static final String CURRENT_PASSWORD = "abcdef1234";

    private static final Credentials VALID_CREDENTIALS = new Credentials(USERNAME, CURRENT_PASSWORD);
    private static final Credentials WRONG_PASSWORD_CREDENTIALS = new Credentials(USERNAME, "wrong");

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private UserDao userDao;

    @Mock
    private UserAuthenticator userAuthenticator;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private EntityValidator entityValidator;

    @Mock
    private NewUserDataGenerator newUserDataGenerator;

    @InjectMocks
    private TrainerServiceImpl trainerService;

    // ---------- create ----------

    @Test
    void create_usesTrainingTypeFromDatabase() {
        TrainingType zumbaFromDb = new TrainingType(3L, TrainingTypeName.ZUMBA);
        Trainer trainer = newTrainer(TrainingTypeName.ZUMBA);
        Trainer saved = existingTrainer(true);
        when(trainingTypeDao.findByName(TrainingTypeName.ZUMBA)).thenReturn(Optional.of(zumbaFromDb));
        when(trainerDao.save(trainer)).thenReturn(saved);

        Trainer result = trainerService.create(trainer);

        // The DAO gets the type loaded from the database: with its id, not the one built by the caller
        assertSame(zumbaFromDb, trainer.getSpecialization());
        assertSame(saved, result);
        verify(entityValidator).validate(trainer);
        verify(newUserDataGenerator).fillNewUserData(trainer.getUser());
    }

    @Test
    void create_returnsGeneratedPassword() {
        Trainer trainer = newTrainer(TrainingTypeName.ZUMBA);
        when(trainingTypeDao.findByName(TrainingTypeName.ZUMBA))
                .thenReturn(Optional.of(new TrainingType(3L, TrainingTypeName.ZUMBA)));
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUsername("Anna.Lee");
            user.setPassword("Gen3rated1");
            return null;
        }).when(newUserDataGenerator).fillNewUserData(trainer.getUser());
        when(trainerDao.save(trainer)).thenReturn(existingTrainer(true));

        Trainer result = trainerService.create(trainer);

        // Registration is the only result with a password: the user needs it to sign in
        assertEquals("Gen3rated1", result.getUser().getPassword());
    }

    @Test
    void create_unknownTrainingType_isNotSaved() {
        Trainer trainer = newTrainer(TrainingTypeName.ZUMBA);
        when(trainingTypeDao.findByName(TrainingTypeName.ZUMBA)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> trainerService.create(trainer));

        verify(trainerDao, never()).save(any());
    }

    // ---------- matchCredentials ----------

    @Test
    void matchCredentials_rightPassword_returnsTrue() {
        signIn(true);

        assertTrue(trainerService.matchCredentials(VALID_CREDENTIALS));
    }

    @Test
    void matchCredentials_wrongPassword_returnsFalse() {
        assertFalse(trainerService.matchCredentials(WRONG_PASSWORD_CREDENTIALS));

        verify(trainerDao, never()).findByUsername(any());
    }

    @Test
    void matchCredentials_traineeCredentials_returnsFalse() {
        when(userAuthenticator.matches(VALID_CREDENTIALS)).thenReturn(true);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertFalse(trainerService.matchCredentials(VALID_CREDENTIALS));
    }

    // ---------- authenticate ----------

    @Test
    void authenticate_rightPassword_returnsOwnProfile() {
        Trainer trainer = signIn(true);

        assertSame(trainer, trainerService.authenticate(VALID_CREDENTIALS));
    }

    @Test
    void authenticate_wrongPassword_throws() {
        assertThrows(AuthenticationException.class, () -> trainerService.authenticate(WRONG_PASSWORD_CREDENTIALS));

        verify(trainerDao, never()).findByUsername(any());
    }

    // ---------- changeActiveStatus ----------

    @Test
    void changeActiveStatus_inactiveTrainer_activates() {
        Trainer trainer = signIn(false);

        trainerService.changeActiveStatus(VALID_CREDENTIALS, true);

        assertTrue(trainer.getUser().isActive());
        verify(userDao).updateActive(trainer.getUser());
    }

    @Test
    void changeActiveStatus_sameStatus_throwsAndKeepsStatus() {
        signIn(true);

        assertThrows(InvalidStateException.class, () -> trainerService.changeActiveStatus(VALID_CREDENTIALS, true));

        verify(userDao, never()).updateActive(any());
    }

    @Test
    void changeActiveStatus_wrongPassword_throwsAndKeepsStatus() {
        assertThrows(AuthenticationException.class,
                () -> trainerService.changeActiveStatus(WRONG_PASSWORD_CREDENTIALS, false));

        verify(userDao, never()).updateActive(any());
    }

    // ---------- helpers ----------

    /** The caller's credentials are right, and the DAO finds the caller's trainer profile. */
    private Trainer signIn(boolean active) {
        Trainer trainer = existingTrainer(active);
        when(userAuthenticator.matches(VALID_CREDENTIALS)).thenReturn(true);
        when(trainerDao.findByUsername(USERNAME)).thenReturn(Optional.of(trainer));
        return trainer;
    }

    /** A trainer as the DAO returns it: with ids and username, without password. */
    private static Trainer existingTrainer(boolean active) {
        return Trainer.builder()
                .id(1L)
                .specialization(new TrainingType(2L, TrainingTypeName.YOGA))
                .user(User.builder()
                        .id(1L)
                        .firstName("Anna")
                        .lastName("Lee")
                        .username(USERNAME)
                        .isActive(active)
                        .build())
                .build();
    }

    /** A trainer as a caller passes it: the specialization is built by hand, without id. */
    private static Trainer newTrainer(TrainingTypeName specialization) {
        return Trainer.builder()
                .specialization(new TrainingType(null, specialization))
                .user(User.builder().firstName("Anna").lastName("Lee").build())
                .build();
    }

    // ---------- getProfile ----------

    @Test
    void getProfile_signedInUser_readsTrainerProfile() {
        Credentials traineeCredentials = new Credentials("John.Smith", "abcdef1234");
        when(trainerDao.findByUsernameWithTrainees(USERNAME)).thenReturn(Optional.of(existingTrainer(true)));

        // A trainee reads a trainer's profile: any signed-in user may do it
        Trainer result = trainerService.getProfile(traineeCredentials, USERNAME);

        assertEquals(USERNAME, result.getUser().getUsername());
        verify(userAuthenticator).authenticate(traineeCredentials);
    }

    // ---------- update ----------

    @Test
    void update_changesNamesAndKeepsSpecialization() {
        Trainer trainer = signIn(true);
        TrainingType specialization = trainer.getSpecialization();
        when(trainerDao.findByUsernameWithTrainees(USERNAME)).thenReturn(Optional.of(trainer));
        Trainer newData = Trainer.builder()
                .specialization(new TrainingType(1L, TrainingTypeName.FITNESS))   // must be ignored
                .user(User.builder().firstName("Annie").lastName("Leigh").isActive(false).build())
                .build();

        Trainer result = trainerService.update(VALID_CREDENTIALS, newData);

        verify(trainerDao).update(trainer);
        assertEquals("Annie", trainer.getUser().getFirstName());
        assertEquals("Leigh", trainer.getUser().getLastName());
        assertFalse(trainer.getUser().isActive());
        assertSame(specialization, trainer.getSpecialization());
        assertEquals(USERNAME, trainer.getUser().getUsername());
        assertSame(trainer, result);
    }

    @Test
    void update_wrongPassword_isRejectedBeforeValidation() {
        Trainer newData = Trainer.builder().user(User.builder().build()).build();

        assertThrows(AuthenticationException.class, () -> trainerService.update(WRONG_PASSWORD_CREDENTIALS, newData));

        verify(entityValidator, never()).validate(any());
    }

    @Test
    void update_invalidNames_isRejectedAndChangesNothing() {
        Trainer trainer = signIn(true);
        when(trainerDao.findByUsernameWithTrainees(USERNAME)).thenReturn(Optional.of(trainer));
        Trainer newData = Trainer.builder().user(User.builder().build()).build();
        doThrow(new ConstraintViolationException(Set.of())).when(entityValidator).validate(newData.getUser());

        assertThrows(ConstraintViolationException.class, () -> trainerService.update(VALID_CREDENTIALS, newData));

        verify(trainerDao, never()).update(any());
    }

    // ---------- getTrainings ----------

    @Test
    void getTrainings_passesUsernameAndCriteriaToDao() {
        Trainer trainer = signIn(true);
        TrainerTrainingCriteria criteria = TrainerTrainingCriteria.builder().traineeName("John").build();
        when(trainingDao.findTrainerTrainings(USERNAME, criteria)).thenReturn(List.of(training(trainer)));

        List<Training> result = trainerService.getTrainings(VALID_CREDENTIALS, criteria);

        assertEquals(List.of("Morning yoga"), result.stream().map(Training::getTrainingName).toList());
    }

    @Test
    void getTrainings_nullCriteria_meansNoFilters() {
        signIn(true);
        when(trainingDao.findTrainerTrainings(USERNAME, TrainerTrainingCriteria.builder().build()))
                .thenReturn(List.of());

        assertTrue(trainerService.getTrainings(VALID_CREDENTIALS, null).isEmpty());
    }

    private static Training training(Trainer trainer) {
        return Training.builder()
                .trainingName("Morning yoga")
                .trainee(Trainee.builder().user(User.builder().username("John.Smith").build()).build())
                .trainer(trainer)
                .trainingType(trainer.getSpecialization())
                .trainingDate(LocalDate.of(2026, 10, 5))
                .trainingDuration(60)
                .build();
    }

}
