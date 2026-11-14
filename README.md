# Gym CRM

REST API for a gym CRM on plain Spring Framework 7 (no Spring Boot): Spring MVC on embedded Tomcat,
JPA with Hibernate, H2 or PostgreSQL.

## Layers

One Maven module; the layers are packages.

| Package       | What is there                                                             | Works with                      |
|---------------|---------------------------------------------------------------------------|---------------------------------|
| `api.rest`    | REST controllers, DTOs and their mappers, error handling, logging filters | DTOs outside, models inside     |
| `api`         | `Main`, `GymApplication` (embedded Tomcat), `GymFacade`                   | models                          |
| `service`     | business logic, model validation; mappers in `service.mapper`             | models outside, entities inside |
| `persistence` | DAOs; JPA entities in `persistence.entity`                                | entities                        |
| `model`       | models: Lombok and validation, no JPA                                     | —                               |
| `config`      | Spring and database configuration                                         | —                               |

Requirements: JDK 21, Maven 3.9+. Optional: Docker or a local PostgreSQL.

## Run

**H2 (default, nothing to install):**

```bash
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

The server starts on port 8080; another port: `-Dserver.port=9090`.
Tables are created by Hibernate on every start; initial data comes from `src/main/resources/db/data.sql`.

- Swagger UI: http://localhost:8080/swagger-ui/index.html (press **Authorize** and enter a username and password)
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Health check: http://localhost:8080/api/v1/health

## Try it

Users from `data.sql`:

| Username         | Password     | Role    |
|------------------|--------------|---------|
| `Emily.Johnson`  | `Hq5sW1dF6y` | trainee |
| `Michael.Brown`  | `Jp2gK8mT4n` | trainee |
| `David.Miller`   | `Xk3mP9qL2w` | trainer |
| `Jessica.Wilson` | `Rt7vN4bZ8c` | trainer |

Examples for Git Bash (in PowerShell use `curl.exe` and other quotes). `-u` sends the HTTP Basic header.

```bash
# Register a trainee: returns the generated username and password
curl -i -X POST http://localhost:8080/api/v1/trainees -H "Content-Type: application/json" \
     -d '{"firstName":"Anna","lastName":"Lee"}'

# Login
curl -i -u Emily.Johnson:Hq5sW1dF6y http://localhost:8080/api/v1/login

# Own trainings in October
curl -i -u Emily.Johnson:Hq5sW1dF6y \
     "http://localhost:8080/api/v1/trainees/Emily.Johnson/trainings?periodFrom=2026-10-01&periodTo=2026-10-31"
