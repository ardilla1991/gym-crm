package com.epam.training.gym.domain;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Trainer extends User {
    private Long id;
    private TrainingType specialization;
}
