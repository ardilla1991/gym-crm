package com.epam.training.gym.service.impl;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.User;
import com.epam.training.gym.domain.auth.Credentials;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import com.epam.training.gym.service.generator.NewUserDataGenerator;
import com.epam.training.gym.service.validation.EntityValidator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void changePassword_blankNewPassword_throwsWithoutDatabaseAccess() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.changePassword(VALID_CREDENTIALS, " "));

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

/*
    @Test
    void update_keepsGeneratedDataOfStoredTrainee() {
        Trainee stored = Trainee.builder()
                .id(1L).userId(5L).username("Emily.Johnson").password("Secret1234").firstName("Emily")
                .build();
        Trainee changes = Trainee.builder().id(1L).firstName("Emma").build();
        when(traineeDao.findById(1L)).thenReturn(Optional.of(stored));
        when(traineeDao.update(changes)).thenReturn(changes);

        Trainee updated = traineeService.update(changes);

        assertEquals("Emma", updated.getFirstName());
        assertEquals(5L, updated.getUserId());
        assertEquals("Emily.Johnson", updated.getUsername());
        assertEquals("Secret1234", updated.getPassword());
    }

    @Test
    void update_unknownId_throwsExceptionAndDoesNotSave() {
        Trainee unknown = Trainee.builder().id(99L).build();
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> traineeService.update(unknown));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void update_withoutId_throwsException() {
        Trainee withoutId = Trainee.builder().firstName("Emily").build();

        assertThrows(NullPointerException.class, () -> traineeService.update(withoutId));
    }

    @Test
    void delete_existingTrainee_deletesIt() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(Trainee.builder().id(1L).build()));

        traineeService.delete(1L);

        verify(traineeDao).delete(1L);
    }

    @Test
    void delete_unknownTrainee_doesNothing() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        traineeService.delete(99L);

        verify(traineeDao, never()).delete(anyLong());
    }

    @Test
    void findById_existingId_returnsTrainee() {
        Trainee emily = Trainee.builder().id(1L).build();
        when(traineeDao.findById(1L)).thenReturn(Optional.of(emily));

        assertSame(emily, traineeService.findById(1L));
    }

    @Test
    void findById_unknownId_throwsException() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> traineeService.findById(99L));
    }

    @Test
    void findAll_returnsTraineesFromDao() {
        List<Trainee> trainees = List.of(Trainee.builder().id(1L).build());
        when(traineeDao.findAll()).thenReturn(trainees);

        assertEquals(trainees, traineeService.findAll());
    }

 */
}
