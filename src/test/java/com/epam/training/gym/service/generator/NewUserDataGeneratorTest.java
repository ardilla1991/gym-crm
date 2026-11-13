package com.epam.training.gym.service.generator;

import com.epam.training.gym.domain.Trainee;
import com.epam.training.gym.domain.Trainer;
import com.epam.training.gym.domain.User;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import com.epam.training.gym.persistence.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewUserDataGeneratorTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private NewUserDataGenerator generator;

    @Test
    void generateUsername_nameIsFree_returnsFirstNameDotLastName() {
        when(userDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());

        assertEquals("John.Smith", generator.generateUsername("John", "Smith"));
    }

    @Test
    void generateUsername_taken_addsNumberOne() {
        when(userDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of("John.Smith"));

        assertEquals("John.Smith1", generator.generateUsername("John", "Smith"));
    }

    @Test
    void generateUsername_severalTaken_addsNextNumber() {
        when(userDao.findUsernamesStartingWith("John.Smith"))
                .thenReturn(List.of("John.Smith", "John.Smith1", "John.Smith2"));

        assertEquals("John.Smith3", generator.generateUsername("John", "Smith"));
    }

    @Test
    void generateUsername_freedNumber_isReused() {
        when(userDao.findUsernamesStartingWith("John.Smith"))
                .thenReturn(List.of("John.Smith", "John.Smith2"));

        assertEquals("John.Smith1", generator.generateUsername("John", "Smith"));
    }

    @Test
    void generateUsername_longerNameWithSamePrefix_doesNotCount() {
        // LIKE 'John.Smith%' also returns John.Smithson; it must not affect the result
        when(userDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of("John.Smithson"));

        assertEquals("John.Smith", generator.generateUsername("John", "Smith"));
    }

    @Test
    void generatePassword_hasTenCharacters() {
        assertEquals(10, generator.generatePassword().length());
    }

    @Test
    void fillNewUserData_setsUsernamePasswordAndActiveStatus() {
        when(userDao.findUsernamesStartingWith("John.Smith")).thenReturn(List.of());
        User user = User.builder().firstName("John").lastName("Smith").build();

        generator.fillNewUserData(user);

        assertEquals("John.Smith", user.getUsername());
        assertNotNull(user.getPassword());
        assertTrue(user.isActive());
    }

}
