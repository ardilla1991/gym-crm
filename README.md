# Gym CRM

Gym CRM on plain Spring Framework (no Spring Boot) with JPA and Hibernate.

## Layers

One Maven module; the layers are packages.

| Package       | What is there                                                        | Works with                         |
|---------------|----------------------------------------------------------------------|------------------------------------|
| `api`         | `GymFacade`, `Main`                                                  | models                             |
| `service`     | business logic, model validation; mappers in `service.mapper`        | models outside, entities inside    |
| `persistence` | DAOs; JPA entities in `persistence.entity`                           | entities                           |
| `model`       | models: Lombok and validation, no JPA                                | —                                  |
| `config`      | Spring and database configuration                                    | —                                  |

Requirements: JDK 21, Maven 3.9+. Optional: Docker or a local PostgreSQL.

## Run

The demo in `Main` calls every operation through `GymFacade` and logs each step with the generated SQL.

**H2 (default, nothing to install):**

```bash
mvn clean install
mvn compile exec:java
```

**PostgreSQL in Docker:**

```bash
docker compose up -d
mvn compile exec:java -Ddb=postgres
```

**Local PostgreSQL:** create the database `gym_crm`, then

```bash
mvn compile exec:java -Ddb=postgres -Ddb.username=<user> -Ddb.password=<password>
```

Tables are created by Hibernate on every start; initial data comes from `src/main/resources/db/data.sql`.

## Tests and coverage

```bash
mvn clean install
```

Coverage report: `target/site/jacoco/index.html` (row **Total**, column **Lines**).

## Who can do what

Every operation except creating a profile takes the `Credentials` (username and password) of the user who performs it.

| Operation                                           | Who must sign in | Whose data         |
|-----------------------------------------------------|------------------|--------------------|
| Create trainee / trainer profile                    | nobody           | new profile        |
| Check username and password                         | —                | own                |
| Read trainee / trainer profile by username          | any user         | any profile        |
| Change password, update profile, activate/deactivate | the user         | own                |
| Delete trainee profile                              | the trainee      | own                |
| Get trainings by criteria                           | the trainee / trainer | own           |
| Add training                                        | the trainee      | own, with any trainer |
| Get not assigned trainers, update trainers list     | the trainee      | own                |

## Decisions

- Reading a profile by username is allowed to any signed-in user; every other operation works only with the caller's own data.
- Training criteria: dates are inclusive; names are compared with the first name, case-insensitive.
- Deleting a trainee deletes the trainee's trainings through the JPA cascade (`CascadeType.REMOVE`);
  for many trainings `ON DELETE CASCADE` in the database would be faster.
- Trainings and profiles are loaded with `JOIN FETCH` in one query to avoid the N+1 problem.
- Models are separate from the JPA entities and are converted by the mappers in `service.mapper`.
  An update changes the loaded entity, and Hibernate saves it at commit (dirty checking).
- Validation: field rules are annotations on the models, checked by the services before any database access;
  business rules are checked in the services; the entities only have column constraints.

## Why Training Type is a separate table

Trainings and trainers refer to a type by id: only existing types can be used (no `Yoga` / `YOGA` duplicates),
trainers and trainings share one list of types, and a type is changed in one place.