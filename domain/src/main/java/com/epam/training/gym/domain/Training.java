package com.epam.training.gym.domain;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Training {
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private Date  trainingDate;
    private int trainingDuration;

}
