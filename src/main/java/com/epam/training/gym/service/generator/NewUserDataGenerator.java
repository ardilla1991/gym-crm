package com.epam.training.gym.service.generator;

import com.epam.training.gym.model.User;
import com.epam.training.gym.persistence.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

/**
 * Calculates the data of a new Trainee/Trainer profile: username, password and userId.
 * Trainees and trainers share one "users" space, so it looks at both of them.
 */
@Component
public class NewUserDataGenerator {
    private static final String PASSWORD_CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@";
    private static final int PASSWORD_LENGTH = 10;
    private static final String USERNAME_SEPARATOR = ".";

    /** SecureRandom instead of Random: passwords must not be predictable. */
    private final SecureRandom random = new SecureRandom();

    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * FirstName.LastName, e.g. John.Smith.
     * If a trainee or trainer with the same name already exists, a serial number is added:
     * John.Smith1, John.Smith2, ...
     */
    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + USERNAME_SEPARATOR + lastName;

        Set<String> taken = new HashSet<>(userDao.findUsernamesStartingWith(baseUsername));
        if (!taken.contains(baseUsername)) {
            return baseUsername;
        }
        int number = 1;
        while (taken.contains(baseUsername + number)) {
            number++;
        }
        return baseUsername + number;
    }

    /** Random string of 10 characters. */
    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(PASSWORD_CHARS.length());
            password.append(PASSWORD_CHARS.charAt(index));
        }
        return password.toString();
    }

    /** Sets a unique username, a random password and the active status. */
    public void fillNewUserData(User user) {
        user.setUsername(generateUsername(user.getFirstName(), user.getLastName()));
        user.setPassword(generatePassword());
        user.setActive(true);
    }

}
