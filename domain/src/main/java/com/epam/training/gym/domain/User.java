package com.epam.training.gym.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString
public abstract class User {
    private Long userId;
    private String firstName;
    private String lastName;
    private String username;
    @ToString.Exclude
    private String password;
    private boolean isActive;
}
