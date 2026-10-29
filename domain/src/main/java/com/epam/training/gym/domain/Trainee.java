package com.epam.training.gym.domain;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Trainee extends User {
    private Long id;
    private Date dateOfBirth;
    private String address;
}
