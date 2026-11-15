# Gym CRM

REST API for a gym CRM on plain Spring Framework 7 (no Spring Boot): Spring MVC on embedded Tomcat,
JPA with Hibernate, H2 or PostgreSQL.

## Layers

One Maven module; the layers are packages.

| Package       | What is there                                                             | Works with                      |
|---------------|---------------------------------------------------------------------------|---------------------------------|
| `api.rest`    | REST controllers, DTOs and their mappers, error handling, logging filters | DTOs outside, models inside     |
| `api`         | `Main`, `GymApplication` (embedded Tomcat), `GymFacade`                   | models                          |
| `service`     | business logic, model validation                                          | models                          |
| `persistence` | DAOs; JPA entities and mappers                                            | models outside, entities inside |
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
# first start on an empty database: creates the tables and loads data.sql
mvn compile exec:java -Ddb=postgres -Ddb.schema=create
# every later start: keeps the data
mvn compile exec:java -Ddb=postgres
```

**Local PostgreSQL:** create the database `gym_crm`, then

```bash
mvn compile exec:java -Ddb=postgres -Ddb.username=<user> -Ddb.password=<password>
```

In PowerShell put each `-D` option in quotes: `mvn compile exec:java "-Ddb=postgres" "-Ddb.schema=create"`.

The server starts on port 8080; another port: `-Dserver.port=9090`.
H2 is recreated on every start: Hibernate creates the tables and loads `src/main/resources/db/data.sql`.
PostgreSQL keeps its data: Hibernate only validates the tables. On the first start with an empty database
add `-Ddb.schema=create`, it creates the tables and loads `data.sql` (and would recreate them on any later start).

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

HTTP Basic: `Authorization: Basic base64(username:password)` on every call except registration and the health check
(`/api/v1/health` is a technical endpoint for monitoring, not one of the operations of the task).
Base64 is an encoding, not encryption, so in production Basic authentication is used only over HTTPS;
this training project runs on plain HTTP.

Generated passwords and new passwords supplied to `PUT /api/v1/login/password` have exactly 10 characters.
A blank new password or a password of a different length is rejected with 400.

The username and password are checked before the request is read (`AuthenticationInterceptor`),
so wrong credentials always give 401, before a 400 for an invalid request or a 403 for another user's data.
Registration and `/api/v1/health` need no authentication.

## Errors

Every error has the same JSON body: `{"status": 404, "error": "Not Found", "message": "..."}`.

| Status | When |
|--------|------|
| 400 | invalid body (the message lists the fields): a required field is missing, a name has other characters than Latin letters, spaces, hyphens and apostrophes, a value is too long, a date of birth is not in the past; a rule checked by the service: an empty or repeated trainers list, a new password of a length other than 10; invalid JSON; wrong date or training type in the query |
| 401 | no or wrong username and password |
| 403 | changing data of another user |
| 404 | no trainee, trainer or training type with this name; unknown path |
| 405 | the path does not support this HTTP method; the `Allow` header lists the methods it does support |
| 409 | activating an active profile or deactivating an inactive one; adding a training with an inactive trainer; a registration that took the same username as another one at the same moment (repeat the request) |
| 415 | the body is sent without `Content-Type: application/json` |
| 500 | unexpected error: no details in the response, the stack trace is in the log |

## Logging

Logback, configured in `src/main/resources/logback.xml`.

- **Transaction id.** Every REST call gets a transactionId: from the `X-Transaction-Id` request header if it is valid,
  otherwise a new UUID. It is put into the MDC, so every log line of the call (controller, service, DAO and Hibernate SQL)
  shows `[txId=...]`, and it is returned in the `X-Transaction-Id` response header.
- **REST call details.** Two lines per call to `/api/v1`: `started` with the endpoint when the call comes in, and at the end
  the endpoint, status, request body, response body or error, and duration. Service and SQL lines are between them.
  2xx are logged as INFO, 4xx as WARN, 5xx as ERROR. Passwords are masked: the request body of the password change
  is not logged, query parameters named like `password` are masked; the `Authorization` header is not logged.
- **One record is one line.** Line breaks inside a message are replaced with spaces (`%replace` in `logback.xml`),
  so a value from the client (a query parameter, a username) can not start a fake log line without the transactionId.

```
20:45:10.195 INFO  [txId=3f2b6c1e-...] c.e.t.g.a.r.l.RequestLoggingFilter - REST POST /api/v1/trainees started
20:45:10.214 DEBUG [txId=3f2b6c1e-...] org.hibernate.SQL - insert into users (first_name,is_active,last_name,password,username,id) values (?,?,?,?,?,default)
...
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

- Any signed-in user may read any trainee or trainer profile; all other operations work only with the user's own data.
- Activate/deactivate takes the wanted status (`{"isActive": false}`); the same status again gives 409,
  which is how the task's "not idempotent" is read (by HTTP terms the effect is idempotent).
- PUT replaces the whole profile: an optional field that is not sent becomes empty.
- Training criteria: dates are inclusive, names are compared with the first name, case-insensitive.
- Deleting a trainee deletes its trainings through `CascadeType.REMOVE`.
- Each layer works with its own types: services see only models, entities never leave `persistence`.
  DAOs convert models and entities with the mappers in `persistence.mapper`, and every change is an explicit DAO call.
- Validation: DTOs in the controllers, models in the services after authentication, column limits in the entities;
  the limits are shared through `FieldLimits`, so a valid value always fits its column.
- Names allow only Latin letters, spaces, hyphens and apostrophes (`@PersonName`): the username is built from them
  and must work in HTTP Basic and in URL paths.
- The password is checked twice: by `AuthenticationInterceptor` (401 before 400 or 403) and by the services,
  which must be safe without REST too. Both checks run in the database (`UserDao.matchesPassword`):
  models read from the database have no password.
- Passwords are stored as plain text: a known limitation; real systems store a salted slow hash (PBKDF2, BCrypt).
- Two registrations of the same name at the same moment: the unique index lets one in, the other gets 409.
- Swagger comes from springdoc, configured by hand in `WebConfig`; the application does not use Spring Boot.

## Why Training Type is a separate table

Trainings and trainers refer to a type by id: only existing types can be used (no `Yoga` / `YOGA` duplicates),
trainers and trainings share one list of types, and a type is changed in one place.
