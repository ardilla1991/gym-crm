package com.epam.training.gym.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Training type: an id and a name from the fixed list in {@link TrainingTypeName}.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TrainingType {

    private Long id;

    private TrainingTypeName trainingTypeName;
}