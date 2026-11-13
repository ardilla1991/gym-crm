package com.epam.training.gym.persistence.entity;

import com.epam.training.gym.model.TrainingTypeName;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

/**
 * Database row of a training type (table "training_type"): a reference record
 * with an id and a name from the fixed list in {@link TrainingTypeName}.
 * Immutable: the application never changes this table.
 */
@Entity(name = "TrainingType")
@Table(name = "training_type")
@Immutable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TrainingTypeEntity {

    /** Not generated: the ids are fixed in db/data.sql. */
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "training_type_name", nullable = false, unique = true)
    private TrainingTypeName trainingTypeName;
}
