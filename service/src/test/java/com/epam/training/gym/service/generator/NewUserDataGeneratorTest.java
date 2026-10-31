package com.epam.training.gym.service.generator;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewUserDataGeneratorTest {
    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    private NewUserDataGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new NewUserDataGenerator();
        generator.setTraineeDao(traineeDao);
        generator.setTrainerDao(trainerDao);
    }

    @Test
    void generateUsername_nameIsFree_returnsFirstNameDotLastName() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals("Emily.Johnson", generator.generateUsername("Emily", "Johnson"));
    }

    @Test
    void generateUsername_takenByTrainee_addsSerialNumber() {
        when(traineeDao.findAll()).thenReturn(List.of(trainee("Emily.Johnson", 1L)));
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals("Emily.Johnson1", generator.generateUsername("Emily", "Johnson"));
    }

    @Test
    void generateUsername_takenByTrainer_addsSerialNumber() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of(trainer("Emily.Johnson", 1L)));

        assertEquals("Emily.Johnson1", generator.generateUsername("Emily", "Johnson"));
    }

    @Test
    void generateUsername_severalTaken_returnsNextFreeNumber() {
        when(traineeDao.findAll()).thenReturn(List.of(
                trainee("Emily.Johnson", 1L),
                trainee("Emily.Johnson1", 2L)));
        when(trainerDao.findAll()).thenReturn(List.of(trainer("Emily.Johnson2", 3L)));

        assertEquals("Emily.Johnson3", generator.generateUsername("Emily", "Johnson"));
    }

    @Test
    void generateUsername_numberFreedAfterDeletion_reusesIt() {
        when(traineeDao.findAll()).thenReturn(List.of(
                trainee("Emily.Johnson", 1L),
                trainee("Emily.Johnson2", 3L)));
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals("Emily.Johnson1", generator.generateUsername("Emily", "Johnson"));
    }

    @Test
    void generatePassword_hasTenLettersOrDigits() {
        String password = generator.generatePassword();

        assertTrue(password.matches("[A-Za-z0-9!@]{10}"), "unexpected password: " + password);
    }

    @Test
    void nextUserId_returnsMaxAmongTraineesAndTrainersPlusOne() {
        when(traineeDao.findAll()).thenReturn(List.of(trainee("a", 1L), trainee("b", 2L)));
        when(trainerDao.findAll()).thenReturn(List.of(trainer("c", 5L)));

        assertEquals(6L, generator.nextUserId());
    }

    @Test
    void nextUserId_noUsers_returnsOne() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals(1L, generator.nextUserId());
    }

    @Test
    void fillNewUserData_setsAllGeneratedFields() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of());
        Trainee emily = Trainee.builder().firstName("Emily").lastName("Johnson").build();

        generator.fillNewUserData(emily);

        assertEquals(1L, emily.getUserId());
        assertEquals("Emily.Johnson", emily.getUsername());
        assertEquals(10, emily.getPassword().length());
        assertTrue(emily.isActive());
    }

    private static Trainee trainee(String username, Long userId) {
        return Trainee.builder().username(username).userId(userId).build();
    }

    private static Trainer trainer(String username, Long userId) {
        return Trainer.builder().username(username).userId(userId).build();
    }

}
