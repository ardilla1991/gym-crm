package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.model.User;

import java.util.List;

public interface UserDao {

    /** Usernames starting with the prefix: for "John.Smith" returns John.Smith, John.Smith1, ... */
    List<String> findUsernamesStartingWith(String prefix);

    /** True if a user with this username and password exists; passwords never leave the persistence layer. */
    boolean matchesPassword(String username, String password);

    void updatePassword(User user, String newPassword);

    /** Saves the user's isActive value. */
    void updateActive(User user);
}