```

## Endpoints

All paths start with `/api/v1`. "Owner" means the username in the path (or `traineeUsername` in the body)
must be the signed-in user.

| #  | Operation                                  | Method and path                                   | Access        |
|----|--------------------------------------------|---------------------------------------------------|---------------|
| 1  | Trainee registration                       | `POST /trainees`                                  | no sign-in    |
| 2  | Trainer registration                       | `POST /trainers`                                  | no sign-in    |
| 3  | Login                                      | `GET /login`                                      | any user      |
| 4  | Change login (password)                    | `PUT /login/password`                             | any user      |
| 5  | Get trainee profile                        | `GET /trainees/{username}`                        | any user      |
| 6  | Update trainee profile                     | `PUT /trainees/{username}`                        | owner         |
| 7  | Delete trainee profile                     | `DELETE /trainees/{username}`                     | owner         |
| 8  | Get trainer profile                        | `GET /trainers/{username}`                        | any user      |
| 9  | Update trainer profile                     | `PUT /trainers/{username}`                        | owner         |
| 10 | Active trainers not assigned to trainee    | `GET /trainees/{username}/unassigned-trainers`    | owner         |
| 11 | Update trainee's trainers list             | `PUT /trainees/{username}/trainers`               | owner         |
| 12 | Trainee trainings by criteria              | `GET /trainees/{username}/trainings?periodFrom=&periodTo=&trainerName=&trainingType=` | owner |
| 13 | Trainer trainings by criteria              | `GET /trainers/{username}/trainings?periodFrom=&periodTo=&traineeName=` | owner |
| 14 | Add training                               | `POST /trainings`                                 | owner (trainee) |
| 15 | Activate / deactivate trainee              | `PATCH /trainees/{username}/active`               | owner         |
| 16 | Activate / deactivate trainer              | `PATCH /trainers/{username}/active`               | owner         |
| 17 | Training types                             | `GET /training-types`                             | any user      |

Request and response bodies with examples are in Swagger UI.

## Authentication

HTTP Basic: `Authorization: Basic base64(username:password)` on every call except registration.
Base64 is an encoding, not encryption, so in production Basic authentication is used only over HTTPS;
this training project runs on plain HTTP.

## Errors

Every error has the same JSON body: `{"status": 404, "error": "Not Found", "message": "..."}`.

| Status | When |
|--------|------|
| 400 | invalid body (the message lists the fields), invalid JSON, wrong date or training type in the query |
| 401 | no or wrong username and password |
| 403 | changing data of another user |
| 404 | no trainee, trainer or training type with this name; unknown path |
| 409 | activating an active profile or deactivating an inactive one |
| 500 | unexpected error: no details in the response, the stack trace is in the log |

## Logging

Logback, configured in `src/main/resources/logback.xml`.

- **Transaction id.** Every REST call gets a transactionId: from the `X-Transaction-Id` request header if it is valid,
  otherwise a new UUID. It is put into the MDC, so every log line of the call (controller, service, DAO and Hibernate SQL)
  shows `[txId=...]`, and it is returned in the `X-Transaction-Id` response header.
- **REST call details.** One line per call to `/api/v1`: endpoint, request body, status, response body or error, duration.
  2xx are logged as INFO, 4xx as WARN, 5xx as ERROR. Passwords are masked, the `Authorization` header is not logged.

```
20:45:10.236 INFO  [txId=3f2b6c1e-...] c.e.t.g.a.r.l.RequestLoggingFilter - REST POST /api/v1/trainees -> 201 in 41 ms,
request: {"firstName":"Anna","lastName":"Lee"}, response: {"username":"Anna.Lee","password":"***"}
```

## Tests and coverage

```bash
mvn clean install
```

Coverage report: `target/site/jacoco/index.html` (row **Total**, column **Lines**).
Not covered on purpose: `Main` and `GymApplication.await()` (they start and block the server;
`GymApplicationTest` starts and stops the whole application instead).

## Decisions

- Not both a trainer and a trainee (note 2): every registration creates a new user with its own username,
  so one username always belongs either to a trainee or to a trainer; no extra check is needed.
- Reading a trainee or trainer profile by username is allowed to any signed-in user;
  every other operation works only with the signed-in user's own data.
- Activate/deactivate takes the wanted status (`{"isActive": false}`) instead of toggling it;
  the same status again returns 409, so the call is not idempotent.
- Updates use PUT and replace the whole profile: an optional field that is not sent becomes empty.
- Registration returns 201 Created, because it creates a resource.
- The training type is not sent when adding a training: it is the trainer's specialization.
- Training criteria are query parameters: dates are inclusive (ISO `2026-01-31`),
  names are compared with the first name, case-insensitive.
- Deleting a trainee deletes the trainee's trainings through the JPA cascade (`CascadeType.REMOVE`).
  Hibernate deletes them one by one; for many trainings `ON DELETE CASCADE` in the database would be faster.
- Trainings and profiles are loaded with `JOIN FETCH` in one query to avoid the N+1 problem.
- Models are separate from the JPA entities and are converted by the mappers in `service.mapper`.
  An update changes the loaded entity, and Hibernate saves it at commit (dirty checking).
- Validation: request bodies are checked in the controllers (`@Valid` on the DTOs, 400 on error);
  field rules on the models are checked by the services before any database access, after authentication;
  business rules are checked in the services; the entities only have column constraints.
- Swagger UI comes from springdoc. springdoc is made for Spring Boot, so without Boot its configuration is registered
  by hand in `WebConfig`, as the springdoc FAQ describes; `spring-boot-autoconfigure` is only a library it depends on,
  the application itself does not use Spring Boot.

## Why Training Type is a separate table

Trainings and trainers refer to a type by id: only existing types can be used (no `Yoga` / `YOGA` duplicates),
trainers and trainings share one list of types, and a type is changed in one place.
