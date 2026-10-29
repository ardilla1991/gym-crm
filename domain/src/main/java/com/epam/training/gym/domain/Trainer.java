package com.epam.training.gym.domain;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Trainer extends User {
    private Long id;
    private String specialization;
}
