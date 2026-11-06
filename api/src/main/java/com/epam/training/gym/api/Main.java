package com.epam.training.gym.api;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.config.AppConfig;
import com.epam.training.gym.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

/**
 * Starts the Spring context and shows the gym CRM in action through the facade.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try{
            run();
        } catch (Exception e) {
            // the outermost catch logs the error; inner layers only throw
            log.error("Application failed", e);
        }
    }

    static void run() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            log.info("--- Create a trainee ---");
            Trainee trainee = facade.createTrainee(Trainee.builder()
                    .dateOfBirth(LocalDate.of(1998, 6, 21))
                    .address("7 Elm Street, Denver")
                    .user(User.builder().firstName("Kate").lastName("Johnson").build())
                    .build());
            log.info("Created trainee: {}", trainee.getUser().getUsername());

            log.info("--- Create a trainee whose name already exists (expect username Emily.Johnson1) ---");
            Trainee trainee1 = facade.createTrainee(Trainee.builder()
                    .dateOfBirth(LocalDate.of(1998, 6, 21))
                    .address("7 Elm Street, Denver")
                    .user(User.builder().firstName("Emily").lastName("Johnson").build())
                    .build());
            log.info("Created trainee: {}", trainee1.getUser().getUsername());

            log.info("--- Create a trainer ---");
            Trainer trainer = facade.createTrainer(Trainer.builder()
                    .specialization(new TrainingType(null, TrainingTypeName.ZUMBA))
                    .user(User.builder().firstName("Chris").lastName("Taylor").build())
                    .build());
            log.info("Created trainer: {}", trainer.getUser().getUsername());

            /*
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

             */
        }
    }

}
