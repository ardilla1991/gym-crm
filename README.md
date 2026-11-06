# Gym CRM

Gym CRM module on plain Spring Framework (no Spring Boot) with JPA and Hibernate.

Requirements: JDK 21, Maven 3.9+. Optional: Docker or a local PostgreSQL.

## Run

The demo in `Main` calls every operation through `GymFacade` and logs each step with the generated SQL.

**H2 (default, nothing to install):**

```bash
mvn clean install
mvn -pl api exec:java
```

**PostgreSQL in Docker:**

```bash
docker compose up -d
mvn -pl api exec:java -Ddb=postgres
```

**Local PostgreSQL:** create the database `gym_crm`, then

```bash
mvn -pl api exec:java -Ddb=postgres -Ddb.username=<user> -Ddb.password=<password>
```

Tables are created by Hibernate on every start; initial data comes from `config/src/main/resources/db/data.sql`.

## Tests and coverage

```bash
mvn clean install
```

Coverage report for the whole project: `api/target/site/jacoco-aggregate/index.html` (row **Total**, column **Lines**).

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

- A user can change only their own data; reading a profile by username is allowed to any signed-in user.
- Username: `FirstName.LastName`, plus the first free number if taken (`John.Smith1`). Password: 10 random characters.
- Activate/deactivate is a toggle, so it is not idempotent.
- Trainer specialization is read-only; the username does not change when the name is updated.
- Training criteria: dates are inclusive, names are compared with the first name, case-insensitive.
- Adding a training also adds the trainer to the trainee's trainers list.
- Deleting a trainee deletes the trainee's trainings through the JPA cascade (`CascadeType.REMOVE`).
  Hibernate deletes them one by one; for many trainings `ON DELETE CASCADE` in the database would be faster.
- Trainings and profiles are loaded with `JOIN FETCH` in one query to avoid the N+1 problem.

## Why Training Type is a separate table

Trainings and trainers refer to a type by id: only existing types can be used (no `Yoga` / `YOGA` duplicates),
trainers and trainings share one list of types, and a type is changed in one place.