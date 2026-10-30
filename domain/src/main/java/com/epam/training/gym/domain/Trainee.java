package com.epam.training.gym.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Trainee extends User {
    private Long id;
    private LocalDate dateOfBirth;
    private String address;
}
