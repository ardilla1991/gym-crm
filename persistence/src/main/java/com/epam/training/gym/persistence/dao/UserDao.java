package com.epam.training.gym.persistence.dao;

import com.epam.training.gym.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    /** Usernames starting with the prefix: for "John.Smith" returns John.Smith, John.Smith1, ... */
    List<String> findUsernamesStartingWith(String prefix);

    Optional<User> findByUsername(String username);
}
