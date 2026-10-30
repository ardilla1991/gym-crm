package com.epam.training.gym.api;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.config.AppConfig;
import com.epam.training.gym.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            log.info("--- Data loaded from files on startup ---");
            facade.findAllTrainees().forEach(trainee -> log.info("{}", trainee));
            facade.findAllTrainers().forEach(trainer -> log.info("{}", trainer));
            facade.findAllTrainings().forEach(training -> log.info("{}", training));

            log.info("--- Create a trainee ---");
            Trainee trainee = facade.createTrainee(Trainee.builder()
                    .firstName("Emily")
                    .lastName("Johnson")
                    .dateOfBirth(LocalDate.of(2001, 6, 21))
                    .address("789 Pine St, Denver, CO 80202")
                    .build());
            log.info("Created: {}", trainee);

            log.info("--- Create a trainee whose name already exists (expect username Emily.Johnson1) ---");
            Trainee trainee1 = facade.createTrainee(Trainee.builder()
                    .firstName("Emily")
                    .lastName("Johnson")
                    .dateOfBirth(LocalDate.of(1997, 2, 3))
                    .address("789 Pine St, Denver, CO 80202")
                    .build());
            log.info("Created: {}", trainee1);

            log.info("--- Create a trainer ---");
            Trainer trainer = facade.createTrainer(Trainer.builder()
                    .firstName("Chris")
                    .lastName("Taylor")
                    .specialization(new TrainingType(3L, TrainingTypeName.ZUMBA))
                    .build());
            log.info("Created: {}", trainer);

            log.info("--- Update the trainee's address ---");
            trainee.setAddress("321 Maple Dr, Boulder, CO 80302");
            facade.updateTrainee(trainee);
            log.info("Found after update: {}", facade.findTrainee(trainee.getId()));

            log.info("--- Create a training ---");
            Training training = facade.createTraining(Training.builder()
                    .traineeId(trainee.getId())
                    .trainerId(trainer.getId())
                    .trainingName("Evening zumba")
                    .trainingType(trainer.getSpecialization())
                    .trainingDate(LocalDate.of(2026, 11, 10))
                    .trainingDuration(50)
                    .build());
            log.info("Created: {}", training);

            log.info("--- Delete the trainee ---");
            facade.deleteTrainee(trainee.getId());
            log.info("Trainees left: {}", facade.findAllTrainees().size());
        } catch (Exception e) {
            log.error("Application failed", e);
        }
    }
}
