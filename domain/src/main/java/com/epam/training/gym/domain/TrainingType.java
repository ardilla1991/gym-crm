package com.epam.training.gym.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingType {

    private Long id;
    private TrainingTypeName trainingTypeName;
}
