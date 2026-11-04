package com.epam.training.gym.persistence.dao;

import java.util.List;

public interface UserDao {
    /** Usernames starting with the prefix: for "John.Smith" returns John.Smith, John.Smith1, ... */
    List<String> findUsernamesStartingWith(String prefix);
}
