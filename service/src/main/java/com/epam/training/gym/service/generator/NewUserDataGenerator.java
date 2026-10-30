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

@Component
public class NewUserDataGenerator {
    private static final String PASSWORD_CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@";
    private static final int PASSWORD_LENGTH = 10;
    private static final String USERNAME_SEPARATOR = ".";

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
     * Calculate Username from Trainer/Trainee first name and last name
     * by concatenation by using a separator (eg. John.Smith).
     * In the case that already exists Trainer or Trainee with the same pair of first and
     * last name as a suffix to the username should be added a serial number.
     * @param firstName first name
     * @param lastName last name
     * @return generated Username
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

    /** Random 10 chars length string */
    public String generatePassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(PASSWORD_CHARS.length());
            password.append(PASSWORD_CHARS.charAt(index));
        }
        return password.toString();
    }

    public Long nextUserId() {
        long max = 0;
        for (User user : allUsers()) {
            if (user.getUserId() != null && user.getUserId() > max) {
                max = user.getUserId();
            }
        }

        return max + 1;
    }

    private List<User> allUsers() {
        List<User> users = new ArrayList<>();
        users.addAll(traineeDao.findAll());
        users.addAll(trainerDao.findAll());

        return users;
    }


}
