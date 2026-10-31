package com.epam.training.gym.domain;

import lombok.*;

/**
 * Training type: a reference record with an id and a name
 * from the fixed list in {@link TrainingTypeName}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingType {

    private Long id;
    private TrainingTypeName trainingTypeName;
}
