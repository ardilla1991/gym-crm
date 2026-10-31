# Gym CRM

A Spring-based module of a gym CRM system: trainees, trainers and trainings stored in an in-memory storage.
Plain Spring Framework (no Spring Boot), Java-based configuration.

## Requirements

- JDK 21
- Maven 3.9+ (or the Maven Wrapper from the repository: `mvnw` / `mvnw.cmd`)

## Modules

| Module        | Contents                                                                                   |
|---------------|--------------------------------------------------------------------------------------------|
| `domain`      | Entities: `User`, `Trainee`, `Trainer`, `Training`, `TrainingType`                          |
| `persistence` | DAOs, storage bean names, `StorageInitializer` (loads initial data from files at startup)  |
| `service`     | `TraineeService`, `TrainerService`, `TrainingService`, username and password generation    |
| `config`      | Spring configuration (`AppConfig`, `StorageConfig`), `application.properties`, data files  |
| `api`         | `GymFacade` (single entry point) and `Main` (runs a demo scenario)                          |

Dependencies go in one direction: `domain ← persistence ← service ← config ← api`.

## Build

From the project root:

```bash
mvn clean install
```

This compiles all modules, runs all unit tests and builds the coverage report.

## Run

**From IntelliJ IDEA:** open `api/src/main/java/.../Main.java` and run `main()`.

**From the command line** (after `mvn clean install`):

```bash
mvn -pl api exec:java
```

`Main` starts the Spring context, prints the data loaded from the files, then creates a trainee and a trainer,
updates the trainee, creates a training and deletes the trainee, logging every step.

## Configuration

Initial data is loaded on startup from JSON files; their paths are set in
`config/src/main/resources/application.properties`:

```properties
storage.init.trainee=classpath:data/trainees.json
storage.init.trainer=classpath:data/trainers.json
storage.init.training=classpath:data/trainings.json
storage.init.training-type=classpath:data/training-types.json
```

To use a file outside the project, use the `file:` prefix, e.g. `file:C:/gym/data/trainees.json`.

Logging is configured in `api/src/main/resources/simplelogger.properties`
(default level `INFO`; uncomment the line there to see `DEBUG` messages of the application).

## Username and password rules

- **Username:** `FirstName.LastName`, e.g. `John.Smith`.
  If a trainee or trainer with this username already exists, the first free serial number is added:
  `John.Smith1`, `John.Smith2`, ...
- **Password:** random string of 10 characters.

## Tests and coverage

Run all tests:

```bash
mvn clean test
```

Coverage is measured by JaCoCo. After the tests, open the report for the whole project in a browser:

```
api/target/site/jacoco-aggregate/index.html
```

The **Total** row shows the coverage of all modules; the **Lines** column is the line coverage.
Click a module, package or class to see the source code: covered lines are green, uncovered are red.

Reports for single modules (only modules that have their own tests):

```
persistence/target/site/jacoco/index.html
service/target/site/jacoco/index.html
api/target/site/jacoco/index.html
```