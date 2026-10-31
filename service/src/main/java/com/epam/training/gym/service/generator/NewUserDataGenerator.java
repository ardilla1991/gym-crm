package com.epam.training.gym.service.generator;

import com.epam.training.gym.domain.User;
import com.epam.training.gym.persistence.dao.TraineeDao;
import com.epam.training.gym.persistence.dao.TrainerDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;


    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    /**
     * FirstName.LastName, e.g. John.Smith.
     * If a trainee or trainer with the same name already exists, a serial number is added:
     * John.Smith1, John.Smith2, ...
     */
    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName + USERNAME_SEPARATOR + lastName;

        Set<String> takenUsernames = new HashSet<>();
        for (User user : allUsers()) {
            takenUsernames.add(user.getUsername());
        }

        if (!takenUsernames.contains(baseUsername)) {
            return baseUsername;
        }

        int suffix = 1;
        while (takenUsernames.contains(baseUsername + suffix)) {
            suffix++;
        }

        return baseUsername + suffix;
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

    /**  The biggest one Usser id + 1. */
    public Long nextUserId() {
        long max = 0;
        for (User user : allUsers()) {
            if (user.getUserId() != null && user.getUserId() > max) {
                max = user.getUserId();
            }
        }

        return max + 1;
    }

    /** Fills the generated part of a new profile: userId, username and password.
     * A new profile is active. */
    public void fillNewUserData(User user) {
        user.setUserId(nextUserId());
        user.setUsername(generateUsername(user.getFirstName(), user.getLastName()));
        user.setPassword(generatePassword());
        user.setActive(true);
    }

    private List<User> allUsers() {
        List<User> users = new ArrayList<>();
        users.addAll(traineeDao.findAll());
        users.addAll(trainerDao.findAll());

        return users;
    }


}
