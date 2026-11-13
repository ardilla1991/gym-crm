package com.epam.training.gym.api;

import com.epam.training.gym.api.facade.GymFacade;
import com.epam.training.gym.config.AppConfig;
import com.epam.training.gym.model.*;
import com.epam.training.gym.model.auth.Credentials;
import com.epam.training.gym.model.criteria.TraineeTrainingCriteria;
import com.epam.training.gym.model.criteria.TrainerTrainingCriteria;
import com.epam.training.gym.service.exception.AuthenticationException;
import com.epam.training.gym.service.exception.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

/**
 * Runs a demo scenario through the facade: every operation of the task, logged step by step.
 * Passwords are never logged.
 */
public final class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /** A trainer from config/src/main/resources/db/data.sql. */
    private static final Credentials DAVID = new Credentials("David.Miller", "Xk3mP9qL2w");
    private static final String JESSICA_USERNAME = "Jessica.Wilson";

    private Main() {
    }

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

            Credentials emily = createTrainee(facade);
            Credentials chris = createTrainer(facade);
            showCredentialsCheck(facade, emily);
            emily = showAccountChanges(facade, emily, chris);
            showProfileUpdates(facade, emily, chris);
            showTrainings(facade, emily, chris);
            showTrainersList(facade, emily);
            showRejectedRequests(facade, emily);
            showDelete(facade, emily);
        }
    }

    // ---------- items 1, 2 ----------

    private static Credentials createTrainee(GymFacade facade) {
        log.info("=== Create trainee ===");
        Trainee trainee = facade.createTrainee(Trainee.builder()
                .dateOfBirth(LocalDate.of(1998, 6, 21))
                .address("7 Elm Street, Denver")
                .user(User.builder().firstName("Emily").lastName("Johnson").build())
                .build());
        // Emily.Johnson already exists in data.sql, so the serial number is added
        log.info("Created trainee {}", trainee.getUser().getUsername());
        return credentialsOf(trainee.getUser());
    }

    private static Credentials createTrainer(GymFacade facade) {
        log.info("=== Create trainer ===");
        Trainer trainer = facade.createTrainer(Trainer.builder()
                .specialization(new TrainingType(null, TrainingTypeName.ZUMBA))
                .user(User.builder().firstName("Chris").lastName("Taylor").build())
                .build());
        log.info("Created trainer {} ({})",
                trainer.getUser().getUsername(), trainer.getSpecialization().getTrainingTypeName());
        return credentialsOf(trainer.getUser());
    }

    // ---------- items 3, 4 ----------

    private static void showCredentialsCheck(GymFacade facade, Credentials emily) {
        log.info("=== Username and password matching ===");
        log.info("Trainee, right password: {}", facade.matchTraineeCredentials(emily));
        log.info("Trainee, wrong password: {}",
                facade.matchTraineeCredentials(new Credentials(emily.username(), "wrong")));
        log.info("Trainer: {}", facade.matchTrainerCredentials(DAVID));
        // A trainee is not a trainer: the same credentials do not match as a trainer
        log.info("Trainee's credentials as a trainer: {}", facade.matchTrainerCredentials(emily));
    }

    // ---------- items 7, 8, 11, 12 ----------

    /** @return Emily's credentials with the new password */
    private static Credentials showAccountChanges(GymFacade facade, Credentials emily, Credentials chris) {
        log.info("=== Password change ===");
        String newPassword = "newPass123";
        facade.changeTraineePassword(emily, newPassword);
        log.info("Old password still works: {}", facade.matchTraineeCredentials(emily));
        Credentials emilyNew = new Credentials(emily.username(), newPassword);
        log.info("New password works: {}", facade.matchTraineeCredentials(emilyNew));

        log.info("=== Activate / deactivate (not idempotent) ===");
        log.info("Trainer active after first toggle: {}", facade.toggleTrainerActive(chris));
        log.info("Trainer active after second toggle: {}", facade.toggleTrainerActive(chris));
        return emilyNew;
    }

    // ---------- items 5, 6, 9, 10 ----------

    private static void showProfileUpdates(GymFacade facade, Credentials emily, Credentials chris) {
        log.info("=== Read profiles by username ===");
        Trainer david = facade.getTrainerProfile(emily, DAVID.username());
        log.info("Trainee {} reads trainer {} {}, specialization {}", emily.username(),
                david.getUser().getFirstName(), david.getUser().getLastName(),
                david.getSpecialization().getTrainingTypeName());

        log.info("=== Update profiles ===");
        Trainee updatedTrainee = facade.updateTrainee(emily, Trainee.builder()
                .dateOfBirth(LocalDate.of(1998, 6, 21))
                .address("12 Pine Road, Denver")
                .user(User.builder().firstName("Emily").lastName("Brown").build())
                .build());
        log.info("Trainee renamed to {} {}, username is still {}",
                updatedTrainee.getUser().getFirstName(), updatedTrainee.getUser().getLastName(),
                updatedTrainee.getUser().getUsername());

        Trainer updatedTrainer = facade.updateTrainer(chris, Trainer.builder()
                .user(User.builder().firstName("Christopher").lastName("Taylor").build())
                .build());
        log.info("Trainer renamed to {}, specialization is still {}",
                updatedTrainer.getUser().getFirstName(), updatedTrainer.getSpecialization().getTrainingTypeName());
    }

    // ---------- items 14, 15, 16 ----------

    private static void showTrainings(GymFacade facade, Credentials emily, Credentials chris) {
        log.info("=== Add trainings ===");
        facade.addTraining(emily, DAVID.username(),
                training("Morning yoga", TrainingTypeName.YOGA, LocalDate.of(2026, 10, 1), 60));
        facade.addTraining(emily, chris.username(),
                training("Evening zumba", TrainingTypeName.ZUMBA, LocalDate.of(2026, 10, 3), 45));

        log.info("=== Trainee trainings ===");
        logTrainings(facade.getTraineeTrainings(emily, null));
        log.info("Only YOGA:");
        logTrainings(facade.getTraineeTrainings(emily,
                TraineeTrainingCriteria.builder().trainingType(TrainingTypeName.YOGA).build()));

        log.info("=== Trainer trainings ===");
        logTrainings(facade.getTrainerTrainings(DAVID,
                TrainerTrainingCriteria.builder().fromDate(LocalDate.of(2026, 10, 1)).build()));
    }

    // ---------- items 6, 17, 18 ----------

    private static void showTrainersList(GymFacade facade, Credentials emily) {
        log.info("=== Trainee's trainers ===");
        // Both trainers were added to the list automatically by addTraining
        log.info("Assigned: {}", trainerNames(facade.getTraineeProfile(emily, emily.username())
                .getTrainers().stream().toList()));
        log.info("Not assigned: {}", trainerNames(facade.getUnassignedTrainers(emily)));

        log.info("=== Update trainee's trainers list ===");
        facade.updateTraineeTrainers(emily, List.of(JESSICA_USERNAME));
        log.info("Assigned now: {}", trainerNames(facade.getTraineeProfile(emily, emily.username())
                .getTrainers().stream().toList()));
    }

    // ---------- note 2 and note 3: rejected requests ----------

    private static void showRejectedRequests(GymFacade facade, Credentials emily) {
        log.info("=== Rejected requests ===");
        try {
            facade.changeTraineePassword(new Credentials(emily.username(), "wrong"), "hacked1234");
        } catch (AuthenticationException e) {
            log.info("Wrong password: {}", e.getMessage());
        }
        try {
            facade.addTraining(emily, "No.Such.Trainer",
                    training("Boxing", TrainingTypeName.FITNESS, LocalDate.of(2026, 10, 5), 30));
        } catch (EntityNotFoundException e) {
            log.info("Unknown trainer: {}", e.getMessage());
        }
        try {
            facade.createTrainee(Trainee.builder()
                    .user(User.builder().firstName(" ").lastName("Doe").build())
                    .build());
        } catch (ConstraintViolationException e) {
            log.info("Blank first name: {}", e.getMessage());
        }
    }

    // ---------- item 13 ----------

    private static void showDelete(GymFacade facade, Credentials emily) {
        log.info("=== Delete trainee ===");
        facade.deleteTrainee(emily);
        log.info("Deleted trainee can still log in: {}", facade.matchTraineeCredentials(emily));
        log.info("David's trainings after the delete: {}",
                facade.getTrainerTrainings(DAVID, null).size());
    }

    // ---------- helpers ----------

    private static Credentials credentialsOf(User user) {
        return new Credentials(user.getUsername(), user.getPassword());
    }

    private static Training training(String name, TrainingTypeName type, LocalDate date, int minutes) {
        return Training.builder()
                .trainingName(name)
                .trainingType(new TrainingType(null, type))
                .trainingDate(date)
                .trainingDuration(minutes)
                .build();
    }

    private static void logTrainings(List<Training> trainings) {
        trainings.forEach(training -> log.info("  {} {}: {} with {}, {} min",
                training.getTrainingDate(), training.getTrainingType().getTrainingTypeName(),
                training.getTrainingName(), training.getTrainer().getUser().getUsername(),
                training.getTrainingDuration()));
    }

    private static List<String> trainerNames(List<Trainer> trainers) {
        return trainers.stream().map(trainer -> trainer.getUser().getUsername()).sorted().toList();
    }

}
