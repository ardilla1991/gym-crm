# Gym CRM

REST API for a gym CRM on Spring Boot 4: Spring MVC on embedded Tomcat,
JPA with Hibernate, H2 or PostgreSQL. Requirements: JDK 21, Maven 3.9+; optional Docker or a local PostgreSQL.

## Layers

One Maven module; the layers are packages.

| Package       | What is there                                                             | Works with                      |
|---------------|---------------------------------------------------------------------------|---------------------------------|
| `api.rest`    | REST controllers, DTOs and their mappers, error handling, logging filters | DTOs outside, models inside     |
| `api`         | `GymFacade`                                                               | models                          |
| `service`     | business logic, model validation                                          | models                          |
| `persistence` | DAOs; JPA entities and mappers                                            | models outside, entities inside |
| `model`       | models: Lombok and validation, no JPA                                     | —                               |

`GymApplication` (the root package) starts Spring Boot. Settings: `src/main/resources/application.yml`.

## Run

Each environment is a Spring profile with its own database (`src/main/resources/application-<profile>.yml`).

**local** (default): H2 in memory, data from `db/data.sql` on every start.
```powershell
mvn spring-boot:run
```

**dev**: PostgreSQL in Docker.
```powershell
docker compose up -d
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```
First start on an empty database: add `"-Dspring-boot.run.arguments=--spring.jpa.hibernate.ddl-auto=create"`.

**stg**: the database comes only from environment variables.
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/gym_crm"
$env:DB_USERNAME="gym"
$env:DB_PASSWORD="gym"
mvn spring-boot:run "-Dspring-boot.run.profiles=stg"
```

**prod**: the database comes only from environment variables.
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/gym_crm"
$env:DB_USERNAME="gym"
$env:DB_PASSWORD="gym"
mvn spring-boot:run "-Dspring-boot.run.profiles=prod"
```

The start log shows the profile: `The following 1 profile is active: "stg"`.

- Swagger UI: http://localhost:8080/swagger-ui/index.html (**Authorize** with a user below)
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health check: http://localhost:8080/api/v1/health
- Actuator health: http://localhost:8080/actuator/health; own checks: `trainingTypes` (all training types are in the database), `memory` (heap used below 90%)

Users from `data.sql`:

| Username         | Password     | Role    |
|------------------|--------------|---------|
| `Emily.Johnson`  | `Hq5sW1dF6y` | trainee |
| `Michael.Brown`  | `Jp2gK8mT4n` | trainee |
| `David.Miller`   | `Xk3mP9qL2w` | trainer |
| `Jessica.Wilson` | `Rt7vN4bZ8c` | trainer |

```bash
curl -i -u Emily.Johnson:Hq5sW1dF6y http://localhost:8080/api/v1/login
```

## Endpoints

Paths start with `/api/v1`. "Owner": the username in the path (or `traineeUsername` in the body) is the signed-in user.

| #  | Operation                                | Method and path                                | Access          |
|----|------------------------------------------|------------------------------------------------|-----------------|
| 1  | Trainee registration                     | `POST /trainees`                               | no sign-in      |
| 2  | Trainer registration                     | `POST /trainers`                               | no sign-in      |
| 3  | Login                                    | `GET /login`                                   | any user        |
| 4  | Change login (password)                  | `PUT /login/password`                          | any user        |
| 5  | Get trainee profile                      | `GET /trainees/{username}`                     | any user        |
| 6  | Update trainee profile                   | `PUT /trainees/{username}`                     | owner           |
| 7  | Delete trainee profile                   | `DELETE /trainees/{username}`                  | owner           |
| 8  | Get trainer profile                      | `GET /trainers/{username}`                     | any user        |
| 9  | Update trainer profile                   | `PUT /trainers/{username}`                     | owner           |
| 10 | Active trainers not assigned to trainee  | `GET /trainees/{username}/unassigned-trainers` | owner           |
| 11 | Update trainee's trainers list           | `PUT /trainees/{username}/trainers`            | owner           |
| 12 | Trainee trainings by criteria            | `GET /trainees/{username}/trainings`           | owner           |
| 13 | Trainer trainings by criteria            | `GET /trainers/{username}/trainings`           | owner           |
| 14 | Add training                             | `POST /trainings`                              | owner (trainee) |
| 15 | Activate / deactivate trainee            | `PATCH /trainees/{username}/active`            | owner           |
| 16 | Activate / deactivate trainer            | `PATCH /trainers/{username}/active`            | owner           |
| 17 | Training types                           | `GET /training-types`                          | any user        |

Bodies, query parameters and examples are in Swagger UI.

## Authentication

HTTP Basic on every call except registration and the health check. The credentials are checked first
(`AuthenticationInterceptor`), so wrong ones give 401 before any 400 or 403.
Passwords have exactly 10 characters. Basic sends them in plain Base64: fine for this project on HTTP, HTTPS only in production.

## Errors

Every error has the body `{"status": 404, "error": "Not Found", "message": "..."}`.

| Status | When |
|--------|------|
| 400 | invalid body or query parameter (the message names the field), a rule checked by the service, invalid JSON |
| 401 | no or wrong username and password |
| 403 | another user's data |
| 404 | no such trainee, trainer or training type; unknown path |
| 405 | method not supported by the path (see the `Allow` header) |
| 409 | the profile already has this active status; training with an inactive trainer; the same username registered at the same moment |
| 415 | no `Content-Type: application/json` |
| 500 | unexpected error, details only in the log |

## Logging

Logback, `src/main/resources/logback.xml`: the format and the default log levels.
A profile overrides them in `application-<profile>.yml`.

- Every call gets a transactionId (from the `X-Transaction-Id` header or a new UUID); it is in every log line of the call
  and in the response header.
- Each call to `/api/v1` logs `started` and `completed` with status, duration, request and response; 2xx INFO, 4xx WARN, 5xx ERROR.
  Passwords and the `Authorization` header are not logged.
- Line breaks in messages become spaces, so client input can not fake a log line.

```
20:45:10.195 INFO  [txId=3f2b6c1e-...] c.e.t.g.a.r.l.RequestLoggingFilter - REST POST /api/v1/trainees started
20:45:10.214 DEBUG [txId=3f2b6c1e-...] org.hibernate.SQL - insert into users (...) values (...)
20:45:10.236 INFO  [txId=3f2b6c1e-...] c.e.t.g.a.r.l.RequestLoggingFilter - REST POST /api/v1/trainees completed: 201 in 41 ms,
request: {"firstName":"Anna","lastName":"Lee"}, response: {"username":"Anna.Lee","password":"***"}
```

## Tests and coverage

```bash
mvn clean install
```

Coverage report: `target/site/jacoco/index.html`.

## Decisions

- Activate/deactivate takes the wanted status (`{"isActive": false}`); the same status again is 409 (the task's "not idempotent").
- PUT replaces the whole profile: an optional field that is not sent becomes empty.
- Training criteria: dates are inclusive, periodFrom after periodTo is 400, a blank name is no filter,
  a name matches the first name or the full name, case-insensitive.
- Length limits are defined once in `FieldLimits` and used by both the validation and the database columns,
  so a value that passes validation always fits its column.
- Names allow only Latin letters, spaces, hyphens and apostrophes: they become the username,
  which must work in HTTP Basic (`:` splits it) and in URL paths.
- Services check the password too, so they are safe without REST; the check runs in the database, models have no password.
- Passwords are stored as plain text: a known limitation (real systems use BCrypt or PBKDF2).
