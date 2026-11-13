package com.epam.training.gym.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

/**
 * Training type: a reference record with an id and a name
 * from the fixed list in {@link TrainingTypeName}.
 */
@Entity
@Table(name = "training_type")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TrainingType {

    /** Not generated: the ids are fixed in db/data.sql. */
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type_name", nullable = false, unique = true)
    private TrainingTypeName trainingTypeName;
}
